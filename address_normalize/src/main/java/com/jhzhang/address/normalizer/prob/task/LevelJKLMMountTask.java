package com.jhzhang.address.normalizer.prob.task;

import com.jhzhang.address.normalizer.common.Element;
import com.jhzhang.address.normalizer.common.Level;
import com.jhzhang.address.normalizer.common.Vector;
import com.jhzhang.address.normalizer.prob.bean.AddressType;
import com.jhzhang.address.normalizer.prob.complete.NormalizeAddressToken;
import com.jhzhang.address.normalizer.prob.structure.AddressToken;
import com.jhzhang.address.normalizer.prob.complete.NormalizeAddressToken;
import com.jhzhang.address.normalizer.prob.structure.AddressToken;

import java.util.HashMap;
import java.util.List;

/**
 * 后续挂载数字的处理
 *
 * @author jhZhang
 * @date 2018/6/11
 */
public class LevelJKLMMountTask implements MountTask {
    final static HashMap<AddressType, Level> levels = getLevelTypes();

    public static HashMap<AddressType, Level> getLevelTypes() {
        HashMap<AddressType, Level> levelHashMap = new HashMap<>();
        levelHashMap.put(AddressType.SuffixBuilding, Level.J);
        levelHashMap.put(AddressType.SuffixBuildingUnit, Level.K);
        levelHashMap.put(AddressType.SuffixFloor, Level.L);
        levelHashMap.put(AddressType.SuffixRoom, Level.M);
        return levelHashMap;
    }

    @Override
    public void task(NormalizeAddressToken levelsTokens, List<AddressToken> tokens) {
        for (int i = 0, len = tokens.size(); i < len; i++) {
            AddressToken token = tokens.get(i);
            if (token == null) {
                continue;
            }
            AddressType type = token.type;
            if (levels.containsKey(type)) {
                levelsTokens.mount(token, levels.get(type));
                tokens.set(i, null);
            }
        }
    }
}
