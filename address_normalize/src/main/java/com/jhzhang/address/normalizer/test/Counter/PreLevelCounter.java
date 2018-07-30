package com.jhzhang.address.normalizer.test.Counter;

import com.jhzhang.address.normalizer.common.Element;
import com.jhzhang.address.normalizer.common.VSM;

import java.util.List;

/**
 * 记录各个地址等级的个数
 */
public enum PreLevelCounter {
    Province, // ("省", "自治区", "直辖市")
    City, // ("市", "自治州", "州")
    County, // ("区", "新区", "自治区", "县", "自治县")
    Town, // ("乡", "镇", "街道", "街道办")
    Village, // ("村", "屯", "新村", "社区")
    Road, // ("路", "街", "巷", "大街", "大道", "公路", "高速公路", "国道")
    RoadNo, // ("号")
    Poi, // ("小区", "大厦", "广场", "大楼", "公司", "饭店", "公寓","酒店", "工业区", "工业园", "科技园", "开发区", "创业园", "产业园")
    //I, // ("区", "座", "苑", "坊")
    BuildingNo, // ("栋", "幢")
    BuildingUnit, // ("单元")
    FloorNo, // ("楼", "层")
    RoomNo, //("室", "舍", "房")
    RUBBISH,
    UNKNOWN;


    MutableInteger val;

    PreLevelCounter() {
        this.val = new MutableInteger(0);
    }

    /**
     * 从一个地址列表中，统计其中不为空的片段
     *
     * @param lists
     */
    public static final void valueOf(List<Element> lists) {
        int index;
        boolean[] values = new boolean[values().length];
        int curIndex = 0;
        int ordinal;
        Element element = null;
        while (curIndex != -1) {
            element = lists.get(curIndex);
            ordinal = element.getLevel().ordinal();
            values[ordinal] = true;
            if (curIndex >= lists.size() - 1) {
                break;
            }
            curIndex = VSM.getNextDiffElementIndex(curIndex, lists);
        }
        valueOf(values);
    }

    private static final void valueOf(boolean[] values) {
        for (int i = 0, len = values.length; i < len; i++) {
            if (values[i]) {
                values()[i].increase();
            }
        }
    }

    /**
     * 返回所有的统计字符串
     *
     * @return
     */
    public static String toAllString() {
        StringBuilder sb = new StringBuilder();
        for (PreLevelCounter levelCounter : values()) {
            sb.append(levelCounter.toString()).append(" ");
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
