package com.jhzhang.address.normalizer.core.process;

import com.jhzhang.address.normalizer.common.Level;
import com.jhzhang.address.normalizer.common.Vector;
import com.jhzhang.address.normalizer.core.CommonOperation;


/**
 * 等级H的处理过程.
 * Created by Administrator on 2016/9/13.
 */
public class LevelHProcessor implements Processor {
    private static Level LEVEL = Level.H;

    public Vector process(Vector vector) {
        CommonOperation.mergeByKeywords(vector, LEVEL);
        return vector;
    }

}
