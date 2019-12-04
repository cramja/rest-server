package com.cramja.rest.core.util;

public class Either<L,R> extends Pair<L,R> {

    private Either(L l, R r) {
        super(l, r);
    }

    public static <A, B> Either<A, B> ofL(A l) {
        return new Either<>(l, null);
    }

    public static <A, B> Either<A, B> ofR(B r) {
        return new Either<>(null, r);
    }

}
