package com.cramja.crypto.core;

import com.google.common.primitives.Ints;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Block {
    static SecureRandom random = new SecureRandom();

    private BigInteger nonce;
    private int sequence;
    private List<Txn> txns; // the 0th txn is the miner's txn
    private Block previous;

    public Block(int sequence, List<Txn> txns, Block previous) {
        byte[] nonceSeed = new byte[16];
        random.nextBytes(nonceSeed);
        this.nonce = new BigInteger(nonceSeed);
        this.sequence = sequence;
        this.txns = txns;
        this.previous = previous;
    }

    public static Block init(String id) {
        LinkedList<Txn> txns = new LinkedList<>();
        txns.addFirst(Txn.sourceTxn(id));
        return new Block(0, txns, null);
    }

    public static Block newBlock(Block latest, Block previous) {
        Set<Txn> pending = new HashSet<>(previous.getTxns());
        Block cursor = latest;
        while (previous.getSequence() != cursor.getSequence()) {
            pending.removeAll(cursor.getTxns());
            cursor = cursor.getPrevious();
        }

        LinkedList<Txn> txns = new LinkedList<>(latest.getTxns());
        txns.removeFirst();
        txns.addFirst(previous.getTxns().get(0));
        txns.addAll(pending);

        return new Block(latest.getSequence(), txns, latest.getPrevious());
    }

    public Block getPrevious() {
        return previous;
    }

    public List<Txn> getTxns() {
        return txns;
    }

    public int getSequence() {
        return sequence;
    }

    public void addTxn(Txn txn) {
        this.txns.add(txn);
    }

    public void inc() {
        this.nonce = nonce.add(BigInteger.ONE);
    }

    public byte[] hash() {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(nonce.toByteArray());
            md.update(Ints.toByteArray(sequence));
            md.update(Ints.toByteArray(txns.hashCode()));
            if (previous != null) {
                md.update(previous.nonce.toByteArray());
            }
            return md.digest();
        } catch (Exception e) {
            throw new RuntimeException("unable to hash block", e);
        }
    }

    /**
     * @param otherBlock
     * @return a new block with all transactions of otherBlock (sans miner's txn)
     */
    public Block merge(Block otherBlock) {
        HashSet<Txn> txns = new HashSet<>(otherBlock.getTxns().subList(1, otherBlock.getTxns().size()));
        txns.addAll(this.getTxns().subList(1, this.getTxns().size()));
        LinkedList<Txn> merged = new LinkedList<>(txns);
        merged.addFirst(this.txns.get(0));
        return new Block(this.sequence, merged, previous);
    }
}
