package com.jhzhang.address.normalizer.cache.exception;

/**
 * 定义无效的地址异常类
 * Created by Administrator on 2016/8/22.
 */
@SuppressWarnings("serial")
public class InvalidAddressException extends Exception {
    public InvalidAddressException() {
    }

    public InvalidAddressException(String message) {
        super(message);
    }
}
