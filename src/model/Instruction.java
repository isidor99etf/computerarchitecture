package model;

import constants.InstructionType;

import java.util.Arrays;

public class Instruction {

    private static final String[] INSTRUCTIONS = {
            InstructionType.INSTRUCTION_ADD,
            InstructionType.INSTRUCTION_SUB,
            InstructionType.INSTRUCTION_MUL,
            InstructionType.INSTRUCTION_DIV,
            InstructionType.INSTRUCTION_AND,
            InstructionType.INSTRUCTION_OR,
            InstructionType.INSTRUCTION_NOT,
            InstructionType.INSTRUCTION_SFL,
            InstructionType.INSTRUCTION_SFR,
            InstructionType.INSTRUCTION_MOV,
            InstructionType.INSTRUCTION_CMP,
            InstructionType.INSTRUCTION_JE,
            InstructionType.INSTRUCTION_JNE,
            InstructionType.INSTRUCTION_JGE,
            InstructionType.INSTRUCTION_JG,
            InstructionType.INSTRUCTION_JLE,
            InstructionType.INSTRUCTION_JL,
            InstructionType.INSTRUCTION_INP,
            InstructionType.INSTRUCTION_OUT
    };

    public static boolean checkInstruction(String instruction) {
        return Arrays.stream(INSTRUCTIONS).anyMatch(ins -> ins.equals(instruction.toLowerCase()));
    }
}
