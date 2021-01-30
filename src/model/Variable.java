package model;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class Variable {

    private static long addressPointer = 0;

    private final String name;
    private byte[] value;
    private final long address;
    private final int size;

    public Variable(String name, int size, byte[] value) {
        this.name = name;
        this.value = value;
        this.size = size;
        address = addressPointer;
        addressPointer += size;
    }

    public String getName() { return name; }

    public byte[] getValue() {
        byte[] result = new byte[8];
        Arrays.fill(result, (byte) 0x00);
        if (value.length < 8)
            System.arraycopy(value, 0, result, 8 - value.length, value.length);
        else
            result = value;

        return result; }

    public void setValue(byte[] value) { this.value = value; }

    public void setValue(long value) {
        this.value = ByteBuffer.allocate(8).putLong(value).array();
    }

    public long getLongValue() {
        byte[] result = new byte[8];
        Arrays.fill(result, (byte) 0x00);
        if (value.length < 8)
            System.arraycopy(value, 0, result, 8 - value.length, value.length);
        else
            result = value;
        return ByteBuffer.wrap(result).getLong();
    }

    public long getAddress() { return address; }

    public int getSize() { return size; }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return String.format("%16s\t\t0x%016x\t%16d", name, address, getLongValue());
    }
}
