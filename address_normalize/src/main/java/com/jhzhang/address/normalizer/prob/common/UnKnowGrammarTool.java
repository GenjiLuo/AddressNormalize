package com.jhzhang.address.normalizer.prob.common;

import com.jhzhang.address.normalizer.prob.bean.AddressSpan;
import com.jhzhang.address.normalizer.prob.bean.AddressType;
import com.jhzhang.address.normalizer.prob.conf.UnknowGrammar;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * 主要加载和解析未知词配置文件
 *
 * @author jhZhang
 * @date 2018/6/7
 */
public class UnKnowGrammarTool {
    /**
     * 初始化加载未知语法识别
     */
    public static void init(String initFilePath) {
        Path p = Paths.get(initFilePath);
        if (!Files.exists(p)) {
            throw new RuntimeException("输入的路径不存在：" + initFilePath);
        }
        UnknowGrammar instance = UnknowGrammar.getInstance();
        BufferedReader br;
        String line;
        try {
            br = Files.newBufferedReader(p, Charset.forName("utf-8"));
            while ((line = br.readLine()) != null) {
                line = line.trim();
                // 跳过注释
                if (line.isEmpty() || line.startsWith("//") || line.startsWith("#")) {
                    continue;
                }
                load(line, instance);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    static ArrayList<AddressType> transToRhsList(String rhsStr) {
        ArrayList<AddressType> rhs = new ArrayList<>();
        if (rhsStr.trim().isEmpty()) {
            return rhs;
        }
        String[] rhsArray = rhsStr.split("\\|", -1);
        for (String s : rhsArray) {
            rhs.add(AddressType.valueOf(s));
        }
        return rhs;
    }

    static ArrayList<AddressSpan> transToLhsList(String lhsStr) {
        ArrayList<AddressSpan> lhs = new ArrayList<>();
        if (lhsStr.trim().isEmpty()) {
            return lhs;
        }
        String[] lhsArray = lhsStr.split("\\|", -1);
        for (String s : lhsArray) {
            String[] tokens = s.split(",", 2);
            if (tokens.length != 2) {
                throw new IndexOutOfBoundsException("输入的字母符号不符合格式(1,类型)，例如: 1,County");
            }
            int num = Integer.valueOf(tokens[0]);
            AddressType type = AddressType.valueOf(tokens[1]);
            lhs.add(new AddressSpan(num, type));
        }
        return lhs;
    }


    /**
     * 根据单行数据，增加单条未知语法配置，例如，江苏苏州市吴中区江苏省。其所表达的语法规则如下：<br>
     * <code>Start|Province|City|County|Unknow|Province|SuffixProvince	1,Start|1,Province|1,City|1,County|1,Unknow|2,Province</code>
     */
    public static void load(String line, UnknowGrammar unknowGrammar) {
        if (line.trim().isEmpty()) {
            return;
        }
        String[] two = line.split("\t", 2);
        if (two.length != 2) {
            return;
        }
        // “rhs  lhs”
        ArrayList<AddressType> rhs;
        ArrayList<AddressSpan> lhs;
        try {
            rhs = transToRhsList(two[0]);
            lhs = transToLhsList(two[1]);
        } catch (IllegalArgumentException e) {
            return;
        }
        unknowGrammar.addProduct(rhs, lhs);
    }
}
