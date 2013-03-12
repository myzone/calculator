package com.myzone.calculator.model;

/**
 * @author: myzone
 * @date: 19.02.13 19:01
 */
public enum Signal {
    DIGIT_0("0"),
    DIGIT_1("1"),
    DIGIT_2("2"),
    DIGIT_3("3"),
    DIGIT_4("4"),
    DIGIT_5("5"),
    DIGIT_6("6"),
    DIGIT_7("7"),
    DIGIT_8("8"),
    DIGIT_9("9"),

    EVALUATE("="),

    DOT("."),

    PLUS("+"),
    MINUS("-"),
    MULTIPLY("*"),
    DIVIDE("/"),

    PERCENT("%"),
    SQUARE_ROOT("√"),
    REVERSE("±"),

    MEMORY_CLEAR("MC"),
    MEMORY_RESTORE("MR"),
    MEMORY_STORE("MS"),
    MEMORY_PLUS("M+"),
    MEMORY_MINUS("M-"),

    BACK_SPACE("←"),
    CLEAR("C"),
    CLEAR_EVALUATION("CE");

    private String representation;

    private Signal(String representation) {
        this.representation = representation;
    }

    public String getRepresentation() {
        return representation;
    }
}
