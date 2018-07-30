package com.jhzhang.address.normalizer.prob.common;

import com.jhzhang.address.normalizer.prob.bean.AddressType;
import com.jhzhang.address.normalizer.prob.dictree.DicAddress;
import com.jhzhang.address.normalizer.prob.dictree.DicAddress;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 创建一个公共的Utils工具
 *
 * @author jhZhang
 * @date 2018/6/6
 */
public class DicAddressTool {
    /**
     * 输入一个文件路径，返回一个BufferedReader对象
     *
     * @param filePath 输入文件路径
     * @return
     */
    public static BufferedReader getBufferedReader(String filePath) {
        Path path = Paths.get(filePath);
        BufferedReader br = null;
        if (Files.exists(path)) {
            try {
                br = Files.newBufferedReader(path, Charset.forName("UTF-8"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("文件路径不存在：" + path.toAbsolutePath());
        }
        return br;
    }

    /**
     * 匹配字符串在起始位置是否为英文
     *
     * @param start    起始点
     * @param sentence 输入的字符串
     * @return
     */
    public static int matchEnglish(int start, String sentence) {
        int i = start;
        int count = sentence.length();
        String sd = "ＱＷＥＲＴＹＵＩＯＰＡＳＤＦＧＨＪＫＬＺＸＣＶＢＮＭ号";
        while (i < count) {
            char c = sentence.charAt(i);
            if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z') {
                ++i;
            } else if (sd.indexOf(c) > -1) {
                ++i;
            } else {
                break;
            }
        }
        return i;
    }

    /**
     * 匹配字符串在起始位置是否为数字
     *
     * @param start    起始点
     * @param sentence 输入的字符串
     * @return
     */
    public static int matchNum(int start, String sentence) {
        int i = start;
        int count = sentence.length();
        while (i < count) {
            char c = sentence.charAt(i);
            // 匹配各种数字
            if ((c >= '0' && c <= '9') || (c >= '０' && c <= '９') || c == '-'
                    || c == '－') {
                ++i;
            } else {
                break;
            }
        }

        if (i > start && i < count) {
            char end = sentence.charAt(i);
            if ('#' == end || '＃' == end) {
                i++;
            }
        }
        return i;
    }

    /**
     * 一个初始化DicAddress的方法工具
     */
    public static void initDicAddress(DicAddress dicAddress) {
        String dicPath = "src/main/resources/small/";
        initDicAddress(dicPath, dicAddress);
    }

    public static void main(String[] args) {
        AddressType[][] bestPre = new AddressType[3][];
        for (int i = 0; i < 3; ++i) {
            bestPre[i] = new AddressType[AddressType.values().length];
        }
        System.out.print(new DicAddressTool().debug(bestPre));
        int[][] pro = new int[3][3];

        System.out.print(debug(pro));

    }

    public static String debug(int[][] arrays) {
        int column = arrays[0].length;
        StringBuilder sb = new StringBuilder();
        for (int[] array : arrays) {
            for (int j = 0; j < column; j++) {
                sb.append(array[j]).append("\t");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public static void initDicAddress(String dicPath, DicAddress dicAddress) {
        if (!dicPath.endsWith("/")) {
            dicPath += "/";
        }
        dicAddress.load(dicPath + "A_province.txt", AddressType.Province, 10000);
        dicAddress.load(dicPath + "B_city.txt", AddressType.City, 10000);
        dicAddress.load(dicPath + "C_county.txt", AddressType.County, 1000);
        dicAddress.load(dicPath + "D_town.txt", AddressType.Town, 1000);
        dicAddress.load(dicPath + "D_SuffixTown.txt", AddressType.SuffixTown, 1000);
        dicAddress.load(dicPath + "E_district.txt", AddressType.District, 1000000);
        dicAddress.load(dicPath + "E_SuffixDistrict.txt", AddressType.SuffixDistrict, 100000);
        dicAddress.load(dicPath + "F_street.txt", AddressType.Street, 10000000);
        dicAddress.load(dicPath + "G_SuffixStreet.txt", AddressType.SuffixStreet, 10000000);
        dicAddress.load(dicPath + "H_landmark.txt", AddressType.LandMark, 100000);
        dicAddress.load(dicPath + "H_village.txt", AddressType.Village, 10000);
        dicAddress.load(dicPath + "H_SuffixLandMark.txt", AddressType.SuffixLandMark, 100000);
        dicAddress.load(dicPath + "J_SuffixBuilding.txt", AddressType.SuffixBuilding, 1000);
        dicAddress.load(dicPath + "K_SuffixBuildingUnit.txt", AddressType.SuffixBuildingUnit, 1000);
        dicAddress.load(dicPath + "L_SuffixFloor.txt", AddressType.SuffixFloor, 1000);
        dicAddress.load(dicPath + "M_SuffixRoom.txt", AddressType.SuffixRoom, 1000);
        dicAddress.load(dicPath + "relatedPos.txt", AddressType.RelatedPos, 100);
        dicAddress.load(dicPath + "other.txt", AddressType.Unknow, 0);
    }

    public <T> String debug(T[][] arrays) {
        int column = arrays[0].length;
        StringBuilder sb = new StringBuilder();
        for (T[] array : arrays) {
            for (int j = 0; j < column; j++) {
                sb.append(array[j]).append("\t");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}