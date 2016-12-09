package com.soarlee.mybike;

import org.json.JSONException;
import org.json.JSONObject;

public class Bike {
	public int id;
	public double lat;
	public double lon;

	/**
	 * 初始化对象
	 * @param JSONObject data
	 */
	public Bike(JSONObject data) {
		// TODO Auto-generated method stub
		try {
			this.id = data.getInt("id");
			this.lat = data.getDouble("lat");
			this.lon = data.getDouble("lon");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
