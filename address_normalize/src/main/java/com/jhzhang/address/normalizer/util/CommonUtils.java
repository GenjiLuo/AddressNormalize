package com.jhzhang.address.normalizer.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonUtils {

    // 地址清理
    private static final Pattern ADDRESS_CLEAN = Pattern.compile("[\u4e00-\u9fa5]+");

    /**
     * 全角转半角.
     *
     * @param value 可能包含全角的字符串
     * @return 只包含半角的字符串
     */
    public static String SBC2DBC(String value) {
        if (value == null) {
            return null;
        }

        char[] c = value.toCharArray();
        for (int i = 0; i < c.length; ++i) {
            if (c[i] == 12288) {
                c[i] = ' ';
            } else if ((c[i] > 65280) && (c[i] < 65375)) {
                c[i] = (char) (c[i] - 65248);
            }
        }

        return new String(c);
    }

    /**
     * 格式化地址.
     * 包括全角转半角、转大写、去除特殊字符
     *
     * @param address 未格式化的地址
     * @return 格式化后的地址
     */
    public static String formatAddress(String address) {
        address = SBC2DBC(address); // 全角转半角
        address = address.toUpperCase(); //转大写
        address = filterRepeatAddress(address); // 去除重复片段

        // 去掉地址前面一串非汉字的内容，比如：025-86988000 建邺区烽火科技
        Matcher m = ADDRESS_CLEAN.matcher(address);
        if (m.find()) {
            address = address.substring(address.indexOf(m.group()));
        }

        // 去除特殊字符
        address = address.replace(" ", "").replace("|", "").replace("\\", "/");
        address = address.replace(",", "").replace(".", "").replace("、", "/");
        address = address.replace("，", "").replace("。", "").replace("%", "");
        address = address.replace("(", "").replace(")", "").replace("\"", "");
        address = address.replace("（", "").replace("）", "").replace("'", "");
        address = address.replace("【", "").replace("】", "").replace("‘", "");
        address = address.replace("[", "").replace("]", "").replace("’", "");
        address = address.replace("{", "").replace("}", "").replace(":", "");
        address = address.replace("<", "").replace(">", "").replace("：", "");
        address = address.replace("《", "").replace("》", "");
        address = address.replace("*", "").replace("?", "");
        address = address.replace("+", "").replace("·", "");
        address = address.replace("—", "-").replace("^", "");
        address = address.replace("“", "").replace("”", "");
        address = address.replace("~", "").replace("～", "");
        address = address.replace("!", "").replace("！", "");

        return address;
    }


    /**
     * 兴趣点处理.
     *
     * @param poi 兴趣点
     * @return 格式化后的兴趣点
     */
    public static String formatPOI(String poi) {

        poi = poi.replaceAll("^[A-Z0-9]*", "");
        // 去除特殊字符
        poi = poi.replaceAll("[^A-Z0-9\u4e00-\u9fa5]*", "");
        return poi;
    }

    /**
     * 中文数字转阿拉伯数字.
     *
     * @param value 包含中文数字的字符串
     * @return 只包含阿拉伯数字的字符串
     */
    public static String covertToNum(String value) {

        //解析“十”或者“拾”
        if (value.equals("十") || value.equals("拾")) {
            value = value.replace("十", "10").replace("拾", "10");
        } else if (value.endsWith("十") || value.endsWith("拾")) {
            //解析形似二十~九十和十一~十九
            value = value.replace("十", "0").replace("拾", "0");
        } else if (value.startsWith("十") || value.startsWith("拾")) {
            value = value.replace("十", "1").replace("拾", "1");
        } else if ((value.contains("十") || value.contains("拾")) && value.length() == 3) {
            //解析形似二十一，十在中间
            String first = value.substring(0, 1);
            String third = value.substring(2, 3);
            value = first + third;
        }

        value = value.replaceAll("零", "0");
        value = value.replaceAll("一", "1");
        value = value.replaceAll("二", "2");
        value = value.replaceAll("三", "3");
        value = value.replaceAll("四", "4");
        value = value.replaceAll("五", "5");
        value = value.replaceAll("六", "6");
        value = value.replaceAll("七", "7");
        value = value.replaceAll("八", "8");
        value = value.replaceAll("九", "9");

        return value;

    }

    /**
     * 转化字符串中阿拉伯数字为中文.
     *
     * @param value 包含阿拉伯数字的字符串
     * @return 包含中文数字的字符串
     */
    public static String convertNumToChinese(String value) {
        value = formatPOI(value);
        char[] c = value.toCharArray();

        for (int i = 0; i < c.length; i++) {
            switch (c[i]) {
                case '0':
                    c[i] = '零';
                    break;
                case '1':
                    c[i] = '一';
                    break;
                case '2':
                    c[i] = '二';
                    break;
                case '3':
                    c[i] = '三';
                    break;
                case '4':
                    c[i] = '四';
                    break;
                case '5':
                    c[i] = '五';
                    break;
                case '6':
                    c[i] = '六';
                    break;
                case '7':
                    c[i] = '七';
                    break;
                case '8':
                    c[i] = '八';
                    break;
                case '9':
                    c[i] = '九';
                    break;
                default:
                    break;
            }
        }
        return String.valueOf(c);
    }

    /**
     * 过滤重复片段连续三个单词及以上都一样的内容
     *
     * @param rawAddress 未过滤的数据
     * @return 过滤后的新数据
     */
    public static String filterRepeatAddress(String rawAddress) {
        StringBuffer address = new StringBuffer();
        char[] chars = rawAddress.toCharArray();
        int patternLength = 3;
        for (int i = chars.length - 1; i >= 0; i--) {
            String patternAddress = rawAddress.substring(i - patternLength + 1, i + 1);
            String lastAddress = rawAddress.substring(0, i - patternLength + 1);
            // 如果匹配到了重复数据
            while (lastAddress.indexOf(patternAddress) != -1) {
                patternLength++;
                patternAddress = rawAddress.substring(i - patternLength + 1, i + 1);
                lastAddress = rawAddress.substring(0, i - patternLength + 1);
            }
            if (patternLength != 3) {
                // 跳过重复匹配到的重复数据片段
                i = i - patternLength + 2;
                patternLength = 3;
                continue;
            }
            // 匹配到最后，剩余元素长度小于指定的模式串长度
            if (lastAddress.length() <= patternLength) {
                address.reverse().insert(0, patternAddress).insert(0, lastAddress);
                break;
            } else {
                address.append(rawAddress.charAt(i));
            }
        }
        return String.valueOf(address);
    }
}
