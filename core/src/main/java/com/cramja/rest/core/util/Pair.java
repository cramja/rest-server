package com.cramja.rest.core.util;

import java.util.Objects;

public class Pair<L,R> {

    protected final L l;
    protected final R r;

    protected Pair(L l, R r) {
        this.l = l;
        this.r = r;
    }

    public static <A, B> Pair<A, B> of(A l, B r) {
        return new Pair<>(l, r);
    }

    public static <A, B> Pair<A, B> ofL(A l) {
        return new Pair<>(l, null);
    }

    public static <A, B> Pair<A, B> ofR(B r) {
        return new Pair<>(null, r);
    }

    public L left() {
        return l;
    }

    public boolean hasL() {
        return l != null;
    }

    public R right() {
        return r;
    }

    public boolean hasR() {
        return r != null;
    }

    @Override
    public String toString() {
        return "Pair{l=" + l + ", r=" + r + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(l, pair.l) &&
                Objects.equals(r, pair.r);
    }

    @Override
    public int hashCode() {
        return Objects.hash(l, r);
    }
}
