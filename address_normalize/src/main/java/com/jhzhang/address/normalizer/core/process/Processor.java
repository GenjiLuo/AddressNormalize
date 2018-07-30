package com.jhzhang.address.normalizer.core.process;

import com.jhzhang.address.normalizer.common.Vector;

/**
 * 每个等级归一化的接口 on 2016/8/30.
 */
public interface Processor {
    /**
     * 每个等级在处理时要实现的接口.
     *
     * @param vector 经简单合并后的地址向量
     * @return 前4个行政等级定级后的地址向量
     * @throws Exception 可能抛出的异常
     */
    Vector process(Vector vector) throws Exception;
}
