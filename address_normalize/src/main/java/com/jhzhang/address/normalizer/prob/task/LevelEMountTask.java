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
 * @author jhZhang
 * @date 2018/6/11
 */
public class LevelEMountTask implements MountTask {
    final Level level = Level.E;

    @Override
    public void task(NormalizeAddressToken levelsTokens, List<AddressToken> tokens) {
        for (int i = 0, len = tokens.size(); i < len; i++) {
            AddressToken token = tokens.get(i);
            if (token == null) {
                continue;
            }
            // 社区级别
            if (token.type.equals(AddressType.District)) {
                levelsTokens.mount(token, level);
                tokens.set(i, null);
                // 村级别，包含新村和小区村两种级别
            } else if (token.type.equals(AddressType.Village)) {
                // 往后搜索是否存在loadMark级别或者同vliiage级别数据
                for (int j = i + 1; j < len; j++) {
                    AddressToken afterToken = tokens.get(j);
                    if (afterToken.type.equals(AddressType.Street)
                            || afterToken.type.equals(AddressType.Village)) {
                        levelsTokens.mount(token, level);
                        tokens.set(i, null);
                    }
                }
            }
        }
    }
}
