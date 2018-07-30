package com.jhzhang.address.normalizer.prob.task;

import com.jhzhang.address.normalizer.AddressTagger;
import com.jhzhang.address.normalizer.common.Element;
import com.jhzhang.address.normalizer.common.Level;
import com.jhzhang.address.normalizer.common.Vector;
import com.jhzhang.address.normalizer.prob.bean.AddressType;
import com.jhzhang.address.normalizer.prob.complete.NormalizeAddressToken;
import com.jhzhang.address.normalizer.prob.structure.AddressToken;
import com.jhzhang.address.normalizer.prob.complete.NormalizeAddressToken;
import com.jhzhang.address.normalizer.prob.structure.AddressToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 通过分词器来补全对切分标注后的行政数据进行补全和剔除不符合条件的地址
 * <ol>1. 调整行政的地址数据</ol>
 * <ol>2. 根据trie树确定最大可能的行政信息</ol>
 * <ol>3. 调整等级位置并且补全缺失信息</ol>
 * ·
 *
 * @author jhZhang
 * @date 2018/6/8
 */
public class LevelABCMountTask implements MountTask {

    /**
     * 前三级
     */
    private final HashMap<AddressType, Level> levels = getLevelTypes();

    public static void main(String[] args) {
        String addressStr = "浙江省杭州市上城区小营街道小营社区上马市街梅林二村";
        // 过滤数据
        String address = addressStr;
        System.out.println(address);
        new LevelABCMountTask();
        ArrayList<AddressToken> ret = AddressTagger.tag(address);

        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < (ret.size() - 1); ++i) {
            AddressToken token = ret.get(i);
            System.out.println("token:" + token);
            sb.append(token.termText + "|" + token.type + " ");
        }
        System.out.println(sb.toString());
    }

    public static HashMap<AddressType, Level> getLevelTypes() {
        HashMap<AddressType, Level> levelHashMap = new HashMap<>();
        levelHashMap.put(AddressType.Province, Level.A);
        levelHashMap.put(AddressType.Municipality, Level.A);
        levelHashMap.put(AddressType.City, Level.B);
        levelHashMap.put(AddressType.County, Level.C);
        return levelHashMap;
    }

    @Override
    public void task(NormalizeAddressToken levelsTokens, List<AddressToken> tokens) {
        for (int i = 0, len = tokens.size(); i < len; i++) {
            AddressToken token = tokens.get(i);
            if (levels.get(token.type) != null) {
                levelsTokens.mount(token, levels.get(token.type));
                // 让这里的值不在被遍历
                tokens.set(i, null);
            }
        }
    }
}
