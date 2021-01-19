package model;

import java.nio.ByteBuffer;
import java.util.HashMap;

public class Register {

    public static final String REGISTER_RAX = "rax";
    public static final String REGISTER_RBX = "rbx";
    public static final String REGISTER_RCX = "rcx";
    public static final String REGISTER_RDX = "rdx";
    public static final String REGISTER_DRE = "dre";
    public static final String REGISTER_MOD = "mod";

    private final String name;
    private byte[] bytes;
    private int valueType;

    public static final HashMap<String, Register> registers;

    static {
        registers = new HashMap<>();

        registers.put(REGISTER_RAX, new Register(REGISTER_RAX));
        registers.put(REGISTER_RBX, new Register(REGISTER_RBX));
        registers.put(REGISTER_RCX, new Register(REGISTER_RCX));
        registers.put(REGISTER_RDX, new Register(REGISTER_RDX));
        registers.put(REGISTER_DRE, new Register(REGISTER_DRE));
        registers.put(REGISTER_MOD, new Register(REGISTER_MOD));
    }

    private Register(String name) {
        this.name = name;
        bytes = new byte[8];
    }

    public String getName() { return name; }

    public void setValue(long value, int valueType) {
        this.valueType = valueType;
        bytes = ByteBuffer.allocate(8).putLong(value).array();
    }

    public void setValue(byte[] bits, int valueType) {
        this.valueType = valueType;
        this.bytes = bits;
    }

    public byte[] getBytes() { return bytes; }

    public int getValueType() { return valueType; }

    public long getValue() { return ByteBuffer.wrap(bytes).getLong(); }
}
