package com.cramja.crypto.core;

import static com.cramja.crypto.core.Helpers.checkProofOfWork;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class HelpersTest {

    @Test
    public void proofOfWork_whenInvalid_thenFalse() {
        byte[] work = new byte[] {1,1,0};
        assertFalse(checkProofOfWork(work,16));
    }

    @Test
    public void proofOfWork_whenInvalidPartialByte_thenFalse() {
        byte[] work = new byte[] {1,1 << 2,0};
        assertFalse(checkProofOfWork(work,11));
    }

    @Test
    public void proofOfWork_whenValid_thenTrue() {
        byte[] work = new byte[] {1,0, 0};
        assertTrue(checkProofOfWork(work,16));
    }

    @Test
    public void proofOfWork_whenValidPartialByte_thenTrue() {
        byte[] work = new byte[] {1,1 << 3,0};
        assertTrue(checkProofOfWork(work,10));
    }

}
