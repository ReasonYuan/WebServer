package com.yiyihealth.ai.dsmain.medicine.wx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.codehaus.plexus.util.FileUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.yiyihealth.ds.esearch.fol.Atom;
import com.yiyihealth.ds.esearch.fol.InherenceEngine;

public class ExcelByUser {
	private static HSSFWorkbook workbook = null;
	private static HashMap<String, Sheet> sheetlist = new HashMap<String, Sheet>();
	private static final String SHEET_NAME1 = "现病史用药";
	private static final String SHEET_NAME2 = "在院用药";
	private static final String SHEET_NAME3 = "出院用药";
	
	private static final String HISTORY_OF_PRESENT_ILLNESS= "history_of_present_illness";//现病史
	private static final String ADMISSION_DETAIL = "admission_detail";//入院情况
	private static final String AFTER_TREATMENT = "after_treatment";//诊治情况
	private static final String DIAGNOSIS_TREATMENT = "diagnosis_treatment";//诊疗经过
	private static final String DISCHARGE_PRESCRIBED= "discharge_prescribed";//出院医嘱
	public static JSONObject jiLiangConfigJSON = null;
	public static JSONObject MedicineAnotherNameConfigJSON = null;
	private static  short[] colors = {HSSFColor.LIGHT_GREEN.index, HSSFColor.LIGHT_BLUE.index,HSSFColor.LIGHT_YELLOW.index,HSSFColor.LIGHT_ORANGE.index,HSSFColor.YELLOW.index,HSSFColor.BROWN.index};
	 /** 
     * 创建新excel. 
     * @param fileDir  excel的路径 
     * @param sheetName 要创建的表格索引 
     * @param titleRow excel的第一行即表格头 
	 * @throws IOException 
     */  
    public static void createExcel(String fileDir,String excelName,String[] sheetNames,ArrayList<String> titleRow) throws IOException{ 
    	if(!fileExist(fileDir)){
    		File file = new File(fileDir);
    		file.mkdirs();
    	}
    	
    	if(!fileExist(fileDir+"/" + excelName)){
    		File file = new File(fileDir);
    		file.createNewFile();
    	}
        //创建workbook  
        workbook = new HSSFWorkbook();  
         
        createSheets(sheetNames,titleRow,fileDir+"/" + excelName);
  
    }  
    
    public static Sheet createOneSheet(String sheetName,ArrayList<String> titleRow,String path){
    	  //添加Worksheet（不添加sheet时生成的xls文件打开时会报错)  
        Sheet sheet1 = workbook.createSheet(sheetName);  
        //新建文件  
        FileOutputStream out = null;  
        try {  
            //添加表头  
            Row row = workbook.getSheet(sheetName).createRow(0);    //创建第一行   
            row.setHeight((short) (500));
            HSSFCellStyle style = workbook.createCellStyle();
            style.setFillForegroundColor(HSSFColor.LIGHT_YELLOW.index);
            style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
            for(int i = 0;i < titleRow.size();i++){  
                Cell cell = row.createCell(i);  
                cell.setCellValue(titleRow.get(i)); 
                if( i > 5){
                	 cell.setCellStyle(style);
                }
            }  
            
            out = new FileOutputStream(path);  
            workbook.write(out);  
        } catch (Exception e) {  
            e.printStackTrace();  
        } finally {    
            try {    
                out.close();    
            } catch (IOException e) {    
                e.printStackTrace();  
            }    
        } 
        return sheet1;
    }
    /**
     * 创建sheets
     * @param sheetNames
     */
    @SuppressWarnings("unchecked")
	public static void createSheets(String[] sheetNames,ArrayList<String> titleRow,String path){
    	for (int i = 0; i < sheetNames.length; i++) {
    		ArrayList<String> tmpRows = (ArrayList<String>) titleRow.clone();
//    		if(i == 0){
//    			tmpRows.add("好转");
//    			tmpRows.add("无好转");
//    		}
    		Sheet sheet = createOneSheet(sheetNames[i], tmpRows, path);
    		sheetlist.put(sheetNames[i], sheet);
		}
    }
    
    
    /** 
     * 判断文件是否存在. 
     * @param fileDir  文件路径 
     * @return 
     */  
    public static boolean fileExist(String fileDir){  
         boolean flag = false;  
         File file = new File(fileDir);  
         flag = file.exists();  
         return flag;  
    }  
    
    public static HSSFCellStyle createExcelColor(){
//    	HSSFPalette customPalette = workbook.getCustomPalette();  
//        customPalette.setColorAtIndex(HSSFColor.ORANGE.index, (byte) getRandomInteger(255), (byte) getRandomInteger(255), (byte) getRandomInteger(255)); 
    	 HSSFCellStyle style = workbook.createCellStyle();
         style.setFillForegroundColor(colors[getRandomInteger(colors.length)]);
         style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
         return style;
    }
    
    public static int getRandomInteger(int max){
    	if (max > 255) {
    		max = 255;
		}
    	if(max < 0){
    		max = 0;
    	}
    	return (int)(Math.random()*max);
    }
    
    /**
     * 将药物统计数据写入excel表中，除tittle以外的数据，即从index = 1 开始。
     * @param medicineUsers
     */
    public  static void writeDateToExcel(String evidenceFile,User medicineUsers,String sheetName,String path,String projectDir,boolean existMoreCell,HSSFCellStyle style){
        FileOutputStream out = null;  
    		try {  
    			int currentSize  = workbook.getSheet(sheetName).getLastRowNum();
    			User mUser = medicineUsers;
    			ArrayList<Medicine> medicineList = mUser.getMedicineList();
                Row row = workbook.getSheet(sheetName).createRow(currentSize+1);  
                row.setHeight((short) (500));
                
                ArrayList<Cell> tittleCellList = getTittleCell(sheetName);
                int tittlecellSize = tittleCellList.size();
                for(int m = 0;m < tittlecellSize;m++){  
                    Cell cell = row.createCell(m);  
                    switch (m) {
                    case 0:
                    	cell.setCellValue(mUser.getAdminNum()); 
                    	if(existMoreCell){
                    		cell.setCellStyle(style);
                    	}
                    	break;
					case 1:
						cell.setCellValue(mUser.getRecordNum()); 
						if(existMoreCell){
                    		cell.setCellStyle(style);
                    	}
						break;
					case 2:
						cell.setCellValue(mUser.getAdmisionDate()); 
						break;
					case 3:
						cell.setCellValue(mUser.getOutAdmisionDate()); 
						break;
					case 4:
						cell.setCellValue(mUser.getZhengzhuang()); 
						break;
					case 5:
						cell.setCellValue(mUser.getZhenduan()); 
						break;
					default:
						break;
					}
                }  
                int userMedicineSize = medicineList.size();
                for(int n = 0 ; n < userMedicineSize ;n++){
                	Medicine medicine = medicineList.get(n);
                	String medicineAnchor = medicine.getAnchor();
                	if(sheetName.equals(SHEET_NAME1)){
                		if(medicineAnchor.equals(HISTORY_OF_PRESENT_ILLNESS) || medicineAnchor.equals(ADMISSION_DETAIL)){
                			//现病史sheet中 只需要 现病史 和 入院情况 锚点中的数据
                		}else{
                			continue;
                		}
                	}
                	
                	if(sheetName.equals(SHEET_NAME2)){
                		if(medicineAnchor.equals(AFTER_TREATMENT) || medicineAnchor.equals(DIAGNOSIS_TREATMENT)){
                			//在院用药sheet中 只需要 诊治情况 和 诊治经过 锚点中的数据
                		}else{
                			continue;
                		}
                	}
                	
                	if(sheetName.equals(SHEET_NAME3)){
                		if(medicineAnchor.equals(DISCHARGE_PRESCRIBED)){
                			//出院用药sheet中 只需要 出院医嘱 锚点中的数据
                		}else{
                			continue;
                		}
                	}
                	
                	String medicineName = medicine.getMedicineName();
                	medicineName = compareAnotherName(medicineName);
                	for(int k = 0;k < tittleCellList.size();k++){ 
                		 Cell cell = tittleCellList.get(k);  
                		 String cellValue = cell.getStringCellValue();
                		if(cellValue.equals(medicineName) && !cellValue.equals("")){
                			int cellcolumIndex = cell.getColumnIndex();
                			row.getCell(cellcolumIndex).setCellValue("Y");
                			row.getCell(cellcolumIndex - 1).setCellValue(medicine.getUserMedicineTime());
                			row.getCell(cellcolumIndex + 1).setCellValue(medicine.getJiLiang());
                			row.getCell(cellcolumIndex + 2).setCellValue(comparePinci(medicine.getPinCi()));
                			if(cellValue.equals("类克")){
                				row.getCell(cellcolumIndex + 3).setCellValue(medicine.getSpecialYiJu());
                				row.getCell(cellcolumIndex + 4).setCellValue(medicine.getGodResult());
                				row.getCell(cellcolumIndex + 5).setCellValue(medicine.getBadResult());
                			}else{
                				row.getCell(cellcolumIndex + 3).setCellValue(medicine.getGodResult());
                				row.getCell(cellcolumIndex + 4).setCellValue(medicine.getBadResult());
                			}
                			
                			String jiliangstr = medicine.getJiLiang();
                			String pincistr = medicine.getPinCi();
                			if(jiliangstr.equals("")){
                				jiliangstr = "*";
                			}
                			if(pincistr.equals("")){
                				pincistr = "*";
                			}
                			String YiJu ="     |"+evidenceFile+"|-----|" + medicineName +"|-----|" + jiliangstr +"|-----|" + pincistr + "|-----|" + medicine.getSpecialYiJu() + "|-----|" + sheetName;
                			writeYiJuToFile(projectDir + "/YiJuResult",YiJu,"drugYiJu.txt");
                		}
                	}
                }
                
                out = new FileOutputStream(path);  
                workbook.write(out);  
            } catch (Exception e) {  
                e.printStackTrace();  
            } finally {    
                try {    
                    out.close();    
                } catch (IOException e) {    
                    e.printStackTrace();  
                }    
            }
    }
    
    /**
     * 获取excel表中tittle的cell
     * @param sheetName
     */
    public static ArrayList<Cell> getTittleCell(String sheetName){
    	ArrayList<Cell> tittleCellList = new ArrayList<Cell>();
    	Iterator<Cell> tittleCells = workbook.getSheet(sheetName).getRow(0).cellIterator();
    	while(tittleCells.hasNext()){
    		Cell cell = tittleCells.next();
    		tittleCellList.add(cell);
    	}
    	return tittleCellList;
    }
    
    /**
     * 创建excel表头tittle数组
     * @return
     */
    public static ArrayList<String> createExcelMedicineTittleArrary(){
		ArrayList<String> tittlelist = new ArrayList<String>();
		tittlelist.add("住院号");
		tittlelist.add("病历号");
		tittlelist.add("入院日期");
		tittlelist.add("出院日期");
		tittlelist.add("症状");
		tittlelist.add("诊断");
		String[] aa = {"得宝松","地塞米松","强的松","美卓乐","甲强龙","激素","阿司匹林","芬必得","散利痛",
						"扶他林","英太青","戴芬","乐松","西乐葆","莫比可","安康信","吲哚美辛",
						"双氯芬酸","尼美舒利","NSAID","柳氮","甲氨蝶呤","反应停","爱若华","雷公藤","帕夫林",
						"其他DMARDS","益赛普","恩利","类克","修美乐","强克","阿达木","其他肿瘤坏死因子抗体",
						"肿瘤坏死因子受体融合蛋白","异烟肼","利福平","利福喷丁","乙胺丁醇"
		};
		for(int i =0;i< aa.length;i++){
			MedicineExcelTittleArray me = new MedicineExcelTittleArray();
			me.setMedicineName(aa[i]);
			tittlelist.addAll(me.getExcelStringArray());
		}
		return tittlelist;
	}
    
    private static String removeMedicineNameWithUnit(String medicineName){
    	String [] units = {"粒","片","针","颗粒"};
    	for (int i = 0; i < units.length; i++) {
    		if(medicineName.endsWith(units[i])){
    			return medicineName.substring(0, medicineName.length() - units[i].length());
        	}
		}
    	return medicineName;
    }
    
    /**
     * 比较药物的别名 替换为excel表中的名字
     * 暂时直接比较 可能有更好的解决方式
     */
    public static String compareAnotherName(String medicineName){
    	medicineName = removeMedicineNameWithUnit(medicineName);
    	
    	JSONObject mJsonObject = MedicineAnotherNameConfigJSON;
    	if(mJsonObject != null){
    		JSONArray mArray = mJsonObject.getJSONArray("anotherName");
    		for(int i = 0 ; i < mArray.size();i++){
    			JSONObject object = mArray.getJSONObject(i);
    			String userName = object.getString("userStr");
    			JSONArray otherNameArray = object.getJSONArray("userWords");
    			for (int j = 0; j < otherNameArray.size(); j++) {
					String name = otherNameArray.getString(j);
					if(name.contains("*")){
						int index = name.indexOf("*");
						String startStr = name.substring(0, index);
						String endStr = name.substring(index + 1, name.length());
						if(medicineName.contains(startStr) && medicineName.contains(endStr)){
							return userName;
						}
					}else{
						if(name.equals(medicineName)){
							return userName;
						}
					}
					
				}
    		}
    	}
    	
		return medicineName;
    }
    
    public static String comparePinci(String pinci){
    	JSONObject mJsonObject = jiLiangConfigJSON;
    	if(mJsonObject != null){
    		Set<String> iterator = mJsonObject.keySet();
    		for(String key : iterator){
    			JSONObject object = mJsonObject.getJSONObject(key);
    			String userUnit =  object.getString("userStr");
    			JSONArray mArray = object.getJSONArray("userWords");
    			for(int i = 0;i < mArray.size();i++){
    				String tmp = mArray.getString(i);
    				if(pinci.equals(tmp)){
    					return userUnit;
    				}
    			}
    		}
    	}
		return pinci;
    }
    
    private static Atom[] sortAtomsByPos(Atom[] atoms){
    	ArrayList<Atom> arrayList = new ArrayList<Atom>();
    	for (int i = 0; i < atoms.length; i++) {
			arrayList.add(atoms[i]);
		}
    	arrayList.sort(new Comparator<Atom>() {
			public int compare(Atom o1, Atom o2) {
				try {
					return Integer.parseInt(o1.getParams().get(2).toString()) - Integer.parseInt(o2.getParams().get(2).toString());
				} catch (Exception e) {
					throw new RuntimeException("单词原子的第二位必须是数字!");
				}
			}
		});
    	Atom[] newAtoms = new Atom[atoms.length];
    	arrayList.toArray(newAtoms);
    	return newAtoms;
    }
    
    public static void getMedicine(Atom[] zhengzhuang,InherenceEngine engine,String evidenceFile,Atom[] patientName,Atom[] adminNum,Atom[] recordNum,Atom[] adminDate,Atom[] outAdminDate,Atom[] drugs,Atom[] jiliang,Atom[] pinci,Atom[] words,Atom[] HaveDrugBadEffects,Atom[] HaveDrugGoodEffects) throws IOException, CloneNotSupportedException, ParseException{
    	words = sortAtomsByPos(words);
    	User mUser = new User();
    	ArrayList<Medicine> medicineList = new ArrayList<Medicine>();
    	String adminDateStr = "";
    	String outAdminDateStr = "";
    	String patientNameStr = "";
    	String adminNumStr = "";
    	String recordNumStr = "";
    	if(adminDate.length > 0 && adminDate[0].getParams().size() > 1){
    		 adminDateStr = adminDate[0].getParams().get(1).toString();
    	}
    	
    	if(outAdminDate.length > 0 && outAdminDate[0].getParams().size() > 1){
    		outAdminDateStr = outAdminDate[0].getParams().get(1).toString();
    	}
    	
    	if(patientName.length > 0 && patientName[0].getParams().size() > 1){
    		patientNameStr = patientName[0].getParams().get(1).toString();
    	}
    	
    	if(adminNum.length > 0 && adminNum[0].getParams().size() > 1){
    		adminNumStr = adminNum[0].getParams().get(1).toString();
    	}
    	
    	if(recordNum.length > 0 && recordNum[0].getParams().size() > 1){
    		recordNumStr = recordNum[0].getParams().get(1).toString();
    	}
		
    	for(int i =0;i<drugs.length;i++){
    		Medicine medicine = new Medicine();
    		String name = drugs[i].getParams().get(0).toString();
    		String anchorStr = drugs[i].getParams().get(4).toString();
    		int pos = Integer.parseInt(drugs[i].getParams().get(2).toString());
    		String userMedicineTime = drugs[i].getParams().get(3).toString();
    		
    		if(engine.getFolNetwork().getMemDB().getEWord(pos).getTaggedActualDates().size() > 0){
    				String str = engine.getFolNetwork().getMemDB().getEWord(pos).getTaggedActualDate(userMedicineTime);
    				userMedicineTime =str;
    		}
    		
    		if(anchorStr.equals("after_treatment") || anchorStr.equals("diagnosis_treatment")){
    			if(!userMedicineTime.equals("") && userMedicineTime.contains(outAdminDateStr)){
    				userMedicineTime = timeFormat(adminDateStr);
    			}
    		}
    		
    		String jiliangPos =  drugs[i].getParams().get(2).toString();
    		String jiliangStr = findJiLiang(jiliangPos, jiliang, words,true);
    		String pinciStr = findJiLiang(jiliangPos, pinci, words,false);
    		
    		String goodEffects = findEffects(jiliangPos, HaveDrugGoodEffects, words);
    		String badEffects = findEffects(jiliangPos, HaveDrugBadEffects, words);
    		
    		
    		medicine.setUserMedicineTime(userMedicineTime);
    		medicine.setMedicineName(name);
    		if(!jiliangStr.equals("")){
    			medicine.setJiLiang(jiliangStr);
    		}
    		
    		if(!pinciStr.equals("")){
    			medicine.setPinCi(pinciStr);
    		}
    		
			medicine.setAnchor(anchorStr);
			medicine.setGodResult(goodEffects);
			medicine.setBadResult(badEffects);
			String specialYiJu = findSpecialYiju(jiliangPos, words);
			medicine.setSpecialYiJu(specialYiJu);
    		
    		medicineList.add(medicine);
    	}
    	mUser.setPatientName(patientNameStr);
    	mUser.setRecordNum(recordNumStr);
    	mUser.setAdminNum(adminNumStr);
    	mUser.setAdmisionDate(adminDateStr);
    	mUser.setOutAdmisionDate(outAdminDateStr);
    	mUser.setMedicineList(medicineList);
    	String sheet2Str = "";
    	String sheet3Str = "";
    	if(evidenceFile.contains("RY")){
    		String zhengzhuangStr = getZhengZhuangStr(zhengzhuang);
    		mUser.setZhengzhuang(zhengzhuangStr);
    	}else{
    		Atom[] sheet2 = getZhengzhuang(zhengzhuang, 2);
    		Atom[] sheet3 = getZhengzhuang(zhengzhuang, 3);
    		sheet2Str = getZhengZhuangStr(sheet2);
    		sheet3Str = getZhengZhuangStr(sheet3);
    		
    	}
    	
    	
    	
    	System.out.println("-----User-------"+mUser.toJson().toString());
    	
    	System.out.println("-------开始向Excel中写入数据---------");
    	
    	JSONObject config = JSONObject.parseObject(FileUtils.fileRead(new File("./conf/config.json")));
		final String projectDir = config.getString("projectDir");
		
		HSSFCellStyle style = createExcelColor();
		if(evidenceFile.contains("RY")){
			ArrayList<User> users = recombinizeUserDataForOnlyOne(mUser);
			boolean existMoreCell = false;
			if(users.size() > 1){
				existMoreCell = true;
			}
			for (int j = 0; j < users.size(); j++) {
				ExcelByUser.writeDateToExcel(evidenceFile,users.get(j), SHEET_NAME1, projectDir + "/excelResult/medicine.xls",projectDir,existMoreCell,style);
			}
			
		}
		
		if(evidenceFile.contains("CY")){
			ArrayList<User> users = recombinizeUserForSheet2AndSheet3(mUser);
			User userForSheet2 = users.get(0);
	    	User userForSheet3 = users.get(1);
	    	
	    	ArrayList<User> usersForSheet2 = recombinizeUserDataForOnlyOne(userForSheet2);
	    	ArrayList<User> usersForSheet3 = recombinizeUserDataForOnlyOne(userForSheet3);
			
	    	for(int m = 0 ;m < usersForSheet2.size();m++){
	    		User user = usersForSheet2.get(m);
//	    		if(user.getMedicineList().size() > 0){
	    			boolean existMoreCell = false;
	    			if(usersForSheet2.size() > 1){
	    				existMoreCell = true;
	    			}
	    			user.setZhengzhuang(sheet2Str);
		    		ExcelByUser.writeDateToExcel(evidenceFile,user, SHEET_NAME2, projectDir + "/excelResult/medicine.xls",projectDir,existMoreCell,style);
//		    	}
	    	}
	    	
	    	for(int k = 0 ;k < usersForSheet3.size();k++){
	    		User user = usersForSheet3.get(k);
//	    		if(user.getMedicineList().size() > 0){
	    			boolean existMoreCell = false;
	    			if(usersForSheet3.size() > 1){
	    				existMoreCell = true;
	    			}
	    			user.setZhengzhuang(sheet3Str);
		    		ExcelByUser.writeDateToExcel(evidenceFile,user, SHEET_NAME3, projectDir + "/excelResult/medicine.xls",projectDir,existMoreCell,style);
//		    	}
	    	}
	    	
//			for (int j = 0; j < users.size(); j++) {
//				User user = users.get(j);
//				ArrayList<User> otherUsers = recombinizeUserData(user);
//				User userForSheet2 = otherUsers.get(0);
//		    	User userForSheet3 = otherUsers.get(1);
//		    	if(userForSheet2.getMedicineList().size() > 0){
//		    		ExcelByUser.writeDateToExcel(evidenceFile,userForSheet2, SHEET_NAME2, projectDir + "/excelResult/medicine.xls",projectDir,existMoreCell,style);
//		    	}
//		    	
//		    	if(userForSheet3.getMedicineList().size() > 0){
//		    		ExcelByUser.writeDateToExcel(evidenceFile,userForSheet3, SHEET_NAME3, projectDir + "/excelResult/medicine.xls",projectDir,existMoreCell,style);
//		    	}
//		    	
//			}
		}
		
		System.out.println("-------结束向Excel中写入数据---------");
    }
    
    public static ArrayList<User> recombinizeUserForSheet2AndSheet3(User mUser) throws CloneNotSupportedException{
    	ArrayList<Medicine> mList = mUser.getMedicineList();
    	ArrayList<Medicine> mListForSheet2 = new ArrayList<Medicine>();
    	ArrayList<Medicine> mListForSheet3 = new ArrayList<Medicine>();
    	ArrayList<User> users = new ArrayList<User>();
    	User  userforSheet2 = (User) mUser.clone();
    	User  userforSheet3 = (User) mUser.clone();
    	for(int i = 0;i  < mList.size();i++){
    		Medicine medicine = mList.get(i);
    		String anchor = medicine.getAnchor();
    		if(anchor.equals(AFTER_TREATMENT) || anchor.equals(DIAGNOSIS_TREATMENT)){
    			//在院用药sheet中 只需要 诊治情况 和 诊治经过 锚点中的数据
    			mListForSheet2.add(medicine);
    		}
    		if(anchor.equals(DISCHARGE_PRESCRIBED)){
    			mListForSheet3.add(medicine);
    		}
    	}
    	userforSheet2.setMedicineList(mListForSheet2);
    	userforSheet3.setMedicineList(mListForSheet3);
    	users.add(userforSheet2);
    	users.add(userforSheet3);
    	return users;
    }
    
    
	public static String findSpecialYiju(String jiliangPos, Atom[] atoms) {
		int jiliangIntPos = Integer.parseInt(jiliangPos);
		final int preOffset = -10;
		final int nextOffset = 15;
		int startSearchPos = jiliangIntPos + preOffset;
		int endSearchPos = jiliangIntPos + nextOffset;
		String tmpStr = "";
		if (startSearchPos < 0) {
			startSearchPos = 0;
		}
		if (endSearchPos > atoms.length) {
			endSearchPos = atoms.length;
		}

		for (int i = startSearchPos; i < endSearchPos; i++) {
			tmpStr += (atoms[i].getParams().get(0).toString()) ;
		}
		tmpStr += "--|--";
		for (int i = startSearchPos; i < endSearchPos; i++) {
			tmpStr += (atoms[i].getParams().get(0).toString() + "/" + atoms[i].getParams().get(1).toString() + "|") ;
		}
		
		return tmpStr;
	}
    
    
    
    /**
     * 重组User数据 为出院所用 sheet2 sheet3
     * @param mUser
     * @return
     * @throws CloneNotSupportedException
     */
    public static ArrayList<User> recombinizeUserData(User mUser) throws CloneNotSupportedException{
    	ArrayList<User> users = new ArrayList<User>();
    	ArrayList<Medicine> medicineList = mUser.getMedicineList();
    	ArrayList<Medicine> listForSheet2 = new ArrayList<Medicine>();
    	ArrayList<Medicine> listForSheet3 = new ArrayList<Medicine>();
    	User userForSheet2 = (User) mUser.clone();
    	User userForSheet3 = (User) mUser.clone();
    	int size = medicineList.size();
    	for (int i = 0; i < size; i++) {
			Medicine medicine = medicineList.get(i);
			String anchor = medicine.getAnchor();
			if (anchor.equals(AFTER_TREATMENT) || anchor.equals(DIAGNOSIS_TREATMENT)) {
				listForSheet2.add(medicine);
			}
			if(anchor.equals(DISCHARGE_PRESCRIBED)){
				listForSheet3.add(medicine);
			}
		}
    	userForSheet2.setMedicineList(listForSheet2);
    	userForSheet3.setMedicineList(listForSheet3);
    	users.add(userForSheet2);
    	users.add(userForSheet3);
//    	System.out.println("------------"+ userForSheet2.toJson().toString());
//    	System.out.println("------------"+ userForSheet3.toJson().toString());
    	return users;
    }
    
    /**
     * 找到效果的str
     * @param jiliangPos
     * @param effects
     * @param words
     * @return
     */
    public static String  findEffects(String jiliangPos,Atom[] effects,Atom[] words){
    	for(int i = 0;i < effects.length;i++){
    		String pos = effects[i].getParams().get(0).toString();
    		String effectsStr = "";
    		if(pos.equals(jiliangPos)){
    			String pos1 = effects[i].getParams().get(1).toString();
    			String pos2 = effects[i].getParams().get(2).toString();
    			String pos3 = effects[i].getParams().get(3).toString();
    			String word1 = getPosWord(pos1, words);
    			String word2 = getPosWord(pos2, words);
    			String word3 = getPosWord(pos3, words);
    			effectsStr = word1 + word2 + word3;
    			return effectsStr;
    		}
    	}
    	return "";
    }
    
    /**
     * 找剂量
     * @param jiliangPos
     * @param jiliang
     * @param words
     * @return
     */
    public static String findJiLiang(String jiliangPos,Atom[] jiliang,Atom[] words,boolean isFindJiLiang){
    	for(int i = 0;i < jiliang.length;i++){
    		String pos = jiliang[i].getParams().get(0).toString();
    		String jiliangStr = "";
    		if(pos.equals(jiliangPos)){
    			String pos1 = jiliang[i].getParams().get(1).toString();
    			String pos2 = jiliang[i].getParams().get(2).toString();
    			String word1 = getPosWord(pos1, words);
    			String word2 = getPosWord(pos2, words);
    			if(isFindJiLiang){
    				if(!word1.equals("")){
    					try {
            				Float.parseFloat(word1);
        				} catch (Exception e) {
        					word1 = "";
        				}
            			
    				}
    			}
    			
    			jiliangStr = word1 + word2;
    			
    			return jiliangStr;
    		}
    	}
    	return "";
    }
    
    /**
     * 在words中找到对应pos的文字
     * @param pos
     * @param words
     * @return
     */
    public static String getPosWord(String pos,Atom[] words){
    	for(int i = 0;i < words.length;i++){
    		String wordPos = words[i].getParams().get(2).toString();
    		if(wordPos.equals(pos)){
    			return words[i].getParams().get(0).toString();
    		}
    	}
    	return "";
    }
    
    /**
     * 在words中找到对应pos的word
     * @param pos
     * @param words
     * @return
     */
    public static Atom getPosWordAtom(String pos,Atom[] words){
    	for(int i = 0;i < words.length;i++){
    		String wordPos = words[i].getParams().get(2).toString();
    		if(wordPos.equals(pos)){
    			return words[i];
    		}
    	}
    	return null;
    }
    
    /**
     * 将药物的依据写入文件中
     * @param path
     * @param str
     * @param fileName
     * @throws IOException
     */
    public static void writeYiJuToFile(String path,String str,String fileName) throws IOException{
    	if(!fileExist(path)){
    		File file = new File(path);
    		file.mkdirs();
    	}
    	
    	if(!fileExist(path+"/" + fileName)){
    		File file = new File(path+"/" + fileName);
    		file.createNewFile();
    	}
    	
    	FileWriter bw =  new FileWriter(path+"/" + fileName, true);
		bw.write(str + "\r\n\n");
		bw.flush();
		bw.close();
    }
    
    /**
     * 获取剂量转换配置json文件
     * @param path
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public static JSONObject getJiLiangDateConfigFile(String path) throws IOException, JSONException{
    	File file = new File(path);
    	long len = file.length();
    	byte[] bytes = new byte[(int)len];
    	FileInputStream in = new FileInputStream(file);
    	int r = in.read(bytes);
    	in.close();
    	if(r != len){
    		throw new IOException("读取文件不正确");
    	}
    	String ss = new String(bytes);
    	JSONObject mJsonObject = JSONObject.parseObject(ss);
    	System.out.println("--mJsonObject---"+ mJsonObject.toString());
    	return mJsonObject;
    }
    
    /**
     * 将user重组为药品不重复的user list
     * @param mUser
     * @return
     * @throws CloneNotSupportedException
     */
    public static ArrayList<User> recombinizeUserDataForOnlyOne(User mUser) throws CloneNotSupportedException {
		ArrayList<Medicine> medicinelis = mUser.getMedicineList();
		HashMap<Integer, ArrayList<Medicine>> hashMap = createBigestArrayList(medicinelis);
		int size = medicinelis.size();
		if(size != 0){
			for (int i = 0; i < size; i++) {
				Medicine medicine = medicinelis.get(i);
				hashMap = saveMedicineToList(hashMap,medicine);
			}
			HashMap<Integer, ArrayList<Medicine>> newhashMap = new HashMap<Integer, ArrayList<Medicine>>();
			for (int i = 0; i < hashMap.size(); i++) {
				int listSize = hashMap.get(i).size();
				if(listSize != 0 ){
					newhashMap.put(i, hashMap.get(i));
				}
			}
			ArrayList<User> users  =  recombinizeUser(newhashMap,mUser);
			return users;
		}else{
			ArrayList<User> users = new ArrayList<User>();
			users.add(mUser);
			return users;
		}
	}
    
    /**
     * 将数组重组为多个user 针对药品里面有相同的药
     * @param newhashMap
     * @param mUser
     * @return
     * @throws CloneNotSupportedException
     */
    public static ArrayList<User> recombinizeUser(HashMap<Integer, ArrayList<Medicine>> newhashMap,User mUser) throws CloneNotSupportedException {
    	ArrayList<User> users = new ArrayList<User>();
		for(int i = 0 ;i < newhashMap.size();i++){
			ArrayList<Medicine> medicines = newhashMap.get(i);
			User tmpUser = (User) mUser.clone();
			tmpUser.setMedicineList(medicines);
			users.add(tmpUser);
		}
		return users;
	}
    
    /**
     * 比较药物是否已经存在list中
     * 
     * @param medicineList
     * @return
     */
    public static boolean compareMedicineExistInList(String cpMedicineName,ArrayList<Medicine> medicineList){
    	for (int i = 0; i < medicineList.size(); i++) {
			Medicine medicine = medicineList.get(i);
			String medicineName = medicine.getMedicineName();
			if(cpMedicineName.equals(medicineName)){
				return true;
			}
		}
    	return false;
    }
    
    /**
     * 根据药品列表，创建可能出现相同药品最多的数组。例如：["强的松"，"强的松"，"强的松"，"强的松"]
     * 则需要创建4个数组 分别装
     * @param medicinelis
     */
    public static HashMap<Integer, ArrayList<Medicine>> createBigestArrayList(ArrayList<Medicine> medicinelis){
    	HashMap<Integer, ArrayList<Medicine>> hashMap = new HashMap<Integer, ArrayList<Medicine>>();
    	for(int i = 0 ;i < medicinelis.size();i++){
    		hashMap.put(i, new ArrayList<Medicine>());
    	}
    	
    	return hashMap;
    }
    
    /**
     * 将重复的药物 分组存入hashmap
     * @param hashMap
     * @param medicine
     * @return
     */
    public static HashMap<Integer, ArrayList<Medicine>> saveMedicineToList(HashMap<Integer, ArrayList<Medicine>> hashMap,Medicine medicine){
    	String tmpName = medicine.getMedicineName();
    	for(int i = 0; i < hashMap.size(); i++){
    		ArrayList<Medicine> list = hashMap.get(i);
    		boolean find = false;
    		if(list.size() > 0){
    			for(int m = 0;m < list.size();m++){
        			String name = list.get(m).getMedicineName();
        			if(tmpName.equals(name)){
        				find = true;
        				break;
        			}
        		}
    			if(!find){
    				hashMap.get(i).add(medicine);
    				return hashMap;
        		}
    		}else{
    			hashMap.get(i).add(medicine);
    			return hashMap;
    		}
    	}
    	return hashMap;
    }
    
	/***
	 * 将字符串转化为时间
	 * @param timeStr
	 * @return
	 * @throws ParseException
	 */
	public static String timeFormat(String timeStr) throws ParseException{
		SimpleDateFormat normalFormatter = new SimpleDateFormat("yyyy-MM-dd");
		Date date = normalFormatter.parse(timeStr);
		SimpleDateFormat normalFormatter1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String formattedTime = normalFormatter1.format(date);
		return formattedTime;
	}
	
	/**
	 * 将用药数组中已经存在停用的药 移除
	 * @param allDrugs
	 * @param stopDrug
	 */
	public static Atom[] removeStopDrug(Atom[] allDrugs,Atom[] stopDrug){
		List<Atom> tmplist = (List<Atom>) Arrays.asList(allDrugs);
		ArrayList<Atom> list = new ArrayList<Atom>(tmplist);
		for(int i = 0;i < stopDrug.length;i++){
			String stopDrugPos = stopDrug[i].getParams().get(2).toString();
			for(int m = 0;m < list.size();m++ ){
				String allDrugPos = list.get(m).getParams().get(2).toString();
				if (stopDrugPos.equals(allDrugPos)) {
					list.remove(m);
					break;
				}
			}
		}
		
		Atom[] newAllDrug = new Atom[list.size()];
		list.toArray(newAllDrug);
		return newAllDrug;
	}
	
	public static void initData(String projectDir) throws IOException, JSONException{
		ExcelByUser.jiLiangConfigJSON = getJiLiangDateConfigFile(projectDir + "/JsonConfig/JiLiangDateConfig.json");
		ExcelByUser.MedicineAnotherNameConfigJSON = getJiLiangDateConfigFile(projectDir + "/JsonConfig/MedicineAnotherNameConfig.json");
		String YiJuTittle = "     |"+"文件名"+"|     |" + "药名" +"|     |" +"剂量" +"|     |" + "频次" + "|     |" + "依据" + "|     |" + "所属表" + "|";
		writeYiJuToFile(projectDir + "/YiJuResult",YiJuTittle,"drugYiJu.txt");
		String[] sheets = {"现病史用药","在院用药","出院用药"};
		createExcel(projectDir + "/excelResult","medicine.xls", sheets, ExcelByUser.createExcelMedicineTittleArrary());
	}
	
	/**
	 * 将症状拆分开
	 * @param zhengzhuangs
	 * @param sheetIndex 哪一个sheet的index
	 */
	public static Atom[] getZhengzhuang(Atom[] zhengzhuangs,int sheetIndex){
		ArrayList<Atom> list = new ArrayList<>();
		for (int i = 0; i < zhengzhuangs.length; i++) {
			Atom atom = zhengzhuangs[i];
			if(sheetIndex == 0){
				if(atom.getParams().get(9).equals("history_of_present_illness") || atom.getParams().get(9).equals("admission_detail")){
					list.add(atom);
				}
			}
			if(sheetIndex == 1){
				if(atom.getParams().get(9).equals("after_treatment") || atom.getParams().get(9).equals("diagnosis_treatment")){
					list.add(atom);
				}
			}
			
			if(sheetIndex == 2){ 
				if(atom.getParams().get(9).toString().equals("discharge_prescribed")){
					list.add(atom);
				}
			}
		}
		Atom[] atoms = new Atom[list.size()];
		list.toArray(atoms);
		return atoms;
	}
	
	/**
	 * 将症状拆分开
	 * @param zhengzhuangs
	 * @param isAdmin 是否为出院
	 */
	public static Atom[] getZhengzhuang(Atom[] zhengzhuangs,boolean isAdmin){
		ArrayList<Atom> list = new ArrayList<>();
		for (int i = 0; i < zhengzhuangs.length; i++) {
			Atom atom = zhengzhuangs[i];
			if(isAdmin){
				if(atom.getParams().get(9).equals("history_of_present_illness") || atom.getParams().get(9).equals("admission_detail")){
					list.add(atom);
				}
			}else{
				if(atom.getParams().get(9).equals("after_treatment") || atom.getParams().get(9).equals("diagnosis_treatment") || atom.getParams().get(9).toString().equals("discharge_prescribed")){
					list.add(atom);
				}
			}
		}
		Atom[] atoms = new Atom[list.size()];
		list.toArray(atoms);
		return atoms;
	}
	
	
	/**
	 * 获取症状str
	 * @param zhengzhuang
	 * @return
	 */
	public static String getZhengZhuangStr(Atom[] zhengzhuang){
		String str = "";
		for (int i = 0; i < zhengzhuang.length; i++) {
			str += (zhengzhuang[i].getParams().get(6).toString().replace("null", "") + zhengzhuang[i].getParams().get(3).toString().replace("null", "") + zhengzhuang[i].getParams().get(0).toString().replace("null", "") + ",");
		}
		return str;
	}
}
