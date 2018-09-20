package com.zdawn.commons.sysmodel.metaservice;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zdawn.commons.sysmodel.metaservice.impl.SysModelImpl;
import com.zdawn.util.ResourceUtil;

public class ModelFactory {
	private static final Logger log = LoggerFactory.getLogger(ModelFactory.class);
	private static SysModel sysModel = null;
	
	private ModelFactory(){
	}
	/**
	 * 从文件系统加载实体模型
	 * @param filePath 路径
	 */
	public static synchronized void loadDataModelFromFileSystem(String filePath){
		SysModelXMLLoader loader = new SysModelXMLLoader();
		sysModel = loader.loadFromXML(filePath);
	}
	public static synchronized void loadDataModelFromFileSystem(String filePath,String regxFileName){
		SysModelXMLLoader loader = new SysModelXMLLoader();
		ArrayList<File> listFiles =  ResourceUtil.findAllFiles(filePath, regxFileName);
		if(sysModel==null) sysModel = new SysModelImpl();
		for (File file : listFiles) {
			SysModel tmp = loader.loadFromXML(file);
			sysModel.getConfig().putAll(tmp.getConfig());
			sysModel.getEntities().putAll(tmp.getEntities());
		}
	}
	public static synchronized void loadDataModelFromJar(String jarPath,String regxFileName){
		ArrayList<String> listFiles =  ResourceUtil.findAllFilesInJar(jarPath, regxFileName);
		if(sysModel==null) sysModel = new SysModelImpl();
		for (String tempPath : listFiles) {
			SysModel tmp = loadFromInputStream(tempPath);
			if(tmp==null) continue;
			sysModel.getConfig().putAll(tmp.getConfig());
			sysModel.getEntities().putAll(tmp.getEntities());
		}
	}
	public static SysModel loadFromInputStream(String resourceName) {
		SysModel tmp = null;
		InputStream is = null;
		try {
			if(resourceName.charAt(0)!='/') resourceName = '/'+resourceName;
			is = ModelFactory.class.getResourceAsStream(resourceName);
			if(is!=null){
				log.info("load datamodel resource name "+resourceName);
				SysModelXMLLoader loader = new SysModelXMLLoader();
				tmp = loader.loadFromXML(is);
			}
		} finally {
			try {
				if(is!=null) is.close();
			} catch (IOException e) {}
		}
		return tmp;
	}
	/**
	 * 从classpath加载实体模型
	 * @param fileName 配置文件名
	 */
	public static synchronized void loadDataModelFromClassPath(String fileName){
		sysModel=loadFromInputStream(fileName);
	}
	/**
	 * 使用正则表达式从classpath装载实体模型
	 * @param regxFileName 文件名正则表达式
	 */
	public static synchronized void loadDataModelFromClassPathByRegexFileName(String regxFileName){
		try {
			Enumeration<URL> e = ModelFactory.class.getClassLoader().getResources("");
			while(e.hasMoreElements()){
				String path = e.nextElement().getPath();
				if(path.indexOf(".jar!")==-1) {
					loadDataModelFromFileSystem(path,regxFileName);	
				}else {
					loadDataModelFromJar(path, regxFileName);
				}
			}
		} catch (IOException e) {
			log.error("loadDataModelFromClassPathByRegexFileName",e);
		}
	}
	public static SysModel getSysModel() {
		return sysModel;
	}
	
	public static void main(String[] arg){
		loadDataModelFromClassPathByRegexFileName("DataModel\\w*\\.xml");
	}
}
