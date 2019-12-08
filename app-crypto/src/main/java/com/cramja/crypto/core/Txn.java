package com.cramja.crypto.core;

import static java.util.Collections.singletonList;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;

public class Txn {
    private List<Change> changes;

    public Txn(List<Change> changes) {
        this.changes = changes;
    }

    public Txn(Change ... changes) {
        this.changes = Lists.newArrayList(changes);
    }

    public static Txn sourceTxn(Long id) {
        return new Txn(singletonList(new Change(id, 1.0)));
    }

    public List<Change> getChanges() {
        return changes;
    }

    public boolean isSourceTxn() {
        return changes.size() == 1;
    }

    public int hashCode() {
        return Objects.hash(changes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Txn txn = (Txn) o;
        return Objects.equals(changes, txn.changes);
    }

    public static class Change {
        private Long id;
        private double amt;

        public Change(Long id, double amt) {
            this.id = id;
            this.amt = amt;
        }

        public static Change of(long id, double amt) {
            return new Change(id, amt);
        }

        public Long getId() {
            return id;
        }

        public double getAmt() {
            return amt;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, amt);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Change change = (Change) o;
            return Double.compare(change.amt, amt) == 0 &&
                    Objects.equals(id, change.id);
        }
    }

}
