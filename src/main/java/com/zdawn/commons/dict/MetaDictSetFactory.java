package com.zdawn.commons.dict;

import java.io.IOException;
import java.io.InputStream;

import com.zdawn.commons.dict.model.DictMetaXMLLoader;
import com.zdawn.commons.dict.model.MetaDictSet;

public class MetaDictSetFactory {
	private static MetaDictSet metaDictSet = null;
	private MetaDictSetFactory(){
	}
	public static synchronized void loadFileSystemDataDictionary(String filePath){
		DictMetaXMLLoader loader = new DictMetaXMLLoader();
		metaDictSet = loader.loadFromXML(filePath);
	}
	public static synchronized void loadClassPathDataDictionary(String resourceName){
		InputStream is = null;
		try {
			if(resourceName.charAt(0)!='/') resourceName = '/'+resourceName;
			is = MetaDictSetFactory.class.getResourceAsStream(resourceName);
			if(is!=null){
				DictMetaXMLLoader loader = new DictMetaXMLLoader();
				metaDictSet = loader.loadFromXML(is);
			}
		} finally {
			try {
				if(is!=null) is.close();
			} catch (IOException e) {}
		}
	}
	public static MetaDictSet getMetaDictSet(){
		return metaDictSet;
	}
}
