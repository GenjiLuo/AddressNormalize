package com.jhzhang.address.normalizer.prob.task;

import com.jhzhang.address.normalizer.cache.AddressQuery;
import com.jhzhang.address.normalizer.cache.IQuery;
import com.jhzhang.address.normalizer.common.Vector;
import com.jhzhang.address.normalizer.prob.complete.NormalizeAddressToken;
import com.jhzhang.address.normalizer.prob.structure.AddressToken;
import com.jhzhang.address.normalizer.cache.AddressQuery;
import com.jhzhang.address.normalizer.cache.IQuery;
import com.jhzhang.address.normalizer.prob.complete.NormalizeAddressToken;
import com.jhzhang.address.normalizer.prob.structure.AddressToken;

import java.util.List;

public interface MountTask {
    IQuery query = AddressQuery.getInstance();

    /**
     * 每个处理逻辑上需要挂载处理的逻辑
     *
     * @param tokens
     */
    void task(NormalizeAddressToken levelsTokens, List<AddressToken> tokens);
}
