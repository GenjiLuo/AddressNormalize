package com.jhzhang.address.normalizer.model.automaton;

/**
 * 状态机返回状态.
 *
 * @author johntse
 */
public enum StatusCode {
    ACCEPT("00"),
    MISSED("01"),
    UNTERMINATED("02"),
    UNKNOWN("99");

    private String code;

    StatusCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
