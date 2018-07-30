package com.jhzhang.address.normalizer.core.process;

import com.jhzhang.address.normalizer.common.Element;
import com.jhzhang.address.normalizer.common.Level;
import com.jhzhang.address.normalizer.common.Vector;
import com.jhzhang.address.normalizer.core.CommonOperation;
import com.jhzhang.address.normalizer.core.support.Position;
import com.jhzhang.address.normalizer.core.support.Position;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 等级L的处理流程.
 * Created by Administrator on 2016/9/13.
 */
public class LevelLProcessor implements Processor {
    /**
     * 该类要处理的等级.
     */
    private static final Level LEVEL = Level.L;


    //修改正则，添加：零壹贰叁肆伍陆柒捌玖拾一二三四五六七八九十
    /**
     * 数字的正则.
     */
    private static final Pattern PATTERN_FOR_L =
            Pattern.compile("([0-9零壹贰叁肆伍陆柒捌玖拾一二三四五六七八九十]+[-][0-9零壹贰叁肆伍陆柒捌玖拾一二三四五六七八九十]+)|([0-9零壹贰叁肆伍陆柒捌玖拾一二三四五六七八九十]+[a-zA-Z]*)|([a-zA-Z]+[0-9零壹贰叁肆伍陆柒捌玖拾一二三四五六七八九十]*)");
    /**
     * 匹配以数字结尾的正则.
     */
    private static final Pattern END_BY_PATTERN =
            Pattern.compile(".*(([0-9零壹贰叁肆伍陆柒捌玖拾一二三四五六七八九十]+[-][0-9零壹贰叁肆伍陆柒捌玖拾一二三四五六七八九十]+)|([0-9零壹贰叁肆伍陆柒捌玖拾一二三四五六七八九十]+[a-zA-Z]*)|([a-zA-Z]+[0-9零壹贰叁肆伍陆柒捌玖拾一二三四五六七八九十]*))$");


    @Override
    public Vector process(Vector vector) {
        CommonOperation.mergeByConstraint(vector, LEVEL, END_BY_PATTERN, PATTERN_FOR_L);
        fix(vector);
        CommonOperation.moveToTagLevel(vector, LEVEL);
        return vector;
    }

    /**
     * 修正地址向量.
     *
     * @param vector 要修正的地址向量
     */
    @SuppressWarnings("static-access")
    private void fix(Vector vector) {
        List<Element> list = vector.indexOf(LEVEL.ordinal());
        if (list.size() < 0) {
            return;
        }
        for (int i = list.size() - 1; i >= 0; i--) {
            Element suffixElement = list.get(i);

            if (suffixElement.getLevel().equals(LEVEL)) {
                Pattern pattern = Pattern.compile(".*([a-zA-Z]+[0-9]*)$");
                //如果名称是以字母结尾，则说明她属于等级J（"栋"）则她携带她前面所有的元素回到等级J
                if (pattern.matcher(suffixElement.getName()).matches()) {
                    vector.removeElement(new Position(LEVEL.ordinal(), i));
                    suffixElement = new Element(suffixElement.getName(),
                            suffixElement.getSuffix(),
                            Level.J,
                            suffixElement.getStart(), suffixElement.getEnd());
                    vector.mount(Level.J.ordinal(), suffixElement);
                    if (i == 0) {
                        return;
                    }
                    for (int j = i - 1; j >= 0; j--) {
                        Element preElement = list.get(j);
                        vector.removeElement(new Position(LEVEL.ordinal(), j));
                        vector.mount(Level.J.ordinal(), preElement);
                    }
                    //调用J的处理过程
                    CommonOperation.moveToTagLevel(vector, Level.J);
                }
            }
        }

    }
}
