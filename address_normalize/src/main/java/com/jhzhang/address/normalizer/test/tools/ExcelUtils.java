package com.jhzhang.address.normalizer.test.tools;

import com.jhzhang.address.normalizer.Normalizer;
import com.jhzhang.address.normalizer.test.CountUtils;
import com.jhzhang.address.normalizer.test.NormalizeTestAll;
import com.jhzhang.address.normalizer.test.bean.AddressTest;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author jhZhang
 * @date 2018/4/9
 */
public class ExcelUtils {

    public static void writeDataToExcel(HSSFWorkbook wb, String fileName) {
        Path outPath = Paths.get("./address_normalizer/src/main/resources/" + fileName);
        try {
            wb.write(Files.newOutputStream(outPath));
        } catch (IOException e) {
            System.err.println("writeDataToExcel:" + outPath.getFileName() + e.getMessage());
        }
    }

    public static void writeDataToExcel(List<AddressTest> lists) throws IOException {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String fileName = String.format("src/main/resources/workbook_%s.xls", df.format(new Date()));
        FileOutputStream out = new FileOutputStream(fileName);
        writeAddressDataToExcel(lists, out);
    }

    /**
     * 将地址输入写入到excel中
     *
     * @param lists
     * @param out
     * @throws IOException
     */
    public static void writeAddressDataToExcel(List<AddressTest> lists, OutputStream out) throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("切分结果");
        HSSFCellStyle cellStyle = wb.createCellStyle();
        // 设置单元格格式
        {
            // 设置为
            cellStyle.setFillForegroundColor(HSSFColor.AQUA.index);
            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        // 创建第一行表头
        HSSFRow row = sheet.createRow(0);
        HSSFCell cell = row.createCell(0);
        cell.setCellValue("type");
        row.createCell(1).setCellValue("raw/excepts");
        for (int i = 0; i < 13; i++) {
            cell = row.createCell(2 + i);
            cell.setCellValue(String.valueOf(i) + "级");
            cell.setCellStyle(cellStyle);
        }

        // 设置一行数据引用
        HSSFRow sRow;
        for (int i = 0, length = lists.size(); i < length; i++) {
            AddressTest at = lists.get(i);
            sRow = sheet.createRow(1 + i);
            sRow.createCell(0).setCellValue(at.type);
            sRow.createCell(1).setCellValue(at.raw + "\n" + Arrays.deepToString(at.excepts));
            if (at.actuals != null) {
                for (int j = 0; j < 13; j++) {
                    sRow.createCell(2 + j).setCellValue(at.actuals[j]);
                    // 如果某一个级别元素不等，设置前景色
                    if (j <= 4 && at.actuals[j] != null && at.excepts[j] != null && !at.actuals[j].equals(at.excepts[j])) {
                        sRow.getCell(2 + j).setCellStyle(cellStyle);
                    }
                }
            }
        }
        // 自动适应raw列大小
        sheet.autoSizeColumn(1);
        // 将统计结果写入excel中
        HSSFSheet sheet2 = wb.createSheet("结果");
        List<String[]> result = new ArrayList<>();
        for (int i = 0; i < 13; i++) {
            result.add(CountUtils.countPresionAndRecallF1(lists, i, i + 1, false));
        }
        result.add(CountUtils.countPresionAndRecallF1(lists, 0, 4, false));
        result.add(CountUtils.countPresionAndRecallF1(lists, 0, 8, false));
        result.add(CountUtils.countPresionAndRecallF1(lists, 0, 13, false));
        sheet2.createRow(0).createCell(0).setCellValue("");
        sheet2.createRow(1).createCell(0).setCellValue("精确率");
        sheet2.createRow(2).createCell(0).setCellValue("召回率");
        sheet2.createRow(3).createCell(0).setCellValue("F值");
        String[] strs = null;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < result.size(); j++) {
                strs = result.get(j);
                sheet2.getRow(i).createCell(1 + j).setCellValue(strs[i]);
            }
        }

        wb.write(out);
    }

    public static HSSFWorkbook getHSSFWorkBookInstance(String filePath) {
        Path path = Paths.get(filePath);
        return getHSSFWorkBookInstance(path);
    }

    public static HSSFWorkbook getHSSFWorkBookInstance(Path path) {
        try {
            return new HSSFWorkbook(Files.newInputStream(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Iterator<String> readFileFromExcel(String filePath) {
        List<String> results = new ArrayList<>();
        HSSFWorkbook wb = getHSSFWorkBookInstance(filePath);
        Sheet sheet;
        Row row;
        Cell cell;
        int columnIndex = -1;
        for (Iterator<Sheet> shIt = wb.iterator(); shIt.hasNext(); ) {
            sheet = shIt.next();
            columnIndex = -1;
            System.out.println("开始读取表：" + sheet.getSheetName());
            for (Iterator<Row> rowIt = sheet.iterator(); rowIt.hasNext(); ) {
                row = rowIt.next();
                if (columnIndex != -1) {
                    results.add(row.getCell(columnIndex).getStringCellValue());
                } else {
                    for (Iterator<Cell> cellIt = row.iterator(); rowIt.hasNext(); ) {
                        cell = cellIt.next();
                        if (cell.getStringCellValue().equals("地址")) {
                            columnIndex = cell.getColumnIndex();
                            break;
                        }
                    }
                }
            }
        }
        return results.iterator();
    }

    public static HSSFWorkbook readFileAndWriteToExcel(String filePath) throws IOException {
        Path fileInput = Paths.get(filePath);
        HSSFWorkbook wb = ExcelUtils.getHSSFWorkBookInstance(fileInput);
        Normalizer normalizer = NormalizeTestAll.getInstance();
//        Normalizer normalizer = LinuxNormalize.getInstance();
        Sheet sheet;
        Row row;
        Cell cell;
        String address;
        List<String> splits;
        StringBuilder sb = new StringBuilder();
        int lastCellNum, addrIndex = -1;
        for (Iterator<Sheet> shIt = wb.iterator(); shIt.hasNext(); ) {
            sheet = shIt.next();
            lastCellNum = -1;
            System.out.println("开始读取表：" + sheet.getSheetName());
            for (Iterator<Row> rowIt = sheet.iterator(); rowIt.hasNext(); sb.setLength(0)) {
                row = rowIt.next();
                if (lastCellNum != -1 && addrIndex != -1) {
                    address = row.getCell(addrIndex).getStringCellValue();
                    try {
                        splits = normalizer.normalize(address);
                        sb.append(splits.get(5)).append(",").append(splits.get(6));
                    } catch (Exception e) {
                        sb.append("null");
//                        continue;
                    }
                    row.createCell(lastCellNum).setCellValue(sb.toString());
                } else {
                    // 读取第一行最末尾的行号
                    lastCellNum = row.getLastCellNum();
                    for (Iterator<Cell> cellIt = row.iterator(); cellIt.hasNext(); ) {
                        cell = cellIt.next();
                        if (cell.getStringCellValue().equals("地址")) {
                            addrIndex = cell.getColumnIndex();
                        }
                    }
                    row.createCell(lastCellNum).setCellValue("路号");
                }
            }
        }
        return wb;
    }

    public static void main(String[] args) throws IOException {
//        HSSFWorkbook wb = readFileAndWriteToExcel(args[0]);
        HSSFWorkbook wb = readFileAndWriteToExcel("./address_normalizer/src/main/resources/HouseLon_Lat.xls");
        writeDataToExcel(wb, "./output3.xls");
    }

    public static void readFileTowExcel(String filePath, Normalizer normalizer) throws IOException {
        Path path = Paths.get(filePath);
        HSSFWorkbook wb = getHSSFWorkBookInstance(filePath);
        HSSFSheet sheet = wb.getSheetAt(0);
        Iterator<Row> iterator = sheet.rowIterator();
        String address = null;
        String splitAddr = null;
        Row row = null;
        long error = 0;
        long sum = 0;
        while (iterator.hasNext()) {
            sum++;
            address = null;
            splitAddr = null;
            row = iterator.next();
            try {
                address = String.valueOf(row.getCell(0).getStringCellValue());
                splitAddr = normalizer.normalizeAsStr(address);
                row.createCell(1).setCellValue(splitAddr);
//                System.out.println(address + "\t" + splitAddr);
            } catch (Exception e) {
                row.createCell(1).setCellValue("");
                error++;
                continue;
            }
        }
        System.out.printf("总共切分失败 %d / %d ", error, sum);
        wb.write(new File(path.getParent().toString() + "./result.xls"));
    }
}
