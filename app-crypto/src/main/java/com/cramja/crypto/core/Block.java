package com.cramja.crypto.core;

import com.google.common.primitives.Ints;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.List;

public class Block {
    static SecureRandom random = new SecureRandom();

    private BigInteger nonce;
    private int sequence;
    private List<Txn> txns; // the 0th txn is the miner's txn
    private BigInteger previousNonce;

    public Block(int sequence, List<Txn> txns, BigInteger previousNonce) {
        byte[] nonceSeed = new byte[16];
        random.nextBytes(nonceSeed);
        this.nonce = new BigInteger(nonceSeed);
        this.sequence = sequence;
        this.txns = txns;
        this.previousNonce = previousNonce;
    }

    public static Block init(Long id) {
        LinkedList<Txn> txns = new LinkedList<>();
        txns.addFirst(Txn.sourceTxn(id));
        return new Block(0, txns, null);
    }

    public List<Txn> getTxns() {
        return txns;
    }

    public BigInteger getNonce() {
        return nonce;
    }

    public int getSequence() {
        return sequence;
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
            md.update(previousNonce.toByteArray());
            return md.digest();
        } catch (Exception e) {
            throw new RuntimeException("unable to hash block", e);
        }
    }
}
