package constants;

public interface Constants {

    String TEXT_DEF = ":def";
    String TEXT_CODE = ":code";
    String TEXT_CONST = ":const";

    String CONST_BYTE = "db";
    String CONST_WORD = "dw";
    String CONST_D_WORD = "dd";
    String CONST_Q_WORD = "dq";

    String RES_BYTE = "resb";
    String RES_WORD = "resw";
    String RES_D_WORD = "resd";
    String RES_Q_WORD = "resq";

    int SIZE_BYTE = 1;
    int SIZE_WORD = 2;
    int SIZE_D_WORD = 4;
    int SIZE_Q_WORD = 8;

    int TYPE_NUMBER = 0x8893;
    int TYPE_ADDRESS = 0x8894;

    String TARGET_EXTENSION = ".arh";
    String DEBUG_MODE_ARG = "-d";

    int SECTION_UNDEFINED = 0x00aa;
    int SECTION_CONST = 0x4534;
    int SECTION_DEF = 0x6799;
    int SECTION_CODE = 0xacaa;

    int EXIT_CODE_OK = 0x0000;
    int EXIT_CODE_WRONG_SYNTAX = 0x0001;
    int EXIT_CODE_UNKNOWN_DATA_TYPE = 0x0002;
    int EXIT_CODE_WRONG_NUMBER_FORMAT = 0x0003;


}
