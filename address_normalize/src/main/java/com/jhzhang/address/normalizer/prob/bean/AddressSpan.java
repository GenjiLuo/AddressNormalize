package com.jhzhang.address.normalizer.prob.bean;


/**
 * 地址片段
 *
 * @author jhzhang
 */
public class AddressSpan {
    public int length;
    public AddressType type;

    public AddressSpan(int l, AddressType t) {
        length = l;
        type = t;
    }

    @Override
    public String toString() {
        return type + ":" + length;
    }
}
