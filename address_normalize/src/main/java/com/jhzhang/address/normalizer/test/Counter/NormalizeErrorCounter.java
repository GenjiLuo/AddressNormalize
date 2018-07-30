package com.jhzhang.address.normalizer.test.Counter;

/**
 * @author jhZhang
 * @date 2018/5/4
 */
public enum NormalizeErrorCounter {
    /**
     * 没有行政单元
     */
    Error01,
    /**
     * 行政单元得分较低
     */
    Error02,
    /**
     * 存在相同得分的地址片段
     */
    Error03,
    /**
     * 未知InvalidAddressException
     */
    Error04,
    /**
     * 其它异常
     */
    Error05,
    /**
     * 未知InvalidAddressException
     */
    Defalult;

    MutableInteger val;

    NormalizeErrorCounter() {
        this.val = new MutableInteger(0);
    }

    /**
     * 返回所有的统计字符串
     *
     * @return
     */
    public static String toAllString() {
        StringBuilder sb = new StringBuilder();
        for (NormalizeErrorCounter errorCounter : values()) {
            sb.append(errorCounter.toString()).append(" ");
        }
        return sb.toString();
    }

    public void increase() {
        this.val.increase();
    }

    @Override
    public String toString() {
        return this.name() + ":" + val.getVal();
    }
}
