package com.zdawn.commons.sqlquery.model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zdawn.commons.sysmodel.metaservice.ModelFactory;
import com.zdawn.util.ResourceUtil;
/**
 * 读取查询配置模型
 * @author zhaobs
 */
public class MetaQueryFactory {
	private static final Logger log = LoggerFactory.getLogger(MetaQueryFactory.class);
	private static MetaQuery metaQuery = null;
	
	private MetaQueryFactory(){
	}
	/**
	 * 从文件系统加载查询定义
	 * @param filePath 路径
	 */
	public static synchronized void loadQueryConfigFromFileSystem(String filePath){
		QueryConfigXMLLoader loader = new QueryConfigXMLLoader();
		metaQuery = loader.loadFromXML(filePath);
	}
	public static synchronized void loadQueryConfigFromFileSystem(String filePath,String regxFileName){
		QueryConfigXMLLoader loader = new QueryConfigXMLLoader();
		ArrayList<File> listFiles =  ResourceUtil.findAllFiles(filePath, regxFileName);
		if(metaQuery==null) metaQuery = new MetaQuery();
		for (File file : listFiles) {
			MetaQuery tmp = loader.loadFromXML(file);
			metaQuery.getResultMappers().putAll(tmp.getResultMappers());
			metaQuery.getQueryConfigs().putAll(tmp.getQueryConfigs());
		}
	}
	public static void loadQueryConfigFromJar(String path, String regexFileName) {
		ArrayList<String> list =  ResourceUtil.findAllFilesInJar(path, regexFileName);
		if(metaQuery==null) metaQuery = new MetaQuery();
		for (String tempPath : list) {
			MetaQuery tmp = loadFromInputStream(tempPath);
			if(tmp==null) continue;
			metaQuery.getResultMappers().putAll(tmp.getResultMappers());
			metaQuery.getQueryConfigs().putAll(tmp.getQueryConfigs());
		}
	}
	public static MetaQuery loadFromInputStream(String resourceName) {
		MetaQuery tmp = null;
		InputStream is = null;
		try {
			if(resourceName.charAt(0)!='/') resourceName = '/'+resourceName;
			is = ModelFactory.class.getResourceAsStream(resourceName);
			if(is!=null){
				log.info("load queryconfig resource name "+resourceName);
				QueryConfigXMLLoader loader = new QueryConfigXMLLoader();
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
	 * 从classpath加载查询定义
	 * @param fileName 验证文件名
	 */
	public static synchronized void loadQueryConfigFromClassPath(String fileName){
		metaQuery = loadFromInputStream(fileName);
	}
	/**
	 * 使用正则表达式从classpath装载查询定义
	 * @param regexFileName 文件名正则表达式
	 */
	public static synchronized void loadQueryConfigFromClassPathByRegexFileName(String regexFileName){
		try {
			Enumeration<URL> e = MetaQueryFactory.class.getClassLoader().getResources("");
			while(e.hasMoreElements()){
				String path = e.nextElement().getPath();
				if(path.indexOf(".jar!")==-1) {
					loadQueryConfigFromFileSystem(path,regexFileName);
				}else {
					loadQueryConfigFromJar(path, regexFileName);
				}
			}
		} catch (IOException e) {
			log.error("loadQueryConfigFromClassPathByRegexFileName",e);
		}
	}
	
	public static MetaQuery getMetaQuery() {
		return metaQuery;
	}
	
	public static void main(String[] arg){
		loadQueryConfigFromClassPathByRegexFileName("Sql-Query\\w*\\.xml");
	}
}
