package com.jhzhang.address.normalizer.segment;

import com.jhzhang.address.normalizer.common.Element;

import java.util.List;

/**
 * 分词接口
 */
public interface Segment {
    /**
     * 根据传入的地址数据返回相应的切分结果
     *
     * @param s 原始地址数据
     * @return 切分好的地名、等级、以及后缀等属性封装在一起的Element对象的列表
     */
    public List<Element> seg(String s);
}
