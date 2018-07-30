package com.jhzhang.address.normalizer.core.process;

import com.jhzhang.address.normalizer.common.Element;
import com.jhzhang.address.normalizer.common.Level;
import com.jhzhang.address.normalizer.common.Vector;
import com.jhzhang.address.normalizer.core.CommonOperation;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 等级M的处理流程 on 2016/9/13.
 */
public class LevelMProcessor implements Processor {
    /**
     * 该类要处理的等级.
     */
    private static final Level LEVEL = Level.M;

    //修改正则
    private static final Pattern PATTERN_FOR_M = Pattern.compile("([0-9零壹贰叁肆伍陆柒捌玖拾一二三四五六七八九十#-]{2,}[A-Z]?)|([0-9零壹贰叁肆伍陆柒捌玖拾一二三四五六七八九十#-]{1}[A-Z]{1})");

    private static final Pattern END_BY_PATTERN = Pattern.compile(".*(([0-9零壹贰叁肆伍陆柒捌玖拾一二三四五六七八九十#-]{2,}[A-Z]?)|([0-9零壹贰叁肆伍陆柒捌玖拾一二三四五六七八九十#-]{1}[A-Z]{1}))$");

    @Override
    public Vector process(Vector vector) {
        CommonOperation.mergeByConstraint(vector, LEVEL, END_BY_PATTERN, PATTERN_FOR_M);
        CommonOperation.moveToTagLevel(vector, LEVEL);
        parseRoom(vector);
        return vector;
    }

    public void parseRoom(Vector vector) {

        List<Element> list = vector.indexOf(LEVEL.ordinal());
        if (list == null || list.isEmpty()) {
            return;
        }

        Element roomEle = list.get(0);
        String value = roomEle.getName();
        String rooms[] = null;
        int offset = -1;


        if ((value.contains("#") || value.contains("-"))
                && value.length() < 10 && value.length() > 3) {
            //移除该等级上元素
            vector.remove(LEVEL.ordinal());

            rooms = value.replaceAll("#", "-").split("-");

            if (rooms != null) {
                // TODO 没有完全的实现Element参数
                if (rooms.length == 3) {
                    mountByLevel(vector, Level.M, new Element(rooms[2], "室", Level.M, offset, -1), roomEle);
                    mountByLevel(vector, Level.K, new Element(rooms[1], "单元", Level.K, offset, -1), roomEle);
                    mountByLevel(vector, Level.J, new Element(rooms[0], "栋", Level.J, offset, -1), roomEle);
                }
                if (rooms.length == 2) {
                    mountByLevel(vector, Level.M, new Element(rooms[1], "室", Level.M, offset, -1), roomEle);
                    mountByLevel(vector, Level.J, new Element(rooms[0], "栋", Level.J, offset, -1), roomEle);
                }

            }

        }
    }

    /**
     * 解析类似1-2-802房间号，挂在到相应的等级下
     *
     * @param vector
     * @param level
     * @param element
     * @param currElement
     * @return
     */
    public boolean mountByLevel(Vector vector, Level level, Element element, Element currElement) {
        if (vector.indexOf(level.ordinal()).size() <= 0) {
            vector.mount(level.ordinal(), element);
            currElement.setName(currElement.toString().replace(element.getName() + "-", ""));
            return true;
        }
        return false;
    }

}
