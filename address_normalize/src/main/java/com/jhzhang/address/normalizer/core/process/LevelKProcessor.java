package com.jhzhang.address.normalizer.core.process;

import com.jhzhang.address.normalizer.common.Level;
import com.jhzhang.address.normalizer.common.Vector;
import com.jhzhang.address.normalizer.core.CommonOperation;

import java.util.regex.Pattern;

/**
 * 等级K的处理流程 on 2016/9/13.
 */
public class LevelKProcessor implements Processor {
    /**
     * 该类要处理的等级.
     */
    private static final Level LEVEL = Level.K;


    //修改数字为[0-9A-Z零壹贰叁肆伍陆柒捌玖拾一二三四五六七八九十甲乙丙丁戊己庚辛壬癸-]{1,}
    /**
     * 正则.
     */
    private static final Pattern NUMBER = Pattern.compile("[0-9A-Z零壹贰叁肆伍陆柒捌玖拾一二三四五六七八九十甲乙丙丁戊己庚辛壬癸-]{1,}");
    /**
     * 匹配以数字结尾的正则.
     */
    private static final Pattern END_BY_NUMBER = Pattern.compile(".*[0-9A-Z零壹贰叁肆伍陆柒捌玖拾一二三四五六七八九十甲乙丙丁戊己庚辛壬癸-]{1,}$");


    @Override
    public Vector process(Vector vector) {
        CommonOperation.mergeByConstraint(vector, LEVEL, END_BY_NUMBER, NUMBER);
        CommonOperation.moveToTagLevel(vector, LEVEL);
        return vector;
    }
}
