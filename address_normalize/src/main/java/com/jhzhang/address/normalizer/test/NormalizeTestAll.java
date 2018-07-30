package com.jhzhang.address.normalizer.test;

import com.jhzhang.address.normalizer.Normalizer;
import com.jhzhang.address.normalizer.test.Counter.MutableInteger;
import com.jhzhang.address.normalizer.test.bean.AddressTest;
import com.jhzhang.address.normalizer.test.tools.ExcelUtils;
import com.jhzhang.address.normalizer.test.tools.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Administrator
 */
public class NormalizeTestAll {
    private static final Logger LOGGER = LogManager.getLogger(NormalizeTestAll.class.getSimpleName());
    static Normalizer instance;

    /**
     * 获得所有的测试集数据
     *
     * @param xml
     * @return
     * @throws Exception
     */
    public static List<AddressTest> runWithFile(String xml) throws Exception {
        Normalizer normalizer = getInstance(xml);
        MutableInteger errCount = new MutableInteger(0);
        List<String> lists = FileUtils.readAllFileToListString("address_normalize/src/main/resources/conf/");
        List<AddressTest> result = new ArrayList<>();
        for (String address : lists) {
            if (address.trim().isEmpty()) {
                continue;
            }
            AddressTest atb = new AddressTest(address);
            if (atb.raw != null) {
                List<String> splits;
                try {
                    splits = normalizer.normalize(atb.raw);
                    atb.setActuals(splits);
                    LOGGER.debug(atb.type + "\t" + atb.raw + "\t" + normalizer.normalizeAsStr(atb.raw, "/"));
                } catch (Exception e) {
                    LOGGER.debug(atb.type + "\t" + atb.raw + "\t地址切分失败");
                    errCount.increase();
                }
                result.add(atb);
            }
        }
        LOGGER.info("归一化失败数据: {}/{}.", errCount.getVal(), lists.size());
        return result;
    }


    public static void runString(String address) throws Exception {
        Normalizer normalizer = getInstance();
        String splitAddress = normalizer.normalizeAsStr(address);
        System.out.println(address + "\t" + splitAddress);
    }

    public static void runMulitString(String address) throws Exception {
        Normalizer normalizer = getInstance();
        String splitAddress = normalizer.normalizeMulitAddressAsStr(address);
        System.out.println(address + "\t" + splitAddress);
    }


    public static void runFileWithNormalizeAddress(String strPath) {
        Normalizer normalizer = getInstance();
        MutableInteger errCount = new MutableInteger(0);
        MutableInteger lineCount = new MutableInteger(0);
        LOGGER.info("=========开始读取归一化的数据=========");
        Iterator<String> it = FileUtils.readFileToListString(strPath).iterator();
        while (it.hasNext()) {
            lineCount.increase();
            String address = it.next();
            String splitAddress = null;
            try {
                splitAddress = normalizer.normalizeAsStr(address);
                System.out.println(address + "\t" + splitAddress);
            } catch (Exception e) {
                LOGGER.info("归一化失败数据: {}/{}.", errCount.getVal(), lineCount.getVal());
            }
//            System.out.println(address + "\t" + splitAddress);
        }
    }

    /**
     * 读取每行的地址数据，按行读取
     */
    public static void runWithSingleFile(String strPath) {
        Normalizer normalizer = getInstance();
        BufferedReader br = null;
        try {
            br = FileUtils.readFileToBuffer(strPath);
        } catch (IOException e) {
            LOGGER.error("读取文件有错");
        }
        MutableInteger errCount = new MutableInteger(0);
        MutableInteger sumCount = new MutableInteger(0);
        MutableInteger poiCount = new MutableInteger(0);
        LOGGER.info("============运行单个数据文件\t" + strPath);
        String line = null;
        List<String> lists = null;
        try {
            while ((line = br.readLine()) != null) {
                sumCount.increase();
                try {
                    lists = null;
                    lists = normalizer.normalize(line);
                    if (lists != null && !lists.get(7).isEmpty()) {
                        poiCount.increase();
                    }
                    System.out.println(line + "\t" + lists);
                } catch (Exception e) {
                    LOGGER.debug("{} 切分失败地址", line);
                    errCount.increase();
                }
            }
        } catch (IOException e) {
            LOGGER.info("读取当行有误" + line);
        }
        LOGGER.info("未归一化率:{}/{} poi的条数{}", errCount.getVal(), sumCount.getVal(), poiCount.getVal());
    }

    /**
     * 读取标准的切分词典中的数据
     *
     * @param xml
     * @throws Exception
     */
    public static void runMainWithRightFile(String xml) throws Exception {
        // 原来是的数据运行时间
        LOGGER.info("---原来是的配置运行时间----");
        long start1 = System.currentTimeMillis();
        List<AddressTest> atLists = runWithFile(xml);
        long end1 = System.currentTimeMillis();
        LOGGER.info("总共归一化用时{}/ms", end1 - start1);
        // 统计前面四级地址元素
        CountUtils.countPresionAndRecallF1(atLists, 0, 4);
        // 统计前面8级地址元素的
        CountUtils.countPresionAndRecallF1(atLists, 0, 8);
        // 统计整个的数值
        CountUtils.countPresionAndRecallF1(atLists);

        CountUtils.countEeveryPRF(atLists);
        ExcelUtils.writeDataToExcel(atLists);
    }

    public static void runWithExcel(String filePath) throws IOException {
        Normalizer normalizer = getInstance();
        List<String> splits;
        String address;
        StringBuffer roadAndNumber = new StringBuffer();
        HSSFWorkbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet("结果");
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("路号");
        row.createCell(1).setCellValue("原始地址");
        row.createCell(2).setCellValue("切分后地址");
        int index = 0;
        for (Iterator<String> it = ExcelUtils.readFileFromExcel(filePath); it.hasNext(); roadAndNumber.setLength(0)) {
            row = sheet.createRow(++index);
            address = it.next();
            try {
                splits = normalizer.normalize(address);
            } catch (Exception e) {
                LOGGER.info("error address: " + address);
                continue;
            }
            roadAndNumber.append(splits.get(5)).append(splits.get(6));
            row.createCell(0).setCellValue(roadAndNumber.toString());
            row.createCell(1).setCellValue(address);
            row.createCell(2).setCellValue(splits.toString());
        }
        Path outPath = Paths.get("output.xls");
        wb.write(Files.newOutputStream(outPath));
    }

    public static void main(String[] args) throws Exception {
//        String address = "深圳市罗湖区翠竹街道办粤海新村4栋2单元704";
//        String address = "广东省深圳市龙岗区大鹏街道建设路东部明珠雅苑1栋D座4层402";
//        String address = "深圳市罗湖区桂园街道金塘街3";
//        String address = "浙江省杭州市富阳区万市街万市村花家18号";
//        String address = "广东省深圳市福田区梅林一村91栋3f"; // 把“梅林”当成了“镇”，“一村”单独分开
//        String address = "广东省深圳市福田区泽田路龙轩100大院";
//        String address = "南京市思南路115弄1号103室"; // 老版本，把“思南路”误匹配到了，“贵州省铜仁市思南县”
//        String address = "江苏省南京市云龙山路88号烽火科技大厦";
//        String address = "深圳市福田区梅岭街道梅林一村13区91栋3F";
//        String address = "深圳市罗湖区东湖街道布心路1023号(东乐花园)76栋(群力楼)7层7A";
//        String address = "杭州拱墅区湖墅新村22栋1单元502号";
        String address = "浙江省杭州市杭州市滨盛路信诚路钱塘帝景西门向阳花教育";
        runString(address);

//        runWithExcel("./address_normalizer/src/main/resources/HouseLon_Lat.xls");
//        ExcelUtils.readFileAndWriteToExcel("./address_normalizer/src/main/resources/HouseLon_Lat.xls");
//        runMainWithRightFile("address.xml");
//        runWithSingleFile("./src/main/resources/errAddrTest");
//        runFileWithNormalizeAddress("./src/main/resources/sample.nb");
//        runWithSingleFile("D:\\User\\Desktop\\problem\\test1.txt");
//        String filePath = "D:\\User\\Desktop\\to南京蒋炳南.xls";
//        ExcelUtils.readFileTowExcel(filePath, getInstance());

    }

    public static Normalizer getInstance(String xml) {

        String parentAddressXml = "address_normalize/src/main/resources/conf/";
        String suffixLevelXml = "addressSuffixLevelData.xml";
        if (instance == null) {
            instance = new Normalizer(parentAddressXml + xml, parentAddressXml + suffixLevelXml);
        }
        return instance;
    }

    public static Normalizer getInstance() {
        String xml = "address.xml";
        return getInstance(xml);
    }
}

