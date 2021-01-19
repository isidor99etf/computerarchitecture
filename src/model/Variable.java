package model;

import java.nio.ByteBuffer;

public class Variable {

    private static long addressPointer = 0;

    private final String name;
    private byte[] value;
    private final long address;

    public Variable(String name, byte[] value) {
        this.name = name;
        this.value = value;
        address = addressPointer;
        addressPointer += 8;
    }

    public String getName() { return name; }

    public byte[] getValue() { return value; }

    public void setValue(byte[] value) { this.value = value; }

    public long getLongValue() { return ByteBuffer.wrap(value).getLong(); }

    public long getAddress() { return address; }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return String.format("0x%08x", address) + ": " + name + " ";
    }
}
