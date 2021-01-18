package task;

import constants.Constants;
import constants.InstructionType;
import model.Instruction;
import model.Register;
import model.Variable;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    private static final HashMap<Integer, Variable> memoryAddress = new HashMap<>(); // variable by address (hash code)
    private static final HashMap<String, Variable> memoryName = new HashMap<>(); // variable by name

    private static final Pattern intPattern = Pattern.compile("-?\\d+");
    private static final Pattern bracketsPattern = Pattern.compile("\\[[a-zA-Z]+]");
    // private static final Pattern doublePattern = Pattern.compile("-?\\d+(\\.\\d+)?");

    public static void main(String[] args) {

        if (args.length == 1) {

            // normal execution mode

            if (args[0].contains(Constants.TARGET_EXTENSION)) {

                try {
                    FileInputStream inputStream = new FileInputStream(args[0]);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    int lineNumber = 0;
                    int section = Constants.SECTION_UNDEFINED;

                    while ((line = reader.readLine()) != null) {

                        line = line.trim();
                        ++lineNumber;

                        // check section
                        if (isSection(line)) {
                            section = checkSection(line, section);
                            if (section == Constants.SECTION_UNDEFINED) {
                                exitWithErrorMessage(lineNumber, Constants.MESSAGE_WRONG_SYNTAX);
                                break;
                            }
                        } else {

                            // check which section is active
                            if (section == Constants.SECTION_CONST) {
                                int response = sectionConst(line);
                                if (response == Constants.EXIT_CODE_WRONG_SYNTAX)
                                    exitWithErrorMessage(lineNumber, Constants.MESSAGE_WRONG_SYNTAX);
                                else if (response == Constants.EXIT_CODE_UNKNOWN_DATA_TYPE)
                                    exitWithErrorMessage(lineNumber, Constants.MESSAGE_UNKNOWN_DATA_TYPE);
                                else if (response == Constants.EXIT_CODE_WRONG_NUMBER_FORMAT)
                                    exitWithErrorMessage(lineNumber, Constants.MESSAGE_WRONG_NUMBER_FORMAT);

                            } else if (section == Constants.SECTION_DEF) {
                                int response = sectionDefine(line);
                                if (response == Constants.EXIT_CODE_WRONG_SYNTAX)
                                    exitWithErrorMessage(lineNumber, Constants.MESSAGE_WRONG_SYNTAX);
                                else if (response == Constants.EXIT_CODE_UNKNOWN_DATA_TYPE)
                                    exitWithErrorMessage(lineNumber, Constants.MESSAGE_UNKNOWN_DATA_TYPE);

                            } else if (section == Constants.SECTION_CODE) {

                                String instructionText = getInstructionText(line).toLowerCase();
                                if (Instruction.checkInstruction(instructionText)) {
                                    if (instructionText.equals(InstructionType.INSTRUCTION_ADD)) {
                                        instructionAdd(line, lineNumber);
                                    } else if (instructionText.equals(InstructionType.INSTRUCTION_SUB)) {

                                    } else if (instructionText.equals(InstructionType.INSTRUCTION_MUL)) {

                                    } else if (instructionText.equals(InstructionType.INSTRUCTION_DIV)) {

                                    } else if (instructionText.equals(InstructionType.INSTRUCTION_MOD)) {

                                    } else if (instructionText.equals(InstructionType.INSTRUCTION_AND)) {

                                    } else if (instructionText.equals(InstructionType.INSTRUCTION_OR)) {

                                    } else if (instructionText.equals(InstructionType.INSTRUCTION_NOT)) {

                                    } else if (instructionText.equals(InstructionType.INSTRUCTION_MOV)) {
                                        instructionMov(line, lineNumber);
                                    } else if (instructionText.equals(InstructionType.INSTRUCTION_CMP)) {

                                    } else if (instructionText.equals(InstructionType.INSTRUCTION_JE)) {

                                    } else if (instructionText.equals(InstructionType.INSTRUCTION_JNE)) {

                                    } else if (instructionText.equals(InstructionType.INSTRUCTION_JGE)) {

                                    } else if (instructionText.equals(InstructionType.INSTRUCTION_JG)) {

                                    } else if (instructionText.equals(InstructionType.INSTRUCTION_JLE)) {

                                    } else if (instructionText.equals(InstructionType.INSTRUCTION_JL)) {

                                    } else if (instructionText.equals(InstructionType.INSTRUCTION_INP)) {

                                    } else if (instructionText.equals(InstructionType.INSTRUCTION_OUT)) {

                                    } else {
                                        //
                                        System.out.println("No way!!!");
                                    }


                                } else
                                    exitWithErrorMessage(lineNumber, Constants.MESSAGE_UNKNOWN_INSTRUCTION);
                            }
                        }
                    }

                    reader.close();

                } catch (Exception exception) {
                    LOGGER.warning(exception.fillInStackTrace().toString());
                }

            } else
                System.out.println("Wrong file!");
        }



        //printMemory();
        printRegisters();
    }

    private static boolean isSection(String line) {
        return line.toLowerCase().equals(Constants.TEXT_CONST) ||
                line.toLowerCase().equals(Constants.TEXT_DEF) ||
                line.toLowerCase().equals(Constants.TEXT_CODE);
    }

    private static int checkSection(String line, int section) {
        if (line.toLowerCase().equals(Constants.TEXT_CONST)) return Constants.SECTION_CONST;
        if (line.toLowerCase().equals(Constants.TEXT_DEF)) return Constants.SECTION_DEF;
        if (line.toLowerCase().equals(Constants.TEXT_CODE)) return Constants.SECTION_CODE;
        return section;
    }

    private static int sectionConst(String line) {
        String[] data = line.split(" ");
        if (data.length == 3) {
            if (intPattern.matcher(data[2]).matches()) {
                putInMemory(
                        new Variable(
                                data[0],
                                ByteBuffer.allocate(8).putLong(Long.parseLong(data[2])).array()
                        )
                );

                return Constants.EXIT_CODE_OK;
            }
            else
                return Constants.EXIT_CODE_WRONG_NUMBER_FORMAT;
            /*switch (data[1]) {
                case Constants.CONST_DATA_TYPE_INT:
                    putInMemory(new Variable<>(data[0], Integer.parseInt(data[2])));
                    return Constants.EXIT_CODE_OK;
                case Constants.CONST_DATA_TYPE_LONG:
                    putInMemory(new Variable<>(data[0], Long.parseLong(data[2])));
                    return Constants.EXIT_CODE_OK;
                case Constants.CONST_DATA_TYPE_FLOAT:
                    putInMemory(new Variable<>(data[0], Float.parseFloat(data[2])));
                    return Constants.EXIT_CODE_OK;
                case Constants.CONST_DATA_TYPE_DOUBLE:
                    putInMemory(new Variable<>(data[0], Double.parseDouble(data[2])));
                    return Constants.EXIT_CODE_OK;
                case Constants.CONST_DATA_TYPE_CHAR:
                    putInMemory(new Variable<>(data[0], data[2].charAt(0)));
                    return Constants.EXIT_CODE_OK;
                default:
                    return Constants.EXIT_CODE_UNKNOWN_DATA_TYPE;
            }*/
        }
        return Constants.EXIT_CODE_WRONG_SYNTAX;
    }

    private static int sectionDefine(String line) {
        String[] data = line.split(" ");
        if (data.length == 2) {
            putInMemory(new Variable(data[0], ByteBuffer.allocate(8).putLong(0L).array()));
            /*switch (data[1]) {
                case Constants.DATA_TYPE_INT:
                    putInMemory(new Variable<Integer>(data[0], 0));
                    return Constants.EXIT_CODE_OK;
                case Constants.DATA_TYPE_LONG:
                    putInMemory(new Variable<Long>(data[0], 0L));
                    return Constants.EXIT_CODE_OK;
                case Constants.DATA_TYPE_FLOAT:
                    putInMemory(new Variable<Float>(data[0], 0.0F));
                    return Constants.EXIT_CODE_OK;
                case Constants.DATA_TYPE_DOUBLE:
                    putInMemory(new Variable<Double>(data[0], 0.0D));
                    return Constants.EXIT_CODE_OK;
                case Constants.DATA_TYPE_CHAR:
                    putInMemory(new Variable<Character>(data[0], ' '));
                    return Constants.EXIT_CODE_OK;
                default:
                    return Constants.EXIT_CODE_UNKNOWN_DATA_TYPE;
            }*/
        }
        return Constants.EXIT_CODE_WRONG_SYNTAX;
    }

    private static String getInstructionText(String line) {
        return line.split(" ")[0];
    }

    private static boolean checkRegister(String register) {
        return Register.registers.get(register) != null;
    }

    // INSTRUCTION FUNCTIONS
    private static void instructionAdd(String line, int lineNumber) {
        String[] data =
                line.replace(" ", ",").replace(",,", ",").split(",");

        if (data.length == 3) {
            if (checkRegister(data[1])) {
                if (intPattern.matcher(data[2]).matches()) {

                }
            } else
                exitWithErrorMessage(lineNumber, Constants.MESSAGE_UNKNOWN_REGISTER);
        }
    }

    private static void instructionMov(String line, int lineNumber) {
        String[] data =
                line.replace(" ", ",").replace(",,", ",").split(",");

        if (data.length == 3)
            if (checkRegister(data[1])) {
                if (intPattern.matcher(data[2]).find())
                    Register.registers
                            .get(data[1])
                            .setValue(
                                    Long.parseLong(data[2]),
                                    Constants.TYPE_NUMBER
                            );
                else if (bracketsPattern.matcher(data[2]).find())
                    Register.registers
                            .get(data[1])
                            .setValue(
                                    memoryName.get(data[2].substring(1, data[2].length() - 1))
                                            .getValue(),
                                    Constants.TYPE_NUMBER
                            );
                else if (memoryName.keySet().stream().anyMatch(var -> var.equals(data[2])))
                    Register.registers
                            .get(data[1])
                            .setValue(
                                    memoryName.get(data[2]).getAddress(),
                                    Constants.TYPE_ADDRESS
                            );
                else if (Register.registers.keySet().stream().anyMatch(name -> name.equals(data[2]))) {
                    Register sourceReg = Register.registers.get(data[2]);
                    Register.registers
                            .get(data[1])
                            .setValue(
                                    sourceReg.getValue(),
                                    sourceReg.getValueType()
                            );
                }

            } else
                    exitWithErrorMessage(lineNumber, Constants.MESSAGE_UNKNOWN_REGISTER);
        else
            exitWithErrorMessage(lineNumber, Constants.MESSAGE_WRONG_SYNTAX);
    }




    private static void printMemory() {
        memoryAddress.values().forEach(val -> System.out.println(val.toString()));
    }

    private static void printRegisters() {
        /*Register rax = Register.registers.get("rax");
        for (byte b : rax.getBits()) {
            System.out.format("0x%02x  ", b);
        }*/

        for (Register r : Register.registers.values()) {
            System.out.print(r.getName() + ": 0x");
            for (byte b : r.getBytes())
                System.out.format("%02x", b);
            System.out.println();
        }
    }

    private static void exitWithErrorMessage(int line, String message) {
        System.out.println("Line " + line + ": " + message);
    }

    private static void putInMemory(Variable v) {
        memoryAddress.put(v.getAddress(), v);
        memoryName.put(v.getName(), v);
    }
}
