package com.cavetale.miniverse;

public final class Util {
    private Util() { }

    static final long LEFT  = 0xFFFFFFFF00000000L;
    static final long RIGHT = 0x00000000FFFFFFFFL;

    static long toLong(final int x, final int z) {
        return (((long) z) << 32) | ((long) x & RIGHT);
    }

    static int xFromLong(final long k) {
        return (int) (k & RIGHT);
    }

    static int zFromLong(final long k) {
        return (int) ((k >> 32) & RIGHT);
    }
}
