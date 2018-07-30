package com.jhzhang.address.normalizer.core.process;

import com.jhzhang.address.normalizer.common.Element;
import com.jhzhang.address.normalizer.common.Level;
import com.jhzhang.address.normalizer.common.Vector;
import com.jhzhang.address.normalizer.core.CommonOperation;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 等级J的处理流程.
 * Created by Administrator on 2016/9/13.
 */
public class LevelJProcessor implements Processor {

    /**
     * 该类要处理的等级.
     */
    private static final Level LEVEL = Level.J;


    //添加：|([0-9A-Z零壹贰叁肆伍陆柒捌玖拾一二三四五六七八九十甲乙丙丁戊己庚辛壬癸]{1,})
    /**
     * 前面是阿拉伯数字或字母或字母和数字的组合或数字与连字符的组合.
     */
    private static final Pattern PATTERN_FOR_J =
            Pattern.compile("([0-9]+[-][0-9]+)|([a-zA-Z]*[0-9]+)|([a-zA-Z]+)|([0-9A-Z零壹贰叁肆伍陆柒捌玖拾一二三四五六七八九十甲乙丙丁戊己庚辛壬癸]{1,})");
    /**
     * 以PATTERN_FOR_J结尾模式.
     */
    private static final Pattern END_BY_PATTERN =
            Pattern.compile(".*(([0-9]+[-][0-9]+)|([a-zA-Z]*[0-9]+)|([a-zA-Z]+)|([0-9A-Z零壹贰叁肆伍陆柒捌玖拾一二三四五六七八九十甲乙丙丁戊己庚辛壬癸]{1,}))$");


    @Override
    public Vector process(Vector vector) {
//        System.out.println("J=1 :"  + vector.toString());
        CommonOperation.mergeByConstraint(vector, LEVEL, END_BY_PATTERN, PATTERN_FOR_J);
//        System.out.println("J=2 :"  + vector.toString());
        CommonOperation.moveToTagLevel(vector, LEVEL);
//        System.out.println("J=3 :"  + vector.toString());
        normalize(vector);
//        System.out.println("J=4 :"  + vector.toString());
        return vector;
    }

    /**
     * 归一化 将“号楼“和“幢”归一化为“栋”.
     *
     * @param vector 要归一化的地址向量
     */
    private void normalize(Vector vector) {
        String normalizedSuffix = "栋";
        List<Element> list = vector.indexOf(LEVEL.ordinal());
        List<Element> normalizeList = new LinkedList<>();
        for (Element element : list) {
            element = new Element(element.getName(),
                    normalizedSuffix,
                    element.getLevel(),
                    element.getStart(), element.getEnd());
            normalizeList.add(element);
        }
        vector.setListOnIndex(normalizeList, LEVEL.ordinal());
    }
}
