package com.example.nchueccdemo.http;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FormParam {
	/**
	 * 輸入form parameters的map物件，回傳string
	 * @param map
	 * @return http request form parameters format like aaa=123&bbb=456&ccc=678
	 */
	public static String getFormParam(Map<String, String> map) {
		String output = "";
		
		List<String> list = new ArrayList<>();
		for (String key : map.keySet()) {
			list.add(key + "=" + map.get(key));
		}
		
		output = list.stream().collect(Collectors.joining("&"));
		
		return output;
	}
	/**
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public static String getFormParam(String key, String value) {
		String output = "";
		
		if (key != null && value != null) {
			output = key + "=" + value;
		}
		
		return output;
	}
	
	public static String getTokenParam(int token) {
		if (token >= 0)
			return "token=" + token;
		else
			return "";
	}
	
	public static String getPasswdParam(String passwd) {
		if (passwd != null)
			return "pw=" + passwd;
		else
			return "";
	}
}
