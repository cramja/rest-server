package com.cramja.crypto.core;

import static com.cramja.crypto.core.Helpers.checkProofOfWork;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;
import javax.security.auth.x500.X500PrivateCredential;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Miner implements Peer, Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Miner.class);
    private static final MinerConfig config = new MinerConfig();

    private final String name;
    private final X500PrivateCredential credentials;

    private List<Peer> peers = new LinkedList<>();
    private boolean running = false;
    private PriorityQueue<Task> scheduledTasks = new PriorityQueue<>();

    private Block work;

    public Miner(String name) {
        this.name = name;
        this.credentials = Crypto.createSelfSignedCredentials();
        this.work = Block.init(name);
    }

    @Override
    public void run() {
        if (!running) {
            this.running = true;
        } else {
            return;
        }
        // sync with peers
        scheduledTasks.add(new SyncPeersTask(5_000));
        scheduledTasks.add(new SyncTxnsTask(10_000));

        while (running) {
            if (scheduledTasks.peek().ready()) {
                Task t = scheduledTasks.poll();
                t.run();
                t.scheduleNextInterval();
                scheduledTasks.add(t);
            } else {
                for (int i = 0; i < 100_000; i++) {
                    work.inc();
                    byte[] hash = work.hash();
                    if (checkProofOfWork(hash, config.proofOfWorkLength())) {
                        logger.debug("found proof of work {}", getId());
                        // TODO: this should broadcast the proven block.
                        work = new Block(work.getSequence() + 1, Collections.singletonList(Txn.sourceTxn(getId())), work);
                    }
                }
            }
        }
    }

    public void stop() {
        this.running = false;
    }

    @Override
    public List<Peer> getPeers() {
        return ImmutableList.copyOf(peers);
    }

    @Override
    public void register(Peer peer) {
        if (!peers.contains(peer)) {
            peers.add(peer);
        }
    }

    public void unregister(Peer peer) {
        peers.remove(peer);
    }

    @Override
    public String getId() {
        return name;
    }

    @Override
    public boolean isAlive() {
        return running;
    }

    @Override
    public Block getWorkingBlock() {
        return work;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Miner miner = (Miner) o;
        return Objects.equals(name, miner.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name;
    }

    abstract class Task implements Comparable<Task> {
        long exeTime;

        long interval;

        public Task(long interval) {
            this.exeTime = System.currentTimeMillis();
            this.interval = interval;
        }

        abstract void run();

        public boolean ready() {
            return exeTime < System.currentTimeMillis();
        }

        public long getExeTime() {
            return exeTime;
        }

        @Override
        public int compareTo(@NotNull Task o) {
            return Long.compare(exeTime, o.exeTime);
        }

        public void scheduleNextInterval() {
            final long now = System.currentTimeMillis();
            if (exeTime + interval < now) {
                exeTime = now + interval;
            } else {
                exeTime += interval;
            }
        }

    }

    class SyncPeersTask extends Task {

        public SyncPeersTask(long interval) {
            super(interval);
        }

        @Override
        void run() {
            HashSet<String> checked = new HashSet<>();
            checked.add(getId());

            List<Peer> peers = new LinkedList<>();
            LinkedList<Peer> candidates = new LinkedList<>(Miner.this.peers);
            while (!candidates.isEmpty()) {
                Peer candidate = candidates.pop();
                if (checked.contains(candidate.getId())) continue;
                if (!candidate.isAlive()) continue;

                checked.add(candidate.getId());
                candidate.register(Miner.this);
                peers.add(candidate);
                candidates.addAll(candidate.getPeers());
            }
            Miner.this.peers = peers;

            logger.info("SYNC_PEER {}:{}", Miner.this, Miner.this.peers);
        }

    }

    class SyncTxnsTask extends Task {

        public SyncTxnsTask(long interval) {
            super(interval);
        }

        @Override
        void run() {
            for (Peer p : Miner.this.peers) {
                Block peerBlock = p.getWorkingBlock();
                if (peerBlock.getSequence() > Miner.this.work.getSequence()) {
                    Miner.this.work = Block.newBlock(peerBlock, Miner.this.work);
                    logger.info("SYNC_TXN fast-forward: {} -> {}[{}]", getId(), p.getId(), peerBlock.getSequence());
                } else {
                    Miner.this.work = Miner.this.work.merge(peerBlock);
                    logger.info("SYNC_TXN merge: {} <- {}", getId(), p.getId());
                }
            }
        }
    }

    static class MinerConfig {
        /**
         * @return number of trailing zero bits in hash for a block to be accepted.
         */
        public int proofOfWorkLength() {
            return 12;
        }
    }
}
