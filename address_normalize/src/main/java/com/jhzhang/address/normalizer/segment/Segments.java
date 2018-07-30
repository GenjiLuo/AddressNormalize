package com.jhzhang.address.normalizer.segment;

import com.jhzhang.address.normalizer.cache.AddressQuery;
import com.jhzhang.address.normalizer.cache.IQuery;
import com.jhzhang.address.normalizer.cache.AddressQuery;
import com.jhzhang.address.normalizer.cache.IQuery;

/**
 * 提供的两个分词器
 */
public final class Segments {
    /**
     * 获取最长切词类LongestSegment的对象
     *
     * @return 最长切词类LongestSegment的对象（以父接口类型的形式表现）
     */
    public static Segment getLongestSegment() {
        return new LongestSegment(AddressQuery.getInstance());
    }

    /**
     * 获取最长切词类LongestSegment的对象
     *
     * @param query 地址查询接口对象
     * @return 最长切词类LongestSegment的对象（以父接口类型的形式表现）
     */
    public static Segment getLongestSegment(IQuery query) {
        return new LongestSegment(query);
    }
}
