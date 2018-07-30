package com.jhzhang.address.normalizer.prob.task;

import com.jhzhang.address.normalizer.common.Level;
import com.jhzhang.address.normalizer.prob.bean.AddressType;
import com.jhzhang.address.normalizer.prob.complete.NormalizeAddressToken;
import com.jhzhang.address.normalizer.prob.structure.AddressToken;
import com.jhzhang.address.normalizer.prob.complete.NormalizeAddressToken;
import com.jhzhang.address.normalizer.prob.structure.AddressToken;

import java.util.HashSet;
import java.util.List;

/**
 * @author jhZhang
 * @date 2018/6/11
 */
public class LevelDMountTask implements MountTask {
    final HashSet sets = getLevelTypes();
    private final Level level = Level.D;

    public static HashSet<AddressType> getLevelTypes() {
        HashSet<AddressType> sets = new HashSet<>();
        sets.add(AddressType.Town);
        sets.add(AddressType.Street);
        return sets;
    }

    @Override
    public void task(NormalizeAddressToken levelsTokens, List<AddressToken> tokens) {
        for (int i = 0, len = tokens.size(); i < len; i++) {
            AddressToken token = tokens.get(i);
            if (token == null) {
                continue;
            }
            if (token.type.equals(AddressType.Town)) {
                levelsTokens.mount(token, level);
                tokens.set(i, null);
                //判断识别出来的是街道还是大街级别
            } else if (token.type.equals(AddressType.Street)) {
                for (int j = i + 1; j < len; j++) {
                    AddressToken afterToken = tokens.get(j);
                    if (afterToken == null) {
                        continue;
                    }
                    // 如果后面等级含有社会或者街道级别，则认为为街道级别
                    if (afterToken.type.equals(AddressType.District) ||
                            afterToken.type.equals(AddressType.Street)) {
                        levelsTokens.mount(token, level);
                        tokens.set(i, null);
                        break;
                    }
                }
            }
        }
    }
}
