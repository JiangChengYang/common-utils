package com.common.utils.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/*public void export(long paperId, HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        response.setHeader("Content-disposition", "attachment; filename=" + new String(
                (paperService.getBare(paperId).getName() + ".xlsx").getBytes(StandardCharsets.UTF_8), "ISO8859-1")
        );
        ExcelWriter writer = ExcelWriter.newInstance("/paperAnswerExport.xlsx", 1, 0);
        int[] pages = new int[]{1};

        writer.createExcel(() -> {
            PageHelper.startPage(pages[0]++, 5000);
            List<PaperStatisticsVo> paperStatisticsVos = paperStatisticsService.paperStatistics(paperId);
            if (CollectionUtils.isEmpty(paperStatisticsVos)) return null;

            List<String[]> data = Lists.newArrayList();
            int i = 0;
            for (PaperStatisticsVo userInfo : paperStatisticsVos) {
                i += 1;
                try {
                    data.add(
                            new String[]{
                                    String.valueOf(i),
                                    userInfo.getUserName(),
                                    userInfo.sexInfo(),
                                    String.valueOf(userInfo.getPhone()),
                                    userInfo.timeUseInfo(),
                                    String.valueOf(userInfo.getScore())
                            }
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            return data;
        });

        writer.write(response.getOutputStream());
    }*/
public class ExcelWriter {
    private static final Logger log = LoggerFactory.getLogger(ExcelWriter.class);
    private String templatePath;
    private int rowNumStart;
    private int cellNumStart;
    private Workbook workbook;

    private ExcelWriter(String template, int rowNumStart, int cellNumStart) {
        this.templatePath = template;
        this.rowNumStart = rowNumStart;
        this.cellNumStart = cellNumStart;
    }

    public static ExcelWriter newInstance(String templatePath, int rowNumStart, int cellNumStart) {
        return new ExcelWriter(templatePath, rowNumStart, cellNumStart);
    }

    public void createExcel(DataInputChannel dataInputChannel) throws IOException {
        this.workbook = new SXSSFWorkbook(new XSSFWorkbook(getClass().getResourceAsStream(this.templatePath)), 10000);
        Sheet sheet = this.workbook.getSheetAt(0);

        List readData;
        while ((readData = dataInputChannel.read()) != null) {
            for (int dataIndex = 0; dataIndex < readData.size(); ++dataIndex) {
                Row row = sheet.createRow(this.rowNumStart++);
                String[] rowData = (String[]) readData.get(dataIndex);

                for (int propertyIndex = 0; propertyIndex < rowData.length; ++propertyIndex) {
                    Cell cell = row.createCell(this.cellNumStart + propertyIndex, CellType.STRING);
                    cell.setCellValue(rowData[propertyIndex]);
                }
            }
        }

    }

    public void write(OutputStream outputStream) throws IOException {
        try {
            this.workbook.write(outputStream);
        } catch (Exception var6) {
            log.error("export Excel error!", var6);
        } finally {
            this.workbook.close();
        }

    }

    public interface DataInputChannel {
        List<String[]> read();
    }
}
