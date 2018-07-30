package com.jhzhang.address.normalizer.core;

import com.jhzhang.address.normalizer.cache.AddressQuery;
import com.jhzhang.address.normalizer.common.Vector;
import com.jhzhang.address.normalizer.core.process.*;
import com.jhzhang.address.normalizer.cache.AddressQuery;
import com.jhzhang.address.normalizer.core.process.*;

/**
 * 地址处理器.
 * <p>负责实际的地址处理工作</p>
 *
 * @author johntse
 */
public final class AddressProcessor implements Processor {

    private AddressProcessor() {
    }

    public static AddressProcessor getInstance() {
        return AddressProcessorHolder.INSTANCE;
    }

    @Override
    public Vector process(Vector vector) throws Exception {
        new FiveLevelAheadCheck(AddressQuery.getInstance()).process(vector);
        new LevelEProcessor().process(vector);
        new LevelFProcessor().process(vector);
        new LevelGProcessor().process(vector);
        new LevelHProcessor().process(vector);
        new LevelJProcessor().process(vector);
        new LevelKProcessor().process(vector);
        new LevelLProcessor().process(vector);
        new LevelRubbishProcessor().process(vector);
        new LevelMProcessor().process(vector);
        new LastProcessor().process(vector);
        return vector;
    }

    private static final class AddressProcessorHolder {
        private static final AddressProcessor INSTANCE = new AddressProcessor();
    }
}
