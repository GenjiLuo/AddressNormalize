package com.jhzhang.address.normalizer.cache.exception;

/**
 * 定义未找到异常，打印需要的信息.
 */
@SuppressWarnings("serial")
public class NotFindAddressException extends Exception {
    public NotFindAddressException() {
    }

    public NotFindAddressException(String reminder) {
        super(reminder);
    }
}
