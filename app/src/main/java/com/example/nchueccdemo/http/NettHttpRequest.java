package com.example.nchueccdemo.http;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;



public class NettHttpRequest {
	private String output;
	private String httpVerb;
	private String requestUrl;
	private byte[] postData = null;
	
	public NettHttpRequest() {
		reset();
	}
	
	public String getResponse() {
		return this.output;
	}
	
	public void doGetUserTokens(String id) {
		requestUrl = "...";

		Map<String, String> map = new HashMap<>();
		map.put("user", id);
		postData = FormParam.getFormParam(map).getBytes(StandardCharsets.UTF_8);
	}
	
	public void doAddTokens(int tokens, String from, String to) {
		requestUrl = "...";
		
		Map<String, String> map = new HashMap<>();
		map.put("user_from", from);
		map.put("user_to", to);
		map.put("tokens", String.valueOf(tokens));
		postData = FormParam.getFormParam(map).getBytes(StandardCharsets.UTF_8);
	}
	
	public void sendRequest() {
		try {
			URL url = new URL(requestUrl);
		    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		    conn.setDoOutput(true); 
		    conn.setInstanceFollowRedirects(false); 
		    conn.setRequestMethod("POST");
			conn.setRequestProperty("charset", StandardCharsets.UTF_8.name());
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("Content-Length", Integer.toString(postData.length));

		    conn.setUseCaches(false);
		    conn.setRequestProperty("Accept", "application/json");
		    conn.setRequestProperty("Accept-Charset", StandardCharsets.UTF_8.name());
		    

		    new DataOutputStream(conn.getOutputStream()).write(postData);


		    int status = conn.getResponseCode();
//			Log.d("===========", httpVerb + " status code " + status);

		    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		    String inputLine;
		    StringBuffer responseData = new StringBuffer();
		    while ((inputLine = reader.readLine()) != null) {
		    	responseData.append(inputLine);
		      	Log.d("===========", "inputLine " + inputLine);
		    }
		    reader.close();
		    conn.disconnect();
		    
		    output = responseData.toString();
		} catch (MalformedURLException | ProtocolException e) {
			output = "{'res':'exception " + e.getMessage()+ "'}";
			e.printStackTrace();
		} catch (IOException e) {
			output = "{'res':'exception [IO]" + e.getMessage()+ "'}";
			e.printStackTrace();
		} finally {
			
		}
	}
	
	public void reset() {
		this.output = "";
		this.postData = null;
	}

}
