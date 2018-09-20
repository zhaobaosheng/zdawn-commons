package com.zdawn.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResourceUtil {
	/**
	 * 在给定文件路径中查找匹配正则表达式文件,不会有目录
	 * @param path 文件路径
	 * @param regxFileName 正则表达式
	 * @return ArrayList&lt;File&gt;
	 */
	public static ArrayList<File> findAllFiles(String path,String regxFileName){
		ArrayList<File> listFiles = new ArrayList<File>();
		Pattern pattern = Pattern.compile(regxFileName);
		ArrayList<File> subFoder = new ArrayList<File>();
		subFoder.add(new File(path));
		while(true){
			ArrayList<File> temp = new ArrayList<File>();
			for (File file : subFoder) {
				if(file.isDirectory()){//如果是文件夹,遍历子文件
					File[] subFile = file.listFiles();
					for (int i = 0; i < subFile.length; i++) {
						if(subFile[i].isDirectory()) temp.add(subFile[i]);
						else{
							Matcher matcher = pattern.matcher(subFile[i].getName());
							if(matcher.matches()) listFiles.add(subFile[i]);
						}
					}
				}else{
					Matcher matcher = pattern.matcher(file.getName());
					if(matcher.matches()) listFiles.add(file);
				}
			}
			//如果没有子目录退回循环，有赋值subFoder变量
			if(temp.size()==0) break;
			subFoder = null;
			subFoder = temp;
		}
		return listFiles;
	}
	/**
	 * 查找jar包中符合正则表达式的文件
	 * @param jarPath jar包的路径
	 * <br> file:/root/common-test-0.0.1-SNAPSHOT.jar!/BOOT-INF/classes!/
	 * <br> file:/root/common-test-0.0.1-SNAPSHOT.jar
	 * @param regxFileName 正则表达式
	 * @return List集合 每个元素是jar包中文件位置 example BOOT-INF/classes/DataModel.xml
	 */
	public static ArrayList<String> findAllFilesInJar(String jarPath,String regxFileName){
		JarFile jarFile = null;
		ArrayList<String> listPaths = new ArrayList<String>();
		try {
			Pattern pattern = Pattern.compile(regxFileName);
			String[] jarInfo = jarPath.split("!");
			if(jarInfo[0].length()==0) return listPaths;
	        String jarFilePath = jarInfo[0].substring(jarInfo[0].indexOf('/'));
	        String packagePath = "";
	        if(jarInfo.length>1) packagePath = jarInfo[1].length()>0 ? jarInfo[1].substring(1):"";
			jarFile = new JarFile(jarFilePath);  
			Enumeration<JarEntry> entrys = jarFile.entries();  
			while (entrys.hasMoreElements()) {
			    JarEntry jarEntry = entrys.nextElement();
			    if(jarEntry.isDirectory()) continue;
			    String entryName = jarEntry.getName();
			    String name = entryName.substring(entryName.lastIndexOf('/')+1);
			    if(packagePath.length()>0) {
			    	if(entryName.startsWith(packagePath)) {
			    		Matcher matcher = pattern.matcher(name);
						if(matcher.matches()) listPaths.add(entryName);
			    	}
			    }else {
			    	Matcher matcher = pattern.matcher(name);
					if(matcher.matches()) listPaths.add(entryName);
			    }
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}finally {
			if(jarFile!=null)
				try {jarFile.close();} catch (Exception e){}
		}
		return listPaths;
	}
	/**
	 * 在给定路径中查找给定文件名的文件
	 * @param path 文件路径
	 * @param regxFileName 正则表达式
	 * @param findOnlyOne 是否找到一个就返回
	 * @return ArrayList&lt;File&gt;
	 */
	public static ArrayList<File> findFiles(String path,String fileName,boolean findOnlyOne){
		ArrayList<File> listFiles = new ArrayList<File>();
		ArrayList<File> subFoder = new ArrayList<File>();
		subFoder.add(new File(path));
		while(true){
			ArrayList<File> temp = new ArrayList<File>();
			for (File file : subFoder) {
				if(file.isDirectory()){//如果是文件夹,遍历子文件
					File[] subFile = file.listFiles();
					for (int i = 0; i < subFile.length; i++) {
						if(subFile[i].isDirectory()) temp.add(subFile[i]);
						else{
							if(subFile[i].getName().equals(fileName)) listFiles.add(subFile[i]);
						}
					}
				}else{
					if(file.getName().equals(fileName)) listFiles.add(file);
				}
			}
			if(findOnlyOne && listFiles.size()==1) return listFiles;
			//如果没有子目录退回循环，有赋值subFoder变量
			if(temp.size()==0) break;
			subFoder = null;
			subFoder = temp;
		}
		return listFiles;
	}
	/**
	 * 查找jar包中的文件
	 * @param path jar文件的路径
	 * @param fileName 文件名
	 * @param findOnlyOne 是否找到一个就返回
	 * @return List集合 每个元素是jar包中文件位置 example BOOT-INF/classes/DataModel.xml
	 */
	public static ArrayList<String> findFilesInJar(String path,String fileName,boolean findOnlyOne){
		JarFile jarFile = null;
		ArrayList<String> listPaths = new ArrayList<String>();
		try {
			if(path.startsWith("file:")){
				path = path.substring(path.indexOf("file:")+5);
			}
			jarFile = new JarFile(path);
			Enumeration<JarEntry> entrys = jarFile.entries();  
			while (entrys.hasMoreElements()) {
			    JarEntry jarEntry = entrys.nextElement();
			    if(jarEntry.isDirectory()) continue;
			    String entryName = jarEntry.getName();
			    String name = entryName.substring(entryName.lastIndexOf('/')+1);
			    if(name.equals(fileName)){
			    	listPaths.add(entryName);
			    	if(findOnlyOne) return listPaths;
			    }
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}finally {
			if(jarFile!=null)
				try {jarFile.close();} catch (Exception e){}
		}
		return listPaths;
	}
	/**
	 * 读取属性文件 内部使用java.util.Properties读取
	 * @param filePath 文件路径
	 * @return 如果文件内容为空返回null
	 */
	public static Map<String,String> readPropertyFile(File file){
		Map<String,String> mapValue = new HashMap<String, String>();
		InputStream in = null;
		try {
			in = new FileInputStream(file);
			Properties props = new Properties();
			props.load(in);
			Enumeration<?> en = props.propertyNames();
			while (en.hasMoreElements()) {
				String key = (String) en.nextElement();
				String Property = props.getProperty(key);
				mapValue.put(key, Property);
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}finally{
			try {
				if(in!=null) in.close();
			} catch (IOException e) {}
		}
		return mapValue.size()>0 ? mapValue:null;
	}
	/**
	 * 从classpath中读取属性文件 内部使用java.util.Properties读取
	 * @param resourceName 属性文件名
	 * @return 如果文件内容为空返回null
	 */
	public static Map<String,String> readPropertyFile(String resourceName){
		Map<String, String> map = new HashMap<String, String>();
		InputStream in = null;
		try {
			in = ResourceUtil.class.getResourceAsStream(resourceName);
			Properties props = new Properties();
			props.load(in);
			Enumeration<?> en = props.propertyNames();
			while (en.hasMoreElements()) {
				String key = (String) en.nextElement();
				String Property = props.getProperty(key);
				map.put(key, Property);
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}finally{
			try {
				if(in!=null) in.close();
			} catch (IOException e) {}
		}
		return map.size()>0 ? map:null;
	}
}
