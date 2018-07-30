package com.jhzhang.address.normalizer.core;

import com.jhzhang.address.normalizer.common.Element;

import java.util.regex.Pattern;

/**
 * 中文数字到阿拉伯数字的相互转换 on 2016/9/10.
 * @author
 */
public final class NumberConverter {
    private static final String EMPTY_STRING = "";
    private static final String CN_NUMBER = "零一二三四五六七八九";
    @SuppressWarnings("unused")
    private static final Pattern digitPattern = Pattern.compile("[0-9]+");


    /**
     * 将中文数字转换成阿拉伯数字（只支持一到九），如果不含中文数字或是空字符串，就原样返回.
     *
     * @param content 输入的字符串
     * @return 经过处理后的字符串
     */
    private static int cnNumberToDigit(char content) {
        switch (content) {
            case '零':
                return 0;
            case '一':
                return 1;
            case '二':
                return 2;
            case '三':
                return 3;
            case '四':
                return 4;
            case '五':
                return 5;
            case '六':
                return 6;
            case '七':
                return 7;
            case '八':
                return 8;
            case '九':
                return 9;

            default:
                return content;
        }

    }

    /**
     * 将阿拉伯数字转换成中文数字（只支持1-9），如果不含阿拉伯数字或是空字符串，就原样返回.
     *
     * @param content 输入的字符串
     * @return 经过处理后的字符串
     */
    public static String digitToCnNumber(String content) {
        if (content.length() <= 0) {
            return EMPTY_STRING;
        }

        char[] chars = content.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            switch (chars[i]) {
                case '1':
                    chars[i] = '一';
                    break;
                case '2':
                    chars[i] = '二';
                    break;
                case '3':
                    chars[i] = '三';
                    break;
                case '4':
                    chars[i] = '四';
                    break;
                case '5':
                    chars[i] = '五';
                    break;
                case '6':
                    chars[i] = '六';
                    break;
                case '7':
                    chars[i] = '七';
                    break;
                case '8':
                    chars[i] = '八';
                    break;
                case '9':
                    chars[i] = '九';
                    break;
                default:
                    break;
            }
        }

        return new String(chars);
    }


    /**
     * 将一百以内字符串中的中文数字转换成阿拉伯数字.
     *
     * @param input 输入的需要处理的字符串
     * @return 处理后的结果
     */
    private static String cnToDigitInHundred(String input) {
        String remark = "十";
        int sum = 10;
        for (int i = 0; i < input.length(); i++) {
            String pre = input.substring(i, i + 1);
            String unit;
            String suf;
            if (CN_NUMBER.contains(pre)) {
                if (i == input.length() - 1
                        || (i == input.length() - 2
                        && !input.substring(i + 1, i + 2).equals(remark))) {
                    String convert = String.valueOf(cnNumberToDigit(pre.charAt(0)));
                    input = input.replaceFirst(pre, convert);
                } else if (i == input.length() - 2
                        && (unit = input.substring(i + 1, i + 2)).equals(remark)) {
                    sum = cnNumberToDigit(pre.charAt(0)) * sum;
                    input = input.replaceFirst(pre + unit, String.valueOf(sum));
                    i = i + 1;
                } else if (i < input.length() - 2
                        && (unit = input.substring(i + 1, i + 2)).equals(remark)
                        && !CN_NUMBER.contains(input.substring(i + 2, i + 3))) {
                    sum = cnNumberToDigit(pre.charAt(0)) * sum;
                    input = input.replaceFirst(pre + unit, String.valueOf(sum));
                    i = i + 1;
                } else if (i < input.length() - 2
                        && (unit = input.substring(i + 1, i + 2)).equals(remark)
                        && CN_NUMBER.contains(suf = input.substring(i + 2, i + 3))) {
                    sum = cnNumberToDigit(pre.charAt(0)) * sum
                            + cnNumberToDigit(suf.charAt(0));
                    input = input.replaceFirst(pre + unit + suf, String.valueOf(sum));
                    i = i + 2;
                }
            } else if (pre.equals(remark)) {
                if (i == input.length() - 1
                        || (i == input.length() - 2
                        && !CN_NUMBER.contains(input.substring(i + 1, i + 2)))) {
                    input = input.replaceFirst(pre, String.valueOf(sum));
                } else if (i <= input.length() - 2
                        && CN_NUMBER.contains(suf = input.substring(i + 1, i + 2))) {
                    sum += cnNumberToDigit(suf.charAt(0));
                    input = input.replaceFirst(pre + suf, String.valueOf(sum));
                    i = i + 1;
                }
            }
        }
        return input;
    }

    /**
     * 将一百以内字符串中的中文数字转换成阿拉伯数字.
     *
     * @param element 要转换的地址元素
     * @return 转换的结果
     */
    public static Element cnToDigitInHundred(Element element) {
        String name = element.getName();
        name = cnToDigitInHundred(name);
        return new Element(name,
                element.getSuffix(),
                element.getLevel(),
                element.getStart(), element.getEnd());
    }


    /**
     * 将一百以内字阿拉伯数字转换成中文数字.
     *
     * @param element 地址元素
     * @return 转换后的结果
     */
    public static Element digitsToCn(Element element) {
        String name = element.getName();
        name = digitToCnNumber(name);
        return new Element(name,
                element.getSuffix(),
                element.getLevel(),
                element.getStart(), element.getEnd());
    }

    /**
     * 去除字符串中的标点符号.
     *
     * @param str 原字符串
     * @return 去除标点符号后的结果
     */
    private static String removePunctuation(String str) {
        if (str.length() <= 0) {
            return str;
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < str.length(); i++) {
            String b = String.valueOf(str.charAt(i));
            if (b.matches("[[\u4e00-\u9fa5]a-zA-Z0-9-#]")) {
                sb.append(b);
            }
        }
        return sb.toString();
    }

    /**
     * 去除地址元素中名称的标点符号.
     *
     * @param element 地址元素
     * @return 去除标点符号后的结果
     */
    public static Element removePunctuation(Element element) {
        String name = element.getName();
        name = removePunctuation(name);
        return new Element(name,
                element.getSuffix(),
                element.getLevel(),
                element.getStart(), element.getEnd());
    }

}
