package com.jhzhang.address.normalizer.cache.exception;

/**
 * Created by Administrator on 2016/10/13.
 * 地址反序列化为Vector异常类
 */
@SuppressWarnings("serial")
public class AddressParseException extends Exception {
    public AddressParseException() {
    }

    public AddressParseException(String message) {
        super(message);
    }
}
