package constants;

public interface Constants {

    String TEXT_DEF = ":def";
    String TEXT_CODE = ":code";
    String TEXT_CONST = ":const";

    String DATA_TYPE_INT = "resi";
    String DATA_TYPE_LONG = "resl";
    String DATA_TYPE_FLOAT = "resf";
    String DATA_TYPE_DOUBLE = "resd";
    String DATA_TYPE_CHAR = "resc";

    String CONST_DATA_TYPE_INT = "ci";
    String CONST_DATA_TYPE_LONG = "cl";
    String CONST_DATA_TYPE_FLOAT = "cf";
    String CONST_DATA_TYPE_DOUBLE = "cd";
    String CONST_DATA_TYPE_CHAR = "cc";

    int TYPE_NUMBER = 0x8893;
    int TYPE_ADDRESS = 0x8894;

    String TARGET_EXTENSION = ".arh";

    int SECTION_UNDEFINED = 0x00aa;
    int SECTION_CONST = 0x4534;
    int SECTION_DEF = 0x6799;
    int SECTION_CODE = 0xacaa;

    int EXIT_CODE_OK = 0x0000;
    int EXIT_CODE_WRONG_SYNTAX = 0x0001;
    int EXIT_CODE_UNKNOWN_DATA_TYPE = 0x0002;
    int EXIT_CODE_WRONG_NUMBER_FORMAT = 0x0003;

    String MESSAGE_WRONG_SYNTAX = "Wrong syntax";
    String MESSAGE_WRONG_NUMBER_FORMAT = "Wrong number format";
    String MESSAGE_UNKNOWN_DATA_TYPE = "Unknown data type";
    String MESSAGE_UNKNOWN_INSTRUCTION = "Unknown instruction";
    String MESSAGE_UNKNOWN_REGISTER = "Unknown register";
}
