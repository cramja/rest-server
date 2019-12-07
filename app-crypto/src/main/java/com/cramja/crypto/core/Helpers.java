package com.cramja.crypto.core;

public final class Helpers {
    private Helpers() {}

    public static boolean checkProofOfWork(byte[] bytes, int zeroCount) {
        int aIdx = bytes.length - 1;
        while (zeroCount - 8 > 0) {
            if (bytes[aIdx] != 0) {
                return false;
            }
            zeroCount -= 8;
            aIdx -= 1;
        }
        byte lastByte = bytes[aIdx];
        while (zeroCount > 0) {
            if ((lastByte & 1) == 1) {
                return false;
            }
            lastByte >>= 1;
            zeroCount -= 1;
        }
        return true;
    }

}
