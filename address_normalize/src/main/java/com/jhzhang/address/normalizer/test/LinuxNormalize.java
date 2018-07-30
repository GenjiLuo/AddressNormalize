package com.jhzhang.address.normalizer.test;

import com.jhzhang.address.normalizer.Normalizer;
import com.jhzhang.address.normalizer.cache.exception.InvalidAddressException;
import com.jhzhang.address.normalizer.test.Counter.LevelCounter;
import com.jhzhang.address.normalizer.test.Counter.MutableInteger;
import com.jhzhang.address.normalizer.test.Counter.NormalizeErrorCounter;
import com.jhzhang.address.normalizer.test.Counter.PreLevelCounter;
import com.jhzhang.address.normalizer.test.tools.FileUtils;
import com.jhzhang.address.normalizer.test.tools.LineIterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author jhZhang
 * @date 2018/3/26
 */
public class LinuxNormalize {
    static final Pattern p = Pattern.compile("(\\d\\d).*$", Pattern.MULTILINE);
    final static Logger LOGGER = LogManager.getLogger(LinuxNormalize.class.getSimpleName());
    static Normalizer instance;

    public static Normalizer getInstance() {
        String parentAddressXml = "/home/nebula/Nebula_DataCenter_ExternalTask/JOB_ADDRESS_THEME/mr_task/address_dict";
        Path path = Paths.get(parentAddressXml);
        // 如果不存在，则删除，切换Linux和windows下的输入路径
        if (!Files.exists(path)) {
            parentAddressXml = "E:/software/SVN/address_normalizer_full/address_normalizer_full/job_address_theme/conf/mr_task/address_dict";
        }
        LOGGER.info("使用的配置路径:" + parentAddressXml);
        if (instance == null) {
            instance = new Normalizer(parentAddressXml + "/address.xml", parentAddressXml + "/addressSuffixLevelData.xml");
        }
        return instance;
    }

    public static void analyseAddress(String filePath) throws IOException {
        Path outPath = FileUtils.getOutPath("error", "errorAddress");
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outPath.toFile())));
        Normalizer normalizer = getInstance();
        // 记录总的文本行数
        MutableInteger sum = new MutableInteger(0);
        List<String> lists;
        Matcher m;
        String line = null;
        LineIterator it = new LineIterator(filePath);
        while (it.hasNext()) {
            sum.increase();
            lists = null;
            line = null;
            line = it.next();
            try {
                lists = normalizer.normalize(line);
                // 统计未出错的地址数据的各个级别
                LevelCounter.valueOf(lists);
            } catch (InvalidAddressException e) {
                m = p.matcher(e.getMessage());
                if (m.matches()) {
                    switch (m.group(1)) {
                        case "01": {
                            NormalizeErrorCounter.Error01.increase();
                            bw.write("01\t" + line + "\n");
                            break;
                        }
                        case "02": {
                            NormalizeErrorCounter.Error02.increase();
                            bw.write("02\t" + line + "\n");
                            break;
                        }
                        case "03": {
                            NormalizeErrorCounter.Error03.increase();
                            bw.write("03\t" + line + "\n");
                            break;
                        }
                        default: {
                            NormalizeErrorCounter.Defalult.increase();
                            bw.write("default\t" + line + "\n");
                            break;
                        }
                    }
                } else {
                    NormalizeErrorCounter.Error04.increase();
                    bw.write("04\t" + line + "\n");
                }
            } catch (Exception e) {
                NormalizeErrorCounter.Error05.increase();
                bw.write("05\t" + line + "\n");
            }
            // 每过1百万就输出一次
            if ((sum.getVal() % 1000000) == 0) {
                LOGGER.info("输出数据条数:{}", sum.getVal());
                bw.flush();
            }
        }
        LOGGER.info("总的数据条数:{}", sum.getVal());
        bw.close();
    }

    public static void main(String[] args) throws IOException {
        LOGGER.info("输入的路径:{}", args[0]);
        analyseAddress(args[0]);
        LOGGER.info("预输入的行政等级:{}", PreLevelCounter.toAllString());
        LOGGER.info("补全后的各级别数据:{}", LevelCounter.toAllString());
        LOGGER.info("错误次数:{}", NormalizeErrorCounter.toAllString());
    }
}
