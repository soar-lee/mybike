package com.soarlee.mybike;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

public class HttpJson extends Thread {
	
	private String url;
	private Context context;
	private Handler handler;
	private MainActivity main;
	private List<Bike> bikeList;
	
	public HttpJson(String url,Context context,Handler handler,MainActivity main){
		this.url = url;
		this.context = context;
		this.handler = handler;
		this.main = main;
	}
	
	public void run(){
		try {
			URL httpUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
	        conn.setReadTimeout(5000);
	        conn.setRequestMethod("GET");
	        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	        StringBuffer sb = new StringBuffer();
	        String str;
	        
	        while((str=reader.readLine())!=null){
	        	sb.append(str);
	        }

	        parseJson(sb.toString());
	        handler.post(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					if(null != bikeList && bikeList.size() !=0){
						main.addBikeToMap(bikeList);
					}
				}
			});
	        
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
	}
	
	private void parseJson(String json){
		JSONObject object;
		List<Bike> bikeList = new ArrayList<Bike>();
		try {
			object = new JSONObject(json);
			int result = object.getInt("result");
			if(result == 1){
				JSONArray bikeData = object.getJSONArray("bikeData");
				
				for(int i=0;i<bikeData.length();i++){
					Bike bike = new Bike(bikeData.getJSONObject(i));
					bikeList.add(bike);
				}
				this.bikeList = bikeList;
			}else{
				Looper.prepare();
				Toast.makeText(context, "获取自行车数据失败!", Toast.LENGTH_SHORT).show();
				Looper.loop();
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	
	private void post_demo(){
		try {
			URL httpUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
	        conn.setReadTimeout(5000);
	        conn.setRequestMethod("POST");
	        OutputStream out = conn.getOutputStream();
	        String content = "x="+main.myLng.longitude+"&y="+main.myLng.latitude;
	        out.write(content.getBytes());
	        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	        StringBuffer sb = new StringBuffer();
	        String str;
	        
	        while((str=reader.readLine())!=null){
	        	sb.append(str);
	        }
	        parseJson(sb.toString());
	        handler.post(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					main.addBikeToMap(bikeList);
				}
			});
	        
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
	
