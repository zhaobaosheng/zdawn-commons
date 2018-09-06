package com.zdawn.util.vo;

public class VOUtil {
	public static ResponseMessage createResponseMessage(String result,String desc,Object data){
		return createResponseMessage(result, desc, null, data);
	}
	public static ResponseMessage createResponseMessage(String result,String desc,String errorCode,Object data){
		ResponseMessage msg = new ResponseMessage();
		msg.setResult(result);
		msg.setDesc(desc);
		if(errorCode!=null) msg.setErrorCode(errorCode);
		msg.setData(data);
		return msg;
	}
	public static ResponseMessage createResponseMessage(boolean success,String desc,Object data){
		return createResponseMessage(String.valueOf(success),desc,data);
	}
}
