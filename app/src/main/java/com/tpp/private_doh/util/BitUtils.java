package com.tpp.private_doh.util;

public class BitUtils {
    public static short getUnsignedByte(byte value) {
        return (short) (value & 0xFF);
    }

    public static int getUnsignedShort(short value) {
        return value & 0xFFFF;
    }

    public static long getUnsignedInt(int value) {
        return value & 0xFFFFFFFFL;
    }

    public static short intToShort(int value) {
        return (short) (value & 0xFFFF);
    }
}


