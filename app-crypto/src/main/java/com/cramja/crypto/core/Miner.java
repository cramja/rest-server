package com.cramja.crypto.core;

import static com.cramja.crypto.core.Helpers.checkProofOfWork;

import com.cramja.crypto.core.Network.NetworkSubscriber;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.security.auth.x500.X500PrivateCredential;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Miner implements NetworkSubscriber, Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Miner.class);
    private static final ChainConfig config = new ChainConfig();

    private final Long id;
    private final X500PrivateCredential credentials;

    private Network network;
    private ReentrantReadWriteLock networkLock = new ReentrantReadWriteLock();
    private List<Block> queuedBlocks = new LinkedList<>();
    private List<Txn> queuedTxns = new LinkedList<>();

    private Ledger ledger;

    private boolean running = false;
    private PriorityQueue<Task> scheduledTasks = new PriorityQueue<>();

    private Block work;

    public Miner(Long id) {
        this.id = id;
        this.credentials = Crypto.createSelfSignedCredentials();
        this.work = Block.init(id);
        this.ledger = new Ledger();
    }

    public void init(Network network) {
        if (this.network != null) {
            throw new IllegalStateException();
        }
        this.network = network;
        network.subscribe(this);
    }

    public Long getId() {
        return id;
    }

    @Override
    public void run() {
        if (!running) {
            this.running = true;
        } else {
            return;
        }
        // sync with peers
        scheduledTasks.add(new SyncTxnsTask(1_000));
        scheduledTasks.add(new SyncBlocksTask(10_000));

        while (running) {
            if (scheduledTasks.peek().ready()) {
                Task t = scheduledTasks.poll();
                t.run();
                scheduledTasks.add(t.scheduleNextInterval());
            } else {
                while (running && !scheduledTasks.peek().ready()) {
                    work.inc();
                    byte[] hash = work.hash();
                    if (checkProofOfWork(hash, config.proofOfWorkLength())) {
                        logger.debug("{} found proof of work", id);
                        network.broadcast(work);
                        // TODO: fix
//                        work = new Block(work.getSequence() + 1, Collections.singletonList(Txn.sourceTxn(id)), work);
                    }
                }
            }
        }
    }

    public void stop() {
        this.running = false;
        if (this.network != null) {
            network.unsubscribe(this);
            network = null;
        }
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
        return Objects.equals(id, miner.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public void accept(Block block) {
        withWriteLock(() -> queuedBlocks.add(block));
    }

    @Override
    public void accept(Txn txn) {
        withWriteLock(() -> queuedTxns.add(txn));
    }

    private void withWriteLock(Runnable r) {
        networkLock.writeLock().lock();
        try {
            r.run();
        } finally {
            networkLock.writeLock().unlock();
        }
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

        public Task scheduleNextInterval() {
            final long now = System.currentTimeMillis();
            if (exeTime + interval < now) {
                exeTime = now + interval;
            } else {
                exeTime += interval;
            }
            return this;
        }
    }

    class SyncBlocksTask extends Task {
        public SyncBlocksTask(long interval) {
            super(interval);
        }

        @Override
        void run() {
            List<Block> blocks = Miner.this.queuedBlocks;
            withWriteLock(() -> Miner.this.queuedBlocks = new LinkedList<>());

            for (Block block : blocks) {
                if (block.getSequence() > Miner.this.work.getSequence()) {
                    // TODO: fix
//                    Miner.this.work = Block.newBlock(block, Miner.this.work);
                    logger.info("SyncBlocksTask fast-forward: {} -> seq({})", Miner.this.id, block.getSequence());
                }
            }
        }
    }

    class SyncTxnsTask extends Task {
        public SyncTxnsTask(long interval) {
            super(interval);
        }

        @Override
        void run() {
            List<Txn> txns = Miner.this.queuedTxns;
            withWriteLock(() -> Miner.this.queuedTxns = new LinkedList<>());

            for (Txn txn : txns) {
                // TODO: check that TXN has not already been submitted.
//                Miner.this.work.addTxn(txn);
            }
        }
    }

    static class ChainConfig {
        /**
         * @return number of trailing zero bits in hash for a block to be accepted.
         */
        public int proofOfWorkLength() {
            return 8 + 8 + 6;
        }
    }
}
