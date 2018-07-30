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
 * 路、号等级别放在一起考虑
 *
 * @author jhZhang
 * @date 2018/6/11
 */
public class LevelFGMountTask implements MountTask {
    final Level level = Level.F;

    @Override
    public void task(NormalizeAddressToken levelsTokens, List<AddressToken> tokens) {
        for (int i = 0, len = tokens.size(); i < len; i++) {
            AddressToken token = tokens.get(i);
            if (token == null) {
                continue;
            }
            if (token.type.equals(AddressType.Street)) {
                // 先找到路、街道级别
                levelsTokens.mount(token, level);
                tokens.set(i, null);
                // 判断后面的号等级是否为数字
                int j = i + 1;
                if (j < len) {
                    AddressToken afterToken = tokens.get(j);
                    if (afterToken.type.equals(AddressType.SuffixStreet)) {
                        levelsTokens.mount(afterToken, Level.G);
                        tokens.set(j, null);
                    }
                }
            }
        }
    }
}
