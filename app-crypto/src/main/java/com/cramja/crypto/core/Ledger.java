package com.cramja.crypto.core;

import com.cramja.crypto.core.Txn.Change;
import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableList;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

// not threadsafe
public class Ledger {

    private List<Block> committed = new ArrayList<>();
    private TxnLog committedLog = new TxnLog();

    private List<Txn> currentGen = new ArrayList<>();
    private TxnLog currentGenLog = new TxnLog();

    public boolean tryAccept(Txn txn) {
        // validate Txn
        HashSet<Long> seenIds = new HashSet<>();
        double movement = 0;
        for (Change change : txn.getChanges()) {
            if (seenIds.contains(change.getId())) {
                return false;
            }
            seenIds.add(change.getId());
            movement += change.getAmt();
        }
        if (Math.abs(movement) > 1e-6 && !txn.isSourceTxn()) {
            return false;
        }

        TxnLog copy = currentGenLog.copy();
        // try perform update
        for (Change change : txn.getChanges()) {
            if (!copy.trySubmit(getCommittedSeq() + 1, change.getId(), change.getAmt())) {
                return false;
            }
        }
        // commit to current gen
        currentGen.add(txn);
        currentGenLog = copy;
        return true;
    }

    public boolean tryAccept(Block block) {
        // validate
        if (getCommittedSeq() != block.getSequence() - 1) {
            return false;
        }
        TxnLog nextGeneration = committedLog.copy();
        for (Txn txn : block.getTxns()) {
            for (Change change : txn.getChanges()) {
                if (!nextGeneration.trySubmit(block.getSequence(), change.getId(), change.getAmt())) {
                    return false;
                }
            }
        }
        // valid, commit
        this.committed.add(block);
        this.committedLog = nextGeneration;
        this.currentGen.clear(); // TODO: don't just toss the old gen, promote if missing
        this.currentGenLog = committedLog.copy();
        return true;
    }

    public void commit() {
        this.committedLog = this.currentGenLog;
        this.currentGenLog = this.committedLog.copy();
    }

    public Block getBlock(int seq) {
        return this.committed.size() > seq ? this.committed.get(seq) : null;
    }

    public Block getCurrentBlock() {
        if (this.committed.isEmpty()) {
            return new Block(0, ImmutableList.copyOf(currentGen), BigInteger.ZERO);
        }
        return new Block(this.committed.size(), ImmutableList.copyOf(currentGen), this.committed.get(this.committed.size() - 1).getNonce());
    }

    public int getCommittedSeq() {
        return this.committed.size() - 1;
    }

    public double getBalance(long id) {
        return this.currentGenLog.getBalance(id);
    }

    static class TxnLog {
        private Map<Long, LinkedList<AccountState>> histories;

        private TxnLog(Map<Long, LinkedList<AccountState>> histories) {
            this.histories = histories;
        }

        TxnLog() {
            this(new HashMap<>());
        }

        public TxnLog copy() {
            HashMap<Long, LinkedList<AccountState>> copy = new HashMap<>();
            for (Entry<Long, LinkedList<AccountState>> entry : histories.entrySet()) {
                copy.put(entry.getKey(), new LinkedList<>(entry.getValue()));
            }
            return new TxnLog(copy);
        }

        public double getBalance(Long id) {
            return getBalance(id, Integer.MAX_VALUE);
        }

        public double getBalance(Long id, int seqLimit) {
            LinkedList<AccountState> changes = histories.get(id);
            if (changes == null) {
                return 0;
            }
            // Note: if these get really long, do binary search
            AccountState lastState = new AccountState(id,0);
            for (AccountState state : changes) {
                if (state.seq >= seqLimit) {
                    return lastState.amt;
                }
                lastState = state;
            }
            return lastState.amt;
        }

        public boolean trySubmit(int seq, Long id, double amt) {
            LinkedList<AccountState> changes = histories.get(id);
            if (changes == null && amt > 0) {
                LinkedList<AccountState> newAccount = new LinkedList<>();
                newAccount.addFirst(new AccountState(amt, seq));
                histories.put(id, newAccount);
                return true;
            } else if (changes == null) {
                return false;
            }

            LinkedList<AccountState> updatedAccount = new LinkedList<>();
            Iterator<AccountState> stateIterator = changes.iterator();
            while (stateIterator.hasNext()) {
                AccountState accountState = stateIterator.next();
                if (accountState.seq <= seq) {
                    updatedAccount.addLast(accountState);
                } else {
                    break;
                }
            }
            AccountState lastUpdated = updatedAccount.peekLast();
            updatedAccount.addLast(new AccountState(lastUpdated == null ? amt : lastUpdated.amt + amt, seq));
            if (updatedAccount.peekLast().amt < 0) {
                return false;
            }
            while (stateIterator.hasNext()) {
                AccountState accountState = stateIterator.next();
                if (accountState.amt + amt < 0) {
                    return false;
                }
                updatedAccount.addLast(new AccountState(accountState.amt + amt, accountState.seq));
            }
            this.histories.put(id, updatedAccount);
            return true;
        }
    }

    private static class AccountState {
        final double amt;
        final int seq;

        public AccountState(double amt, int seq) {
            this.amt = amt;
            this.seq = seq;
        }
    }

}
