package com.jhzhang.address.normalizer.prob.dictree;

import com.jhzhang.address.normalizer.prob.bean.AddTypes;

/**
 * 匹配命中的结果类
 *
 * @author jhZhang
 * @date 2018/6/6
 */
public class MatchRet {
    public int end;
    public AddTypes posInf;

    public MatchRet(int e, AddTypes d) {
        end = e;
        posInf = d;
    }

    @Override
    public String toString() {
        return "endPosition:" + end + " posInf:" + posInf;
    }
}