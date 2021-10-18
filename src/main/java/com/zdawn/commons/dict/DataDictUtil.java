package com.zdawn.commons.dict;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zdawn.commons.dict.model.DataField;
import com.zdawn.commons.dict.model.DictData;
import com.zdawn.commons.dict.model.MetaDict;
import com.zdawn.commons.dict.model.PropertyValue;
import com.zdawn.commons.dict.model.RecordData;
import com.zdawn.util.Utils;
/**
 * 数据字典工具类
 * <br>为下拉框提供数据
 * <br>将数据转换成JSON串
 * @author zhaobs
 */
public class DataDictUtil {
	
	private static DataDictionary dataDictionary;
	
	public static Map<String,List<Map<String,Object>>> convertCommonDataForDicts(boolean dateToString,String... dictNames) {
		if(dictNames==null) return null;
		Map<String,List<Map<String,Object>>> map = new HashMap<>();
		for (String one : dictNames) {
			map.put(one, convertCommonDataForDict(one, dateToString));
		}
		return map;
	}
	public static List<Map<String,Object>> convertCommonDataForDict(String dictName,boolean dateToString){
		if(dataDictionary==null) throw new RuntimeException("DataDictionary is not init");
		MetaDict dicMeta  = dataDictionary.getMetaDict(dictName);
		if(dicMeta==null) throw new RuntimeException(dictName+" is not config");
		DictData data = dataDictionary.getDictData(dictName);
		if(data==null) throw new RuntimeException(dictName+" data is not exists");
		List<Map<String,Object>> dataList = new ArrayList<>();
		List<RecordData> list = data.getListRecordDatas();
		if(dateToString) {//date 类型转换字符串
			for (RecordData recordData : list) {
				Map<String,Object> temp = new HashMap<String,Object>();
				List<DataField> listField = dicMeta.getListDataFields();
				for (DataField dataField : listField) {
					Object obj = recordData.getValue(dataField.getFieldName());
					if(dataField.getDataType().equals("date")){
						temp.put(dataField.getFieldName(), obj!=null ? 
								Utils.transformDate(dataField.getToStringformat(), (Date)obj):"");
					}else{
						temp.put(dataField.getFieldName(), obj);
					}
				}
				dataList.add(temp);
			}
		}else {
			for (RecordData recordData : list) {
				dataList.add(recordData.getAllData());
			}
		}
		return dataList;
	}
	
	public static Map<String,List<PropertyValue>> convertPropertyValueForDicts(String... dictNames) {
		if(dictNames==null) return null;
		Map<String,List<PropertyValue>> map = new HashMap<>();
		for (String one : dictNames) {
			map.put(one, convertPropertyValueForDict(one));
		}
		return map;
	}
	
	public static List<PropertyValue> convertPropertyValueForDict(String dictName) {
		if(dataDictionary==null) throw new RuntimeException("DataDictionary is not init");
		MetaDict dicMeta  = dataDictionary.getMetaDict(dictName);
		if(dicMeta==null) throw new RuntimeException(dictName+" is not config");
		DictData data = dataDictionary.getDictData(dictName);
		if(data==null) throw new RuntimeException(dictName+" data is not exists");
		List<PropertyValue> al = new ArrayList<PropertyValue>();
		List<RecordData> list = data.getListRecordDatas();
		List<DataField> listField = dicMeta.getListDataFields();
		for (RecordData recordData : list) {
			PropertyValue pv = new PropertyValue();
			StringBuilder sb = new StringBuilder();
			for (DataField dataField : listField) {
				if(dataField.getFieldName().equals(dicMeta.getUniqueField())) {
					pv.setId(recordData.getValue(dataField.getFieldName())+"");
				}
				if(dataField.isDisplay()) {
					Object obj = recordData.getValue(dataField.getFieldName());
					String disp = "";
					if(dataField.getDataType().equals("date")){
						disp = obj!=null ? Utils.transformDate(dataField.getToStringformat(),(Date)obj):"";
					}else{
						disp = obj!=null ? obj.toString():"";
					}
					if(!disp.equals("")) {
						if(sb.length()==0) {
							sb.append(disp);
						}else {
							sb.append("-"+disp);
						}
					}
				}
			}
			pv.setText(sb.toString());
			al.add(pv);
		}
		return al;
	}
	
	public static void setDataDictionary(DataDictionary dataDictionary) {
		DataDictUtil.dataDictionary = dataDictionary;
	}
}
