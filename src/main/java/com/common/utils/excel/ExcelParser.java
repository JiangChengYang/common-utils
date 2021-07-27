package com.common.utils.excel;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.poi.ss.usermodel.IndexedColors.RED;


/*public void excelUpload(@PathVariable long classId, @RequestParam MultipartFile file, HttpServletResponse response) throws NoSuchMethodException, InstantiationException,
        IllegalAccessException, InvocationTargetException, IOException, InvalidFormatException {

        response.setContentType("application/octet-stream");
        response.setHeader("Content-disposition", "attachment; filename=" + new String(
        (schoolClassService.getBare(classId).getName() + ".xlsx").getBytes(StandardCharsets.UTF_8), "ISO8859-1")
        );

        LinkedHashMap<String, CellValueMapper> cm = Maps.newLinkedHashMap();

        cm.put("name", value -> {
        if (StringUtils.isBlank(value)) {
        throw new InvalidValueException("学生姓名不能为空");
        }
        return value.trim();
        });

        cm.put("sex", value -> {
        if (!sex_map.containsKey(value)) {
        throw new InvalidValueException("性别格式不正确");
        }
        return sex_map.get(value);
        });

        cm.put("phone", value -> {
        if (StringUtils.isBlank(value) || !value.trim().matches("1\\d{10}")) {
        throw new InvalidValueException("手机号不匹配");
        }
        return Long.parseLong(value.trim());
        });

        ExcelParser<SchoolClassStudentPost> memberExcelParser = ExcelParser.newInstance(1, 0, file.getInputStream(), SchoolClassStudentPost.class, cm);
        memberExcelParser.sax();

        if (CollectionUtils.isNotEmpty(memberExcelParser.getData())) {
        classStudentService.insert(
        classId,
        memberExcelParser.getData()
        .stream()
        .map(DataWrapper::getData)
        .collect(Collectors.toList())
        );
        }

        if (memberExcelParser.isError()) {
        memberExcelParser.write(response.getOutputStream());
        }
        }*/
@Slf4j
public class ExcelParser<T> {
    //删除行标记
    private Set<Integer> proceedRowsIndex = Collections.synchronizedSet(new TreeSet<Integer>(Comparator.reverseOrder()));

    @Getter//校验成功数据模型
    private List<DataWrapper<T>> data = Lists.newArrayList();
    //起始行 从0开始
    private int rowNumStart;
    //起始列 从0开始
    private int cellNumStart;
    //行数据要转换的类型
    private Class<T> modelClass;
    //单元格数据校验及针对modelClass属性的格式换换
    private LinkedHashMap<String, CellValueMapper> cellMapper;
    private Map<Integer, CellValueMapper> cellIndex_checker = Maps.newHashMap();
    private Workbook workbook;
    private FormulaEvaluator evaluator;

    @Getter//是否存在错误
    private boolean error;


    public ExcelParser(int rowNumStart, int cellNumStart, InputStream inputStream, Class<T> modelClass, LinkedHashMap<String, CellValueMapper> cellMapper) throws IOException, InvalidFormatException {
        this.rowNumStart = rowNumStart;
        this.cellNumStart = cellNumStart;
        this.modelClass = modelClass;
        this.cellMapper = cellMapper;

        int cellIndex = cellNumStart;
        for (CellValueMapper checker : this.cellMapper.values()) {
            cellIndex_checker.put(cellIndex++, checker);
        }

        workbook = WorkbookFactory.create(inputStream);
        evaluator = workbook.getCreationHelper().createFormulaEvaluator();
    }

    public static <T> ExcelParser<T> newInstance(int rowNumStart, int cellNumStart, InputStream inputStream, Class<T> modelClass, LinkedHashMap<String, CellValueMapper> cellMapper) throws IOException, InvalidFormatException {
        return new ExcelParser<>(rowNumStart, cellNumStart, inputStream, modelClass, cellMapper);
    }

    public void sax() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        Sheet sheet = workbook.getSheetAt(0);

        for (int rowNum = rowNumStart; rowNum <= sheet.getLastRowNum(); rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (row == null) {
                proceedRowsIndex.add(rowNum);
                continue;
            }

            RowData rowData = new RowData();
            rowData.setIndex(rowNum);
            log.info("row num：{}", row.getRowNum());

            for (int cellIndex = cellNumStart; cellIndex < cellNumStart + cellMapper.size(); cellIndex++) {
                Cell cell = row.getCell(cellIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

                CellData cellData = new CellData(cellIndex, cell != null ? getValue(cell) : null);

                CellValueMapper cellValueMapper = cellIndex_checker.get(cellIndex);
                Assert.notNull(cellValueMapper, "no CellValueMapper found!");
                try {
                    cellData.setTypedValue(cellValueMapper.map((String) cellData.getValue()));
                } catch (InvalidValueException e) {
                    log.error("invalid value!", e);
                    cellData.setMessage(e.getMessage());
                }
                rowData.addCell(cellData);
            }
            if (rowData.getCellData().stream().allMatch(cellData -> cellData.getValue() == null)) {
                //标记删除行
                proceedRowsIndex.add(rowData.getIndex());
                continue;
            }

            //生成数据模型/记录错误数据
            if (rowData.getCellData().stream().noneMatch(CellData::hasError)) {//不存在错误
                T model = ConstructorUtils.invokeConstructor(modelClass, null);

                int index = 0;
                for (String property : cellMapper.keySet()) {
                    BeanUtils.setProperty(model, property, rowData.getCellData().get(index++).getTypedValue());
                }
                data.add(new DataWrapper<>(rowData.getIndex(), model));
            } else {
                error(
                        rowData.getIndex(),
                        rowData.getCellData().stream()
                                .filter(cellData -> StringUtils.isNotBlank(cellData.getMessage()))
                                .map(CellData::getMessage)
                                .collect(Collectors.joining(";"))
                );
            }
        }

    }

    public void error(int rowIndex, String message) {
        error = true;

        Sheet sheet = workbook.getSheetAt(0);
        Row row = sheet.getRow(rowIndex);
        Cell cell = row.createCell(cellNumStart + cellMapper.size());
        Font font = workbook.createFont();
        font.setColor(RED.index);
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        cell.setCellStyle(style);
        cell.setCellValue(message);
    }

    private String getValue(Cell cell) {
        switch (cell.getCellTypeEnum()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    if (cell.getDateCellValue() == null) return null;
                    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cell.getDateCellValue());
                }

                String numString = new DecimalFormat("#.00").format(cell.getNumericCellValue());
                while (numString.endsWith("0")) {
                    numString = StringUtils.removeEnd(numString, "0");
                }
                return StringUtils.removeEnd(numString, ".");
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return getValue(evaluator.evaluateInCell(cell));
            case BLANK:
                return null;
            default:
                return null;
        }
    }


    public void proceed(int rowIndex) {
        proceedRowsIndex.add(rowIndex);
    }

    public void write(OutputStream outputStream) throws IOException {
        Sheet sheet = workbook.getSheetAt(0);
        for (Integer rowIndex : proceedRowsIndex) {
            sheet.shiftRows(Math.min(sheet.getLastRowNum(), rowIndex + 1), sheet.getLastRowNum(), -1);
        }

        workbook.write(outputStream);
        workbook.close();
    }
}
