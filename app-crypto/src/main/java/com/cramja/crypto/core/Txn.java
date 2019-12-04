package com.cramja.crypto.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Txn {
    private List<Unit> input;
    private List<Unit> output;

    public Txn(List<Unit> input, List<Unit> output) {
        this.input = input;
        this.output = output;
    }

    public static Txn sourceTxn(String name) {
        return new Txn(Collections.emptyList(), Collections.singletonList(new Unit(name, 1)));
    }

    public List<Unit> getInput() {
        return input;
    }

    public List<Unit> getOutput() {
        return output;
    }

    public boolean isIncentiveTxn() {
        return input.isEmpty() && output.size() == 1;
    }

    public int hashCode() {
        return Objects.hash(input, output);
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
        return Objects.equals(input, txn.input) &&
                Objects.equals(output, txn.output);
    }

    public static class Unit {
        private String id;
        private double amt;

        public Unit(String id, double amt) {
            this.id = id;
            this.amt = amt;
        }

        public String getId() {
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
            Unit unit = (Unit) o;
            return Double.compare(unit.amt, amt) == 0 &&
                    Objects.equals(id, unit.id);
        }
    }
}
