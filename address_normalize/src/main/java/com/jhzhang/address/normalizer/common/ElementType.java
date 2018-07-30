package com.jhzhang.address.normalizer.common;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jhZhang
 * @date 2018/4/20
 */
public enum ElementType {
    FULL, // 全词数据
    HALF, // 半词数据
    SUFFIX, // 后缀数据
    UNKNOW; // 未知数据

    public static ElementType valueOf(Element element) {
        int len = element.getEnd() - element.getStart();
        if (element.getLevel().equals(Level.UNKNOWN)) {
            return ElementType.UNKNOW;
        } else if (!element.getName().isEmpty() && !element.getSuffix().isEmpty()
                && len == element.getName().length() + element.getSuffix().length()) {
            return ElementType.FULL;
        } else if (!element.getName().isEmpty() && len == element.getName().length()) {
            return ElementType.HALF;
        } else if (!element.getSuffix().isEmpty() && len == element.getSuffix().length()) {
            return ElementType.SUFFIX;
        } else {
            return ElementType.UNKNOW;
        }
    }

    public List<String> getRelation() {
        ArrayList<String> lists = new ArrayList<>();
        lists.add("A  FULL");
        lists.add("A  HALF");
        lists.add("B  FULL");
        lists.add("B  HALF");
        lists.add("C  FULL");
        lists.add("C  HALF");
        lists.add("D  FULL");
        lists.add("D  HALF");
        lists.add("E  FULL");
        lists.add("E  HALF+SUFFIX");
        lists.add("E  UNKNOW+SUFFIX");
        lists.add("F  UNKNOW");
        lists.add("F  UNKNOW+SUFFIX");
        lists.add("G  UNKNOW+SUFFIX");
        lists.add("H  UNKNOW");
        lists.add("H  UNKNOW+SUFFIX");
        lists.add("H  HALF+UNKNOW+SUFFIX");
//        lists.add("I  HALF+UNKNOW+SUFFIX");
        lists.add("J  UNKNOW");
        lists.add("J  UNKNOW+SUFFIX");
        lists.add("K  UNKNOW");
        lists.add("K  UNKNOW+SUFFIX");
        lists.add("L  UNKNOW");
        lists.add("L  UNKNOW+SUFFIX");
        lists.add("M  UNKNOW");
        lists.add("M  UNKNOW+SUFFIX");
        return lists;
    }

}
