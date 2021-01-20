package task;

import constants.Constants;
import constants.InstructionType;
import constants.Messages;
import model.Instruction;
import model.Register;
import model.Variable;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    private static final HashMap<Long, Variable> memoryAddress = new HashMap<>(); // variable by address (hash code)
    private static final HashMap<String, Variable> memoryName = new HashMap<>(); // variable by name
    private static final HashMap<String, Integer> labels = new HashMap<>(); // save label for jump instruction, map label on label line in code

    private static final Pattern numberPattern = Pattern.compile("-?\\d+");
    private static final Pattern bracketsPattern = Pattern.compile("\\[[a-zA-Z]+]");
    private static final Pattern labelPattern = Pattern.compile("\\w+:");
    // private static final Pattern doublePattern = Pattern.compile("-?\\d+(\\.\\d+)?");


    public static void main(String[] args) {

        if (args.length == 1) {

            // normal execution mode

            if (args[0].contains(Constants.TARGET_EXTENSION)) {

                try {
                    FileInputStream inputStream = new FileInputStream(args[0]);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                    int section = Constants.SECTION_UNDEFINED;

                    ArrayList<String> lines = (ArrayList<String>) reader.lines().collect(Collectors.toList());
                    reader.close();

                    for (int i = 0; i < lines.size(); ++i) {

                        String line = lines.get(i).trim();
                        System.out.println(line);

                        if (line.isEmpty())
                            continue;

                        // check section
                        if (isSection(line)) {
                            section = checkSection(line, section);
                            if (section == Constants.SECTION_UNDEFINED) {
                                exitWithErrorMessage(i + 1, Messages.MESSAGE_WRONG_SYNTAX);
                                break;
                            }
                        } else if (isLabel(line)) {
                            labels.put(line.replace(":", ""), i);
                        } else {

                            // check which section is active
                            if (section == Constants.SECTION_CONST) {
                                int response = sectionConst(line);
                                if (response == Constants.EXIT_CODE_WRONG_SYNTAX)
                                    exitWithErrorMessage(i + 1, Messages.MESSAGE_WRONG_SYNTAX);
                                else if (response == Constants.EXIT_CODE_UNKNOWN_DATA_TYPE)
                                    exitWithErrorMessage(i + 1, Messages.MESSAGE_UNKNOWN_DATA_TYPE);
                                else if (response == Constants.EXIT_CODE_WRONG_NUMBER_FORMAT)
                                    exitWithErrorMessage(i + 1, Messages.MESSAGE_WRONG_NUMBER_FORMAT);

                            } else if (section == Constants.SECTION_DEF) {
                                int response = sectionDefine(line);
                                if (response == Constants.EXIT_CODE_WRONG_SYNTAX)
                                    exitWithErrorMessage(i + 1, Messages.MESSAGE_WRONG_SYNTAX);
                                else if (response == Constants.EXIT_CODE_UNKNOWN_DATA_TYPE)
                                    exitWithErrorMessage(i + 1, Messages.MESSAGE_UNKNOWN_DATA_TYPE);

                            } else if (section == Constants.SECTION_CODE) {
                                String instructionText = getInstructionText(line).toLowerCase();
                                if (Instruction.checkInstruction(instructionText)) {
                                    if (instructionText.equals(InstructionType.INSTRUCTION_ADD)) {
                                        instructionAdd(line, i + 1);
                                    } else if (instructionText.equals(InstructionType.INSTRUCTION_SUB)) {
                                        instructionSub(line, i + 1);
                                    } else if (instructionText.equals(InstructionType.INSTRUCTION_MUL)) {
                                        instructionMul(line, i + 1);
                                    } else if (instructionText.equals(InstructionType.INSTRUCTION_DIV)) {
                                        instructionDiv(line, i + 1);
                                    } else if (instructionText.equals(InstructionType.INSTRUCTION_AND)) {
                                        instructionAnd(line, i + 1);
                                    } else if (instructionText.equals(InstructionType.INSTRUCTION_OR)) {
                                        instructionOr(line, i + 1);
                                    } else if (instructionText.equals(InstructionType.INSTRUCTION_NOT)) {
                                        instructionNot(line, i + 1);
                                    } else if (instructionText.equals(InstructionType.INSTRUCTION_MOV)) {
                                        instructionMov(line, i + 1);
                                    } else if (instructionText.equals(InstructionType.INSTRUCTION_SFL)) {
                                        instructionSfl(line, i + 1);
                                    } else if (instructionText.equals(InstructionType.INSTRUCTION_SFR)) {
                                        instructionSfr(line, i + 1);
                                    } else if (instructionText.equals(InstructionType.INSTRUCTION_CMP)) {
                                        instructionCmp(line, i + 1);
                                    } else if (instructionText.equals(InstructionType.INSTRUCTION_JE)) {
                                        i = instructionJe(line, i + 1);
                                        System.out.println(i);
                                    } else if (instructionText.equals(InstructionType.INSTRUCTION_JNE)) {
                                        i = instructionJne(line, i + 1);
                                    } else if (instructionText.equals(InstructionType.INSTRUCTION_JGE)) {
                                        i = instructionJge(line, i + 1);
                                    } else if (instructionText.equals(InstructionType.INSTRUCTION_JG)) {
                                        i = instructionJg(line, i + 1);
                                    } else if (instructionText.equals(InstructionType.INSTRUCTION_JLE)) {
                                        i = instructionJle(line, i + 1);
                                    } else if (instructionText.equals(InstructionType.INSTRUCTION_JL)) {
                                        i = instructionJl(line, i + 1);
                                    } else if (instructionText.equals(InstructionType.INSTRUCTION_INP)) {
                                        instructionInp(line, i + 1);
                                    } else if (instructionText.equals(InstructionType.INSTRUCTION_OUT)) {
                                        instructionOut(line, i + 1);
                                    } else {
                                        //
                                        System.out.println("No way!!!");
                                    }


                                } else
                                    exitWithErrorMessage(i + 1, Messages.MESSAGE_UNKNOWN_INSTRUCTION);
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



        printMemory();
        printRegisters();
        long l = Register.registers.get("rax").getValue();
        System.out.println("RAX: " + l);
    }

    private static boolean isSection(String line) {
        return line.toLowerCase().equals(Constants.TEXT_CONST) ||
                line.toLowerCase().equals(Constants.TEXT_DEF) ||
                line.toLowerCase().equals(Constants.TEXT_CODE);
    }

    private static boolean isLabel(String line) {
        return labelPattern.matcher(line).matches();
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
            if (numberPattern.matcher(data[2]).matches()) {
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
            return Constants.EXIT_CODE_OK;
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

    private static boolean checkVariable(String name) {
        return memoryName.get(name) != null;
    }

    // INSTRUCTION FUNCTIONS
    private static void instructionAdd(String line, int lineNumber) {
        String[] data =
                line.replace(" ", ",").replace(",,", ",").split(",");

        if (data.length == 3) {
            if (checkRegister(data[1])) {
                long val1 = Register.registers.get(data[1]).getValue();
                long val2;
                if (numberPattern.matcher(data[2]).matches())
                    val2 = Long.parseLong(data[2]);
                else if (checkRegister(data[2]))
                    val2 = Register.registers.get(data[2]).getValue();
                else {
                    exitWithErrorMessage(lineNumber, Messages.MESSAGE_WRONG_SYNTAX);
                    return;
                }
                long result = val1 + val2;
                Register.registers.get(data[1]).setValue(result, Constants.TYPE_NUMBER);
            } else
                exitWithErrorMessage(lineNumber, Messages.MESSAGE_UNKNOWN_REGISTER);
        } else
            exitWithErrorMessage(lineNumber, Messages.MESSAGE_WRONG_SYNTAX);
    }

    private static void instructionSub(String line, int lineNumber) {
        String[] data =
                line.replace(" ", ",").replace(",,", ",").split(",");

        if (data.length == 3) {
            if (checkRegister(data[1])) {
                long val1 = Register.registers.get(data[1]).getValue();
                long val2;
                if (numberPattern.matcher(data[2]).matches())
                    val2 = Long.parseLong(data[2]);
                else if (checkRegister(data[2]))
                    val2 = Register.registers.get(data[2]).getValue();
                else {
                    exitWithErrorMessage(lineNumber, Messages.MESSAGE_WRONG_SYNTAX);
                    return;
                }
                long result = val1 - val2;
                Register.registers.get(data[1]).setValue(result, Constants.TYPE_NUMBER);
            } else
                exitWithErrorMessage(lineNumber, Messages.MESSAGE_UNKNOWN_REGISTER);
        } else
            exitWithErrorMessage(lineNumber, Messages.MESSAGE_WRONG_SYNTAX);
    }

    private static void instructionMul(String line, int lineNumber) {
        String[] data =
                line.replace(" ", ",").replace(",,", ",").split(",");

        if (data.length == 3) {
            if (checkRegister(data[1])) {
                long val1 = Register.registers.get(data[1]).getValue();
                long val2;
                if (numberPattern.matcher(data[2]).matches())
                    val2 = Long.parseLong(data[2]);
                else if (checkRegister(data[2]))
                    val2 = Register.registers.get(data[2]).getValue();
                else {
                    exitWithErrorMessage(lineNumber, Messages.MESSAGE_WRONG_SYNTAX);
                    return;
                }
                long result = val1 * val2;
                Register.registers.get(data[1]).setValue(result, Constants.TYPE_NUMBER);
            } else
                exitWithErrorMessage(lineNumber, Messages.MESSAGE_UNKNOWN_REGISTER);
        } else
            exitWithErrorMessage(lineNumber, Messages.MESSAGE_WRONG_SYNTAX);
    }

    private static void instructionDiv(String line, int lineNumber) {
        String[] data =
                line.replace(" ", ",").replace(",,", ",").split(",");

        if (data.length == 3) {
            if (checkRegister(data[1])) {
                long val1 = Register.registers.get(data[1]).getValue();
                long val2;
                if (numberPattern.matcher(data[2]).matches())
                    val2 = Long.parseLong(data[2]);
                else if (checkRegister(data[2]))
                    val2 = Register.registers.get(data[2]).getValue();
                else {
                    exitWithErrorMessage(lineNumber, Messages.MESSAGE_WRONG_SYNTAX);
                    return;
                }
                long result = val1 / val2;
                long mod = val1 % val2;
                Register.registers.get(Register.REGISTER_DRE).setValue(result, Constants.TYPE_NUMBER);
                Register.registers.get(Register.REGISTER_MOD).setValue(mod, Constants.TYPE_NUMBER);
            } else
                exitWithErrorMessage(lineNumber, Messages.MESSAGE_UNKNOWN_REGISTER);
        } else
            exitWithErrorMessage(lineNumber, Messages.MESSAGE_WRONG_SYNTAX);
    }

    private static void instructionAnd(String line, int lineNumber) {
        String[] data =
                line.replace(" ", ",").replace(",,", ",").split(",");

        if (data.length == 3) {
            if (checkRegister(data[1])) {
                long val1 = Register.registers.get(data[1]).getValue();
                long val2;
                if (numberPattern.matcher(data[2]).matches())
                    val2 = Long.parseLong(data[2]);
                else if (checkRegister(data[2]))
                    val2 = Register.registers.get(data[2]).getValue();
                else {
                    exitWithErrorMessage(lineNumber, Messages.MESSAGE_WRONG_SYNTAX);
                    return;
                }
                long result = val1 & val2;
                Register.registers.get(data[1]).setValue(result, Constants.TYPE_NUMBER);
            } else
                exitWithErrorMessage(lineNumber, Messages.MESSAGE_UNKNOWN_REGISTER);
        } else
            exitWithErrorMessage(lineNumber, Messages.MESSAGE_WRONG_SYNTAX);
    }

    private static void instructionOr(String line, int lineNumber) {
        String[] data =
                line.replace(" ", ",").replace(",,", ",").split(",");

        if (data.length == 3) {
            if (checkRegister(data[1])) {
                long val1 = Register.registers.get(data[1]).getValue();
                long val2;
                if (numberPattern.matcher(data[2]).matches())
                    val2 = Long.parseLong(data[2]);
                else if (checkRegister(data[2]))
                    val2 = Register.registers.get(data[2]).getValue();
                else {
                    exitWithErrorMessage(lineNumber, Messages.MESSAGE_WRONG_SYNTAX);
                    return;
                }
                long result = val1 | val2;
                Register.registers.get(data[1]).setValue(result, Constants.TYPE_NUMBER);
            } else
                exitWithErrorMessage(lineNumber, Messages.MESSAGE_UNKNOWN_REGISTER);
        } else
            exitWithErrorMessage(lineNumber, Messages.MESSAGE_WRONG_SYNTAX);
    }

    private static void instructionNot(String line, int lineNumber) {
        String[] data =
                line.replace(" ", ",").replace(",,", ",").split(",");

        if (data.length == 2) {
            if (checkRegister(data[1])) {
                long val = Register.registers.get(data[1]).getValue();
                long result = ~val;
                Register.registers.get(data[1]).setValue(result, Constants.TYPE_NUMBER);
            } else
                exitWithErrorMessage(lineNumber, Messages.MESSAGE_UNKNOWN_REGISTER);
        } else
            exitWithErrorMessage(lineNumber, Messages.MESSAGE_WRONG_SYNTAX);
    }

    private static void instructionSfl(String line, int lineNumber) {
        String[] data =
                line.replace(" ", ",").replace(",,", ",").split(",");

        if (data.length == 3) {
            if (checkRegister(data[1])) {
                long val1 = Register.registers.get(data[1]).getValue();
                long val2;
                if (numberPattern.matcher(data[2]).matches())
                    val2 = Long.parseLong(data[2]);
                else if (checkRegister(data[2]))
                    val2 = Register.registers.get(data[2]).getValue();
                else {
                    exitWithErrorMessage(lineNumber, Messages.MESSAGE_WRONG_SYNTAX);
                    return;
                }
                long result = val1 << val2;
                Register.registers.get(data[1]).setValue(result, Constants.TYPE_NUMBER);
            } else
                exitWithErrorMessage(lineNumber, Messages.MESSAGE_UNKNOWN_REGISTER);
        } else
            exitWithErrorMessage(lineNumber, Messages.MESSAGE_WRONG_SYNTAX);
    }

    private static void instructionSfr(String line, int lineNumber) {
        String[] data =
                line.replace(" ", ",").replace(",,", ",").split(",");

        if (data.length == 3) {
            if (checkRegister(data[1])) {
                long val1 = Register.registers.get(data[1]).getValue();
                long val2;
                if (numberPattern.matcher(data[2]).matches())
                    val2 = Long.parseLong(data[2]);
                else if (checkRegister(data[2]))
                    val2 = Register.registers.get(data[2]).getValue();
                else {
                    exitWithErrorMessage(lineNumber, Messages.MESSAGE_WRONG_SYNTAX);
                    return;
                }
                long result = val1 >> val2;
                Register.registers.get(data[1]).setValue(result, Constants.TYPE_NUMBER);
            } else
                exitWithErrorMessage(lineNumber, Messages.MESSAGE_UNKNOWN_REGISTER);
        } else
            exitWithErrorMessage(lineNumber, Messages.MESSAGE_WRONG_SYNTAX);
    }

    private static void instructionMov(String line, int lineNumber) {
        String[] data =
                line.replace(" ", ",").replace(",,", ",").split(",");

        if (data.length == 3)
            if (checkRegister(data[1])) {
                if (numberPattern.matcher(data[2]).find())
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
                } else
                    exitWithErrorMessage(lineNumber, Messages.MESSAGE_WRONG_SYNTAX);

            } else if (checkVariable(data[1])) {
                if (checkRegister(data[2])) {
                        memoryName.get(data[1]).setValue(Register.registers.get(data[2]).getBytes());
                } else
                    exitWithErrorMessage(lineNumber, Messages.MESSAGE_WRONG_SYNTAX);

            } else
                    exitWithErrorMessage(lineNumber, Messages.MESSAGE_UNKNOWN_REGISTER);
        else
            exitWithErrorMessage(lineNumber, Messages.MESSAGE_WRONG_SYNTAX);
    }

    private static void instructionCmp(String line, int lineNumber) {
        String[] data =
                line.replace(" ", ",").replace(",,", ",").split(",");

        if (data.length == 3) {
            if (checkRegister(data[1])) {
                long val1 = Register.registers.get(data[1]).getValue();
                long val2;
                if (numberPattern.matcher(data[2]).matches())
                    val2 = Long.parseLong(data[2]);
                else if (checkRegister(data[2]))
                    val2 = Register.registers.get(data[2]).getValue();
                else {
                    exitWithErrorMessage(lineNumber, Messages.MESSAGE_WRONG_SYNTAX);
                    return;
                }
                long result = Long.compare(val1, val2);
                Register.registers.get(Register.REGISTER_FLG).setValue(result, Constants.TYPE_NUMBER);
            } else
                exitWithErrorMessage(lineNumber, Messages.MESSAGE_UNKNOWN_REGISTER);
        } else
            exitWithErrorMessage(lineNumber, Messages.MESSAGE_WRONG_SYNTAX);
    }

    private static int instructionJe(String line, int lineNumber) {
        String[] data =
                line.replace(" ", ",").replace(",,", ",").split(",");

        if (data.length == 2) {
            if (labels.containsKey(data[1])) {
                long val = Register.registers.get(Register.REGISTER_FLG).getValue();
                if (val == 0)
                    return labels.get(data[1]);
            } else
                exitWithErrorMessage(lineNumber, Messages.MESSAGE_UNKNOWN_LABEL);
        } else
            exitWithErrorMessage(lineNumber, Messages.MESSAGE_WRONG_SYNTAX);

        return lineNumber - 1;
    }

    private static int instructionJne(String line, int lineNumber) {
        String[] data =
                line.replace(" ", ",").replace(",,", ",").split(",");

        if (data.length == 2) {
            if (labels.containsKey(data[1])) {
                long val = Register.registers.get(Register.REGISTER_FLG).getValue();
                if (val != 0)
                    return labels.get(data[1]);
            } else
                exitWithErrorMessage(lineNumber, Messages.MESSAGE_UNKNOWN_LABEL);
        } else
            exitWithErrorMessage(lineNumber, Messages.MESSAGE_WRONG_SYNTAX);

        return lineNumber - 1;
    }

    private static int instructionJge(String line, int lineNumber) {
        String[] data =
                line.replace(" ", ",").replace(",,", ",").split(",");

        if (data.length == 2) {
            if (labels.containsKey(data[1])) {
                long val = Register.registers.get(Register.REGISTER_FLG).getValue();
                if (val >= 0)
                    return labels.get(data[1]);
            } else
                exitWithErrorMessage(lineNumber, Messages.MESSAGE_UNKNOWN_LABEL);
        } else
            exitWithErrorMessage(lineNumber, Messages.MESSAGE_WRONG_SYNTAX);

        return lineNumber - 1;
    }

    private static int instructionJg(String line, int lineNumber) {
        String[] data =
                line.replace(" ", ",").replace(",,", ",").split(",");

        if (data.length == 2) {
            if (labels.containsKey(data[1])) {
                long val = Register.registers.get(Register.REGISTER_FLG).getValue();
                if (val > 0)
                    return labels.get(data[1]);
            } else
                exitWithErrorMessage(lineNumber, Messages.MESSAGE_UNKNOWN_LABEL);
        } else
            exitWithErrorMessage(lineNumber, Messages.MESSAGE_WRONG_SYNTAX);

        return lineNumber - 1;
    }

    private static int instructionJle(String line, int lineNumber) {
        String[] data =
                line.replace(" ", ",").replace(",,", ",").split(",");

        if (data.length == 2) {
            if (labels.containsKey(data[1])) {
                long val = Register.registers.get(Register.REGISTER_FLG).getValue();
                if (val <= 0)
                    return labels.get(data[1]);
            } else
                exitWithErrorMessage(lineNumber, Messages.MESSAGE_UNKNOWN_LABEL);
        } else
            exitWithErrorMessage(lineNumber, Messages.MESSAGE_WRONG_SYNTAX);

        return lineNumber - 1;
    }

    private static int instructionJl(String line, int lineNumber) {
        String[] data =
                line.replace(" ", ",").replace(",,", ",").split(",");

        if (data.length == 2) {
            if (labels.containsKey(data[1])) {
                long val = Register.registers.get(Register.REGISTER_FLG).getValue();
                if (val < 0)
                    return labels.get(data[1]);
            }
            else
                exitWithErrorMessage(lineNumber, Messages.MESSAGE_UNKNOWN_LABEL);
        } else
            exitWithErrorMessage(lineNumber, Messages.MESSAGE_WRONG_SYNTAX);

        return lineNumber - 1;
    }

    private static void instructionInp(String line, int lineNumber) {

    }

    private static void instructionOut(String line, int lineNumber) {

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
        System.exit(-1);
    }

    private static void putInMemory(Variable v) {
        memoryAddress.put(v.getAddress(), v);
        memoryName.put(v.getName(), v);
    }
}
