package com.cramja.crypto.core;

import static org.junit.Assert.*;

import com.cramja.crypto.core.Txn.Change;
import org.junit.Test;

public class LedgerTest {

    @Test
    public void tryAccept_whenValidTxnSeq_thenAccepts() {
        Ledger ledger = new Ledger();
        assertTrue(ledger.tryAccept(Txn.sourceTxn(1L)));

        Block block0 = ledger.getCurrentBlock();
        assertTrue(ledger.tryAccept(block0));

        assertTrue(ledger.tryAccept(Txn.sourceTxn(2L)));
        assertTrue(ledger.tryAccept(new Txn(Change.of(1L, -0.5), Change.of(2L, 0.5))));
        assertFalse(ledger.tryAccept(new Txn(Change.of(1L, -0.75), Change.of(2L, 0.75))));
        assertFalse(ledger.tryAccept(new Txn(Change.of(1L, -0.75), Change.of(2L, 1.75))));
        Block block1 = ledger.getCurrentBlock();
        assertFalse(ledger.tryAccept(block0));
        assertTrue(ledger.tryAccept(block1));

        assertEquals(0.5, ledger.getBalance(1L), 1e-6);
        assertEquals(1.5, ledger.getBalance(2L), 1e-6);
    }

}