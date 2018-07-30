package com.jhzhang.address.normalizer;


import com.jhzhang.address.normalizer.cache.AddressTokenMatcher;
import com.jhzhang.address.normalizer.prob.common.DicAddressTool;
import com.jhzhang.address.normalizer.prob.common.UnKnowGrammarTool;
import com.jhzhang.address.normalizer.prob.complete.NormalizeAddressToken;
import com.jhzhang.address.normalizer.prob.dictree.DicAddress;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 对外接口类. 提供地址归一化功能：
 *
 * @author johntse & jhzhang
 */
public final class Normalizer {
    public Normalizer(String addressXmlPath, String suffixXmlPath) {
        AddressTokenMatcher.getInstance().init(addressXmlPath, suffixXmlPath);
        // todo: 初始化加载字典，如何在不更改原有配置的基础上读取jar中的配置文件
        URL dicPathUrl = this.getClass().getClassLoader().getResource("small");
        URL unkownPathUrl = this.getClass().getClassLoader().getResource("unkown.txt");

        Path dicPath = Paths.get(dicPathUrl.getPath().substring(1));
        Path unkownPath = Paths.get(unkownPathUrl.getPath().substring(1));
        if (!Files.exists(dicPath) && !Files.exists(unkownPath)) {
            dicPath = Paths.get("src/main/resources/small");
            unkownPath = Paths.get("src/main/resources/unkown.txt");
        }
        DicAddressTool.initDicAddress(dicPath.toString(), DicAddress.getInstance());
        UnKnowGrammarTool.init(unkownPath.toString());
    }


    /**
     * 对原始地址进行归一化处理.
     *
     * @param raw 原始地址
     * @return 归一化后的地址
     * @throws Exception 地址处理异常
     */
    public List<String> normalize(String raw) throws Exception {
        NormalizeAddressToken token = NormalizeAddressToken.valueOf(raw);
        // 不进行补全替换操作
        token.fixABCDELevel();
        return token.asList();
    }

    /**
     * 对原始地址进行归一化处理，并将结果生成以指定分隔符分割的字符串.
     *
     * @param raw       原始地址
     * @param delimiter 指定分隔符
     * @return 以指定分隔符分割的归一化后的地址
     * @throws Exception 地址处理异常
     */
    public String normalizeAsStr(String raw, String delimiter) throws Exception {
        List<String> list = normalize(raw);
        int size = list.size();

        if (size == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        String tmp = null;
        for (int i = 0; i < list.size(); i++) {
            tmp = list.get(i);
            if (tmp != null) {
                sb.append(tmp);
            }
            if (i < list.size() - 1) {
                sb.append(delimiter);
            }
        }

        return sb.toString();
    }

    /**
     * 对原始地址进行归一化处理，并将结果生成以默认分隔符(\u0019)分割的字符串.
     *
     * @param raw 原始地址
     * @return 以默认分隔符分割的归一化后的地址
     * @throws Exception 地址处理异常
     */
    public String normalizeAsStr(String raw) throws Exception {
        return normalizeAsStr(raw, "\u0019");
    }

    /**
     * 对多条拼接的地址数据进行归一化，以默认分隔符切分拼接
     *
     * @param raw
     * @return 返回切分后重新拼接的地址
     */
    public String normalizeMulitAddressAsStr(String raw) {
        return normalizeMulitAddressAsStr(raw, "\u001F");
    }

    /**
     * 对多条拼接的地址数据进行归一化
     *
     * @param raw
     * @param delimiter 拼接地址分隔符
     * @return 返回切分后重新拼接的地址
     */
    public String normalizeMulitAddressAsStr(String raw, String delimiter) {
        String[] lists = raw.split(delimiter);
        StringBuilder sb = new StringBuilder();
        for (String address : lists) {
            // 如果成功，则添加切分后地址，否则添加失败地址
            try {
                sb.append(normalizeAsStr(address));
            } catch (Exception e) {
                sb.append(address);
            }
            sb.append(delimiter);
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

}
