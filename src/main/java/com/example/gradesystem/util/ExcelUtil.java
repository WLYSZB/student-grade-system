package com.example.gradesystem.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExcelUtil {

    /**
     * 简易CSV解析，用于批量导入成绩
     * 格式：学号,课程ID,成绩
     */
    public static List<Map<String, String>> parseCsv(InputStream inputStream) throws Exception {
        List<Map<String, String>> result = new ArrayList<>();
        byte[] bytes = inputStream.readAllBytes();
        String content = new String(bytes, "UTF-8");
        String[] lines = content.split("\n");

        if (lines.length < 2) {
            return result;
        }

        String[] headers = lines[0].split(",");
        for (int i = 1; i < lines.length; i++) {
            String[] values = lines[i].split(",");
            if (values.length < headers.length) continue;
            Map<String, String> row = new java.util.HashMap<>();
            for (int j = 0; j < headers.length; j++) {
                row.put(headers[j].trim(), values[j].trim());
            }
            result.add(row);
        }
        return result;
    }

    /**
     * 读取CSV/Excel，返回行列表（兼容旧调用）
     * 每行为 String[]，第一行为表头
     */
    public static List<String[]> readExcel(InputStream inputStream) throws Exception {
        List<String[]> result = new ArrayList<>();
        byte[] bytes = inputStream.readAllBytes();
        String content = new String(bytes, "UTF-8");
        String[] lines = content.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            result.add(trimmed.split(","));
        }
        return result;
    }

    /**
     * 简易CSV导出
     */
    public static void exportCsv(OutputStream outputStream, String[] headers, List<String[]> data) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append(String.join(",", headers)).append("\n");
        for (String[] row : data) {
            sb.append(String.join(",", row)).append("\n");
        }
        outputStream.write(sb.toString().getBytes("UTF-8"));
        outputStream.flush();
    }
}
