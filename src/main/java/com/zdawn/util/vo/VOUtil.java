package com.zdawn.util.vo;

public class VOUtil {
	public static ResponseMessage createResponseMessage(boolean success,String desc,Object data){
		return createResponseMessage(success, desc, null, data);
	}
	public static ResponseMessage createResponseMessage(boolean success,String desc,String errorCode,Object data){
		ResponseMessage msg = new ResponseMessage();
		msg.setResult(success);
		msg.setDesc(desc);
		if(errorCode!=null) msg.setErrorCode(errorCode);
		msg.setData(data);
		return msg;
	}
}
