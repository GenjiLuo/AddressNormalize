package com.jhzhang.address.normalizer.prob.task;

import com.jhzhang.address.normalizer.common.Element;
import com.jhzhang.address.normalizer.common.Level;
import com.jhzhang.address.normalizer.common.Vector;
import com.jhzhang.address.normalizer.prob.bean.AddressType;
import com.jhzhang.address.normalizer.prob.complete.NormalizeAddressToken;
import com.jhzhang.address.normalizer.prob.structure.AddressToken;
import com.jhzhang.address.normalizer.prob.complete.NormalizeAddressToken;
import com.jhzhang.address.normalizer.prob.structure.AddressToken;

import java.util.List;

/**
 * POI等挂载
 *
 * @author jhZhang
 * @date 2018/6/11
 */
public class LevelHMountTask implements MountTask {
    final Level level = Level.H;

    @Override
    public void task(NormalizeAddressToken levelsTokens, List<AddressToken> tokens) {
        for (int i = 0, len = tokens.size(); i < len; i++) {
            AddressToken token = tokens.get(i);
            if (token == null) {
                continue;
            }
            if (token.type.equals(AddressType.LandMark)
                    || token.type.equals(AddressType.Village)) {
                levelsTokens.mount(token, level);
                tokens.set(i, null);
            }
        }
    }
}
