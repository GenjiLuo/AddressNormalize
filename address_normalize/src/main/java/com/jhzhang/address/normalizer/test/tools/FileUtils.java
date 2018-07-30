package com.jhzhang.address.normalizer.test.tools;

import com.alibaba.fastjson.JSONObject;
import com.jhzhang.address.normalizer.test.bean.AddressTest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.nio.charset.Charset.defaultCharset;

public class FileUtils {
    final static Logger LOGGER = LogManager.getLogger(FileUtils.class.getSimpleName());

    /**
     * 获得所有的文件夹下的文件
     *
     * @param strPath
     * @return
     */
    public static List<Path> getAllFile(String strPath) {
        List<Path> result = new ArrayList<>();
        Path dir = Paths.get(strPath);
        try (DirectoryStream<Path> entries = Files.newDirectoryStream(dir)) {
            for (Path entry : entries) {
                if (Files.isDirectory(entry)) {
                    result.addAll(getAllFile(entry.toString()));
                } else {
                    if (!entry.endsWith("README.txt")) {
                        result.add(entry);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 路径下的所有文件内容，按行读取到列表中
     *
     * @param strPath
     * @return 列表
     * @throws IOException
     */
    public static List<String> readAllFileToListString(String strPath) {
        List<String> listsStr = new ArrayList<>();
        List<Path> lists = getAllFile(strPath);
        for (Path p : lists) {
            try {
                listsStr.addAll(Files.readAllLines(p, defaultCharset()));
            } catch (Exception e) {
                e.getMessage();
            }
        }
        return listsStr;
    }

    /**
     * 从每行的json数据中解析出地址
     *
     * @param strPath
     * @return 列表
     * @ IOException
     */
    public static List<String> readFileToListString(String strPath) {
        List<String> result = new ArrayList<>();
        Path path = Paths.get(strPath);
        List<String> listsStr = null;
        if (!Files.exists(path)) {
            throw new RuntimeException("输入文件不存在");
        }
        try {
            listsStr = Files.readAllLines(path, Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Iterator<String> it = listsStr.iterator();
        while (it.hasNext()) {
            JSONObject jsonObject = null;
            try {
                jsonObject = (JSONObject) JSONObject.parse(it.next());
            } catch (Exception e) {
                continue;
            }
            String line = jsonObject.getString("addr");
            if (line != null) {
                result.add(line);
            }
        }
        return result;
    }


    public static List<String> readFileToList(String strPath) {
        Path path = Paths.get(strPath);
        List<String> lists = null;
        try {
            lists = Files.readAllLines(path, defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lists;
    }

    public static BufferedReader readFileToBuffer(String strPath) throws IOException {
//        Path path = Paths.get(strPath);
        FileInputStream fis = new FileInputStream(new File(strPath));
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
        return br;
    }


    /**
     * 将输出结果写入到文件中
     *
     * @param result
     * @throws IOException
     */
    public static void writeToResult(List<AddressTest> result) throws IOException {
        String path = "E:\\software\\Python\\20180122scapty\\addrType\\test.txt";
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(path))))) {
            for (AddressTest atb : result) {
                bw.write(atb.toString() + "\n");
            }
        }
    }

    /**
     * 将新旧配置的文件写入到文件中
     *
     * @param result1
     * @throws IOException
     */
    public static void writeToResult(List<AddressTest> result1, List<AddressTest> result2) throws IOException {
        String path = "E:\\software\\Python\\20180122scapty\\addrType\\test.txt";
        if (result1.size() == result2.size()) {
            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(path))))) {
                for (int i = 0; i < result1.size(); i++) {
                    bw.write(result1.get(i).toString() + "\n");
                    bw.write(result2.get(i).toString() + "\n");
                }
            }
        } else {
            new RuntimeException("the output result size not equal");
        }
    }

    /**
     * 提取归一化json格式数据中的地址，并且输出
     *
     * @param filePath
     * @throws IOException
     */
    public static void writeNormalizeToAddress(String filePath) throws IOException {
        LineIterator it = new LineIterator(filePath);
        // 便于去重
        HashSet<String> results = new HashSet<>();
        JSONObject jsonObject;
        String address = null;
        while (it.hasNext()) {
            jsonObject = null;
            try {
                jsonObject = (JSONObject) JSONObject.parse(it.next());
            } catch (Exception e) {
                continue;
            }
            address = jsonObject.getString("addr");
            // 替换其中的空格
            results.add(address.replaceAll(" ", ""));
        }
        // 写入文件
        FileWriter fw = new FileWriter("./addrOutput");
        for (String line : results) {
            fw.write(line + "\n");
        }
        fw.flush();
    }

    /**
     * 去除文件中的重复文件字符串
     *
     * @param filePath
     * @throws IOException
     */
    public static void removeSameAddress(String filePath) throws IOException {
        Path fPath = Paths.get(filePath);
        LineIterator it = new LineIterator(fPath);
        // 便于去重
        HashSet<String> results = new HashSet<>();
        while (it.hasNext()) {
            results.add(it.next().replaceAll(" ", ""));
        }
        // 写入文件
        String time = SimpleDateFormat.getTimeInstance().format(new Date());
        Path outPut = Paths.get(fPath.getParent().toString(), time);
        FileWriter fw = new FileWriter(outPut.toFile());
        for (String line : results) {
            fw.write(line + "\n");
        }
        fw.flush();
    }

    /**
     * 按照名称/日期/fileName_时间的形式生成路径
     *
     * @param parentDirName 父类名称
     * @param fileName      文件名
     * @return 文件输出路径
     */
    public static Path getOutPath(String parentDirName, String fileName) {
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_kk-mm-ss");
        String[] dateTime = sdf.format(new Date()).split("_");
        Path parentPath = Paths.get("./", parentDirName, dateTime[0]);
        if (!Files.exists(parentPath)) {
            try {
                Files.createDirectories(parentPath);
            } catch (IOException e) {
                throw new RuntimeException("创建文件异常" + parentPath.toString());
            }
        }
        return Paths.get(parentPath.toString(), fileName + "_" + dateTime[1]);
    }


    public static void main(String[] args) throws IOException {
//        writeNormalizeToAddress(args[0]);
//        removeSameAddress(args[0]);
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_kk'h'-mm'm'-ss's'");
        System.out.println(sdf.format(new Date()));
    }
}
