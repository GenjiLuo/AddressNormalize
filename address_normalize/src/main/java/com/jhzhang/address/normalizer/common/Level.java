package com.jhzhang.address.normalizer.common;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * 地区等级定义.
 */
public enum Level {
    A, // ("省", "自治区", "直辖市")
    B, // ("市", "自治州", "州")
    C, // ("区", "新区", "自治区", "县", "自治县")
    D, // ("乡", "镇", "街道", "街道办")
    E, // ("村", "屯", "新村", "社区")
    F, // ("路", "街", "巷", "大街", "大道", "公路", "高速公路", "国道")
    G, // ("号")
    H, // ("小区", "大厦", "广场", "大楼", "公司", "饭店", "公寓","酒店", "工业区", "工业园", "科技园", "开发区", "创业园", "产业园")
    //I, // ("区", "座", "苑", "坊")
    J, // ("栋", "幢")
    K, // ("单元")
    L, // ("楼", "层")
    M, //("室", "舍", "房")
    RUBBISH,//垃圾
    UNKNOWN;

    /**
     * 存放某个类别下所有的等级关键词
     */
    private Set<String> keywords = new HashSet<>();

    public static int size() {
        return Level.values().length - 1;
    }
    /**
     * 设置单个地址元素关键字.
     *
     * @param keyword 单个地址元素关键字
     */
    public void addKeyword(String keyword) {
        if (keyword != null && !keyword.isEmpty()) {
            this.keywords.add(keyword);
        }
    }

    public Set<String> getKeywords() {
        return keywords;
    }

    /**
     * 批量设置地址元素关键字.
     *
     * @param keywords 地址元素关键字集合
     */
    public void setKeywords(Collection<String> keywords) {
        if (keywords != null && !keywords.isEmpty()) {
            this.keywords.addAll(keywords);
        }
    }
}