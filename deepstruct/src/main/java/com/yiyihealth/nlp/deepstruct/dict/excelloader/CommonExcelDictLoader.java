package com.yiyihealth.nlp.deepstruct.dict.excelloader;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.yiyihealth.nlp.deepstruct.dict.Word;

public class CommonExcelDictLoader {

	/**
	 * 
	 * @param file Excel file
	 * @param rowStart
	 * @param colNum
	 * @param rowParser
	 * @return
	 */
	public ArrayList<Word> loadDict(String file, int rowStart, int colNum, RowParser rowParser){
		ArrayList<Word> results = new ArrayList<Word>();
		try {
			File excelFile = new File(file); // 创建文件对象
			FileInputStream is = new FileInputStream(excelFile); // 文件流
			// 创建对Excel工作簿文件的引用
			Workbook wbs = WorkbookFactory.create(excelFile);
			// int sheetCount = workbook.getNumberOfSheets(); //Sheet的数量
			Sheet dictSheet = wbs.getSheetAt(0);
			for (int r = rowStart; r < dictSheet.getLastRowNum()+1; r++) {// 循环该子sheetrow
				Row row = dictSheet.getRow(r);
				if (row == null) {
					continue;
				}
				String[] rowValues = new String[colNum];
				for (int c = 0; c < colNum; c++) {// 循环该子sheet行对应的单元格项
					Cell cell = row.getCell(c);
					String value = null;
					boolean error = false;
					if (cell != null) {
						switch (cell.getCellType()) {
						case Cell.CELL_TYPE_STRING:
							value = cell.getStringCellValue().trim();
							break;
						case Cell.CELL_TYPE_BLANK:
							value = "";
							break;
						case Cell.CELL_TYPE_NUMERIC:
							value = "" + cell.getNumericCellValue();
							break;
						default:
							error = true;
							System.out.print("Error: unknown cell type: " + cell.getCellType());
							break;
						}
						if (!error) {
							rowValues[c] = value;
						}
					}
				}
				Word word = rowParser.parseRow(rowValues);
				if (word != null) {
					results.add(word);
				}
			}
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return results;
	}
	
	public interface RowParser {
		
		public Word parseRow(String[] rowValues);
		
	}
	
}
