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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
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

        if (args.length > 0) {

            // normal execution mode
            // without debugging

            if (args[0].contains(Constants.TARGET_EXTENSION)) {

                boolean debuggingMode = false;
                int[] breakpoints = null;

                if (args.length > 1) {
                    if (args[1].equals(Constants.DEBUG_MODE_ARG)) {
                        debuggingMode = true;

                        if (args.length > 2) {
                            breakpoints = new int[args.length - 2];
                            for (int i = 2; i < args.length; ++i)
                                breakpoints[i - 2] = Integer.parseInt(args[i]);
                        }
                    }
                }

                try {
                    FileInputStream inputStream = new FileInputStream(args[0]);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                    int section = Constants.SECTION_UNDEFINED;

                    ArrayList<String> lines = (ArrayList<String>) reader.lines().collect(Collectors.toList());
                    reader.close();

                    // get all labels
                    for (int i = 0; i < lines.size(); ++i)
                        if (isLabel(lines.get(i).trim()))
                            labels.put(lines.get(i).trim().replace(":", ""), i);

                    for (int i = 0; i < lines.size(); ++i) {

                        String line = lines.get(i).trim();

                        if (line.isEmpty() || isLabel(line))
                            continue;

                        if (debuggingMode)
                            if (breakpoints == null)
                                trap(i + 1);
                            else {
                                final int lineNumber = i + 1;
                                if (Arrays.stream(breakpoints).filter(lineNum -> lineNum == lineNumber).findAny().orElse(-1) != -1)
                                    trap(i + 1);
                            }

                        // check section
                        if (isSection(line)) {
                            section = checkSection(line, section);
                            if (section == Constants.SECTION_UNDEFINED) {
                                exitWithErrorMessage(i + 1, Messages.MESSAGE_WRONG_SYNTAX);
                                break;
                            }
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
                                    } else if (instructionText.equals(InstructionType.INSTRUCTION_JMP)) {
                                        i = instructionJmp(line, i + 1);
                                    } else if (instructionText.equals(InstructionType.INSTRUCTION_JE)) {
                                        i = instructionJe(line, i + 1);
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
                                    }
                                } else
                                    exitWithErrorMessage(i + 1, Messages.MESSAGE_UNKNOWN_INSTRUCTION);
                            }
                        }
                    }
                } catch (Exception exception) {
                    LOGGER.warning(exception.fillInStackTrace().toString());
                }

            } else
                System.out.println("Wrong file!");
        }

        end();
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
            int size = getSize(data[1]);
            if (size != -1 && numberPattern.matcher(data[2]).matches()) {
                byte[] startArray = ByteBuffer.allocate(8).putLong(Long.parseLong(data[2])).array();
                byte[] array = new byte[size];
                System.arraycopy(startArray, startArray.length - size, array, 0, size);
                putInMemory(new Variable(data[0], size, array));
                return Constants.EXIT_CODE_OK;
            }
            else
                return Constants.EXIT_CODE_WRONG_NUMBER_FORMAT;
        }
        return Constants.EXIT_CODE_WRONG_SYNTAX;
    }

    private static int sectionDefine(String line) {
        String[] data = line.split(" ");
        if (data.length == 2) {
            int size = getSize(data[1]);
            if (size != -1) {
                putInMemory(new Variable(data[0], size, ByteBuffer.allocate(8).putLong(0L).array()));
                return Constants.EXIT_CODE_OK;
            }
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
                    int size = memoryName.get(data[1]).getSize();
                    byte[] bytes = Register.registers.get(data[2]).getBytes();
                    byte[] result = new byte[size];
                    System.arraycopy(bytes, bytes.length - size, result, 0, size);
                    /*for (int i = bytes.length - size; i < bytes.length; ++i)
                        result[i - size] = bytes[i];*/
                    memoryName.get(data[1]).setValue(result);
                } else
                    exitWithErrorMessage(lineNumber, Messages.MESSAGE_WRONG_SYNTAX);

            } else if (bracketsPattern.matcher(data[1]).find()) {
                if (checkRegister(data[1].substring(1 , data[1].length() - 1))) {
                    long address = Register.registers.get(data[1].substring(1 , data[1].length() - 1)).getValue();
                    if (numberPattern.matcher(data[2]).find())
                        memoryAddress.get(address).setValue(Long.parseLong(data[2]));
                    else if (bracketsPattern.matcher(data[2]).find())
                        memoryAddress.get(address)
                                .setValue(memoryName.get(data[2].substring(1, data[2].length() - 1)).getValue());
                    else if (memoryName.keySet().stream().anyMatch(var -> var.equals(data[2])))
                        memoryAddress.get(address).setValue(memoryName.get(data[2]).getAddress());
                    else if (Register.registers.keySet().stream().anyMatch(name -> name.equals(data[2])))
                        memoryAddress.get(address).setValue(Register.registers.get(data[2]).getBytes());
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
                // a value less than 0 if val1 < val2
                // a value greater than 0 if val1 > val2
                // 0 if val1 == val2
                Register.registers.get(Register.REGISTER_FLG).setValue(result, Constants.TYPE_NUMBER);
            } else
                exitWithErrorMessage(lineNumber, Messages.MESSAGE_UNKNOWN_REGISTER);
        } else
            exitWithErrorMessage(lineNumber, Messages.MESSAGE_WRONG_SYNTAX);
    }

    private static int instructionJmp(String line, int lineNumber) {
        String[] data =
                line.replace(" ", ",").replace(",,", ",").split(",");
        if (data.length == 2) {
            if (labels.containsKey(data[1]))
                return labels.get(data[1]);
            else
                exitWithErrorMessage(lineNumber, Messages.MESSAGE_UNKNOWN_LABEL);
        } else
            exitWithErrorMessage(lineNumber, Messages.MESSAGE_WRONG_SYNTAX);

        return lineNumber - 1;
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
        String[] data =
                line.replace(" ", ",").replace(",,", ",").split(",");

        if (data.length == 2) {
            if (checkRegister(data[1])) {
                Scanner scanner = new Scanner(System.in);
                long val = scanner.nextLong();
                Register.registers.get(data[1]).setValue(val, Constants.TYPE_NUMBER);
            } else
                exitWithErrorMessage(lineNumber, Messages.MESSAGE_UNKNOWN_REGISTER);
        } else
            exitWithErrorMessage(lineNumber, Messages.MESSAGE_WRONG_SYNTAX);
    }

    private static void instructionOut(String line, int lineNumber) {
        String[] data =
                line.replace(" ", ",").replace(",,", ",").split(",");

        if (data.length == 2) {
            if (checkRegister(data[1])) {
                Register r = Register.registers.get(data[1]);
                if (r.getValueType() == Constants.TYPE_NUMBER)
                    System.out.println(r.getValue());
                else if (r.getValueType() == Constants.TYPE_ADDRESS) {
                    System.out.print("0x");
                    for (byte b : r.getBytes())
                        System.out.format("%02x", b);
                    System.out.println();
                }
            } else
                exitWithErrorMessage(lineNumber, Messages.MESSAGE_UNKNOWN_REGISTER);
        } else
            exitWithErrorMessage(lineNumber, Messages.MESSAGE_WRONG_SYNTAX);
    }

    // END OF INSTRUCTIONS

    private static int getSize(String command) {
        if (command.toLowerCase().equals(Constants.CONST_BYTE) || command.toLowerCase().equals(Constants.RES_BYTE)) return Constants.SIZE_BYTE;
        if (command.toLowerCase().equals(Constants.CONST_WORD) || command.toLowerCase().equals(Constants.RES_WORD)) return Constants.SIZE_WORD;
        if (command.toLowerCase().equals(Constants.CONST_D_WORD) || command.toLowerCase().equals(Constants.RES_D_WORD)) return Constants.SIZE_D_WORD;
        if (command.toLowerCase().equals(Constants.CONST_Q_WORD) || command.toLowerCase().equals(Constants.RES_Q_WORD)) return Constants.SIZE_Q_WORD;
        return -1;
    }

    private static void printMemory() {
        System.out.println("Variables");
        System.out.format("%16s\t\t%18s\t%16s\n", "Name", "Address", "Value");
        memoryAddress.values().forEach(val -> System.out.println(val.toString()));
        System.out.println();
    }

    private static void printRegisters() {
        System.out.println("Registers");
        for (Register r : Register.registers.values()) {
            System.out.print(r.getName() + ": 0x");
            for (byte b : r.getBytes())
                System.out.format("%02x", b);
            System.out.println();
        }
        System.out.println();
    }

    private static void exitWithErrorMessage(int line, String message) {
        System.out.println("Line " + line + ": " + message);
        System.exit(-1);
    }

    private static void putInMemory(Variable v) {
        memoryAddress.put(v.getAddress(), v);
        memoryName.put(v.getName(), v);
    }

    private static void trap(int lineNumber) {
        System.out.println("DEBUGGING\nLine: " + lineNumber);
        printMemory();
        printRegisters();
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
    }

    private static void end() {
        System.out.println("END OF PROGRAM\n");
        printMemory();
        printRegisters();
    }
}
