package model;

public class Variable {

    private final String name;
    private byte[] value;
    private final int address;

    public Variable(String name, byte[] value) {
        this.name = name;
        this.value = value;
        address = hashCode();
    }

    public String getName() { return name; }

    public byte[] getValue() { return value; }

    public void setValue(byte[] value) { this.value = value; }

    public int getAddress() { return address; }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return String.format("0x%016x", address) + ": " + name + " ";
    }
}
