package com.jhzhang.address.normalizer.test.Counter;

/**
 * @author jhZhang
 * @date 2018/5/4
 */
public class MutableInteger implements Counter {
    private long val;

    public MutableInteger(long val) {
        this.val = val;
    }

    public long getVal() {
        return val;
    }

    public void setVal(long val) {
        this.val = val;
    }

    @Override
    public void increase() {
        this.val = this.val + 1;
    }

    @Override
    public void increase(int val) {
        this.val = this.val + val;
    }
}
