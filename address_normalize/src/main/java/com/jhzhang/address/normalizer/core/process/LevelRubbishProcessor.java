package com.jhzhang.address.normalizer.core.process;

import com.jhzhang.address.normalizer.common.Element;
import com.jhzhang.address.normalizer.common.Level;
import com.jhzhang.address.normalizer.common.Vector;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 等级M的处理流程 on 2016/9/13.
 */
public class LevelRubbishProcessor implements Processor {
    /**
     * 该类要处理的等级.
     */
    private static final Pattern PATTERN_FOR_M = Pattern.compile("[0-9零壹贰叁肆伍陆柒捌玖拾一二三四五六七八九十#-]{2,}[A-Z]?[号]*");

    @Override
    public Vector process(Vector vector) {
        List<Element> list = vector.indexOf(Level.RUBBISH.ordinal());
        if (list == null || list.isEmpty()) {
            return vector;
        }
        String room = "";
        int offset = -1;
        Matcher m = null;

        for (Element ele : list) {
            if (ele.getLevel().equals(Level.UNKNOWN)) {
                m = PATTERN_FOR_M.matcher(ele.toString());

                while (m.find()) {
                    if ((m.group().contains("#") || m.group().contains("-"))
                            && m.group().length() < 10 && m.group().length() > 3) {
                        room = m.group();
                    } else if (m.group().length() < 5) {
                        room = m.group();
                    }

                }
                ele.setName(ele.toString().replace(room, ""));
            }
        }

        Element element = new Element(room, "", Level.UNKNOWN, offset, -1);
        vector.mount(Level.M.ordinal(), element);
        return vector;
    }

}
