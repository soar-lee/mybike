package com.soarlee.mybike;

import java.util.List;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements LocationSource,
		AMapLocationListener {

	private String server = "http://bike.soarlee.com/index/getbike";
	private Handler handler = new Handler();
	private Context context = this;
	
	public LatLng myLng = new LatLng(30.6747,104.0592);  
	private int zoom = 16;						//缩放比例
	private MarkerOptions markerOption;			//坐标点类
	private AMap aMap;							//地图类
	private MapView mapView;					//mapview控件
	private OnLocationChangedListener mListener;	//定位改变监听类
	private boolean mFirstFix = false;			//首次定位
	
	private TextView mLocationErrText;	//定位失败提示框
	private Circle mCircle;
	public static final String LOCATION_MARKER_FLAG = "我的位置";
	private Marker mLocMarker;
	private AMapLocationClient mlocationClient;
	private AMapLocationClientOption mLocationOption;
	
	private static final int STROKE_COLOR = Color.argb(180, 3, 145, 255);
	private static final int FILL_COLOR = Color.argb(10, 0, 0, 180);
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	    /*
         * 设置离线地图存储目录，在下载离线地图或初始化地图设置;
         * 使用过程中可自行设置, 若自行设置了离线地图存储的路径，
         * 则需要在离线地图下载和使用地图页面都进行路径设置
         * */
	    //Demo中为了其他界面可以使用下载的离线地图，使用默认位置存储，屏蔽了自定义设置
      //  MapsInitializer.sdcardDir =OffLineMapUtils.getSdCacheDir(this);
		mapView = (MapView) findViewById(R.id.map);
		mapView.onCreate(savedInstanceState);// 此方法必须重写
		init();
    }
    
	/**
	 * 初始化
	 */
	private void init() {
		if (aMap == null) {
			aMap = mapView.getMap();
			setUpMap();				//初始化地图
			init_location();		//添加我的位置到地图
			//sendRequestWithHttpClient();
			String url = server+"?lon="+myLng.longitude+"&lat="+myLng.latitude;
			new HttpJson(url,context,handler,this).start();		//添加自行车到地图
		}
	}
	
	/**
	 * 初始化localtion
	 */
	private void init_location(){            
        //设置中心点和缩放比例  
		addMyLocation(myLng);//添加定位图标
		aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLng, zoom));
	}
	
	/**
	 * 设置一些amap的属性
	 */
	private void setUpMap() {
		
		aMap.setLocationSource(this);// 设置定位监听
		//aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
		aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
		// 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
		aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
		UiSettings uiSettings = aMap.getUiSettings();
		uiSettings.setZoomControlsEnabled(false);
		uiSettings.setLogoPosition(AMapOptions.LOGO_POSITION_BOTTOM_RIGHT);
	}

    
	/**
	 * 方法必须重写
	 */
	@Override
	protected void onResume() {
		super.onResume();
		mapView.onResume();
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onPause() {
		super.onPause();
		mapView.onPause();
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
	}
	
	/**
	 * 在地图上添加marker
	 */
	public void addBikeToMap(List<Bike> bikeList) {
		LatLng bikeLng;
		for(Bike bike:bikeList){
			bikeLng = new LatLng(bike.lat, bike.lon);
			markerOption = new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                    .decodeResource(getResources(),
                            R.drawable.bycicle)))
					.position(bikeLng)
					.draggable(true);
			aMap.addMarker(markerOption);
		}
		
	}

	@Override
	public void onLocationChanged(AMapLocation amapLocation) {
		// TODO Auto-generated method stub
		if (mListener != null && amapLocation != null) {
			if (amapLocation != null
					&& amapLocation.getErrorCode() == 0) {
				mLocationErrText.setVisibility(View.GONE);
				this.myLng = new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude());
				
			} else {
				String errText = "定位失败," + amapLocation.getErrorCode()+ ": " + amapLocation.getErrorInfo();
				Log.e("AmapErr",errText);
				Toast.makeText(context, errText, Toast.LENGTH_SHORT).show();
			}
		}
		if (!mFirstFix) {
			mFirstFix = true;
			addCircle(myLng, amapLocation.getAccuracy());//添加定位精度圆
			addMyLocation(myLng);//添加定位图标
			//mSensorHelper.setCurrentMarker(mLocMarker);//定位图标旋转
		} else {
			mCircle.setCenter(myLng);
			mCircle.setRadius(amapLocation.getAccuracy());
			mLocMarker.setPosition(myLng);
		}
		
		//aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, zoom));
	}

	@Override
	public void activate(OnLocationChangedListener listener) {
		// TODO Auto-generated method stub
		mListener = listener;
		if (mlocationClient == null) {
			mlocationClient = new AMapLocationClient(this);
			mLocationOption = new AMapLocationClientOption();
			//设置定位监听
			mlocationClient.setLocationListener(this);
			//设置为高精度定位模式
			mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
			//设置定位参数
			mlocationClient.setLocationOption(mLocationOption);
			// 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
			// 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
			// 在定位结束后，在合适的生命周期调用onDestroy()方法
			// 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
			mlocationClient.startLocation();
		}
	}

	@Override
	public void deactivate() {
		// TODO Auto-generated method stub
		mListener = null;
		if (mlocationClient != null) {
			mlocationClient.stopLocation();
			mlocationClient.onDestroy();
		}
		mlocationClient = null;
	}
	
	private void addCircle(LatLng latlng, double radius) {
		CircleOptions options = new CircleOptions();
		options.strokeWidth(1f);
		options.fillColor(FILL_COLOR);
		options.strokeColor(STROKE_COLOR);
		options.center(latlng);
		options.radius(radius);
		mCircle = aMap.addCircle(options);
	}

	private void addMyLocation(LatLng latlng) {
		if (mLocMarker != null) {
			return;
		}
		Bitmap bMap = null;
		bMap = BitmapFactory.decodeResource(this.getResources(),
				R.drawable.navi_map_gps_locked);
		BitmapDescriptor des = BitmapDescriptorFactory.fromBitmap(bMap);
		
//		BitmapDescriptor des = BitmapDescriptorFactory.fromResource(R.drawable.navi_map_gps_locked);
		MarkerOptions options = new MarkerOptions();
		options.icon(des);
		options.anchor(0.5f, 0.5f);
		options.position(latlng);
		mLocMarker = aMap.addMarker(options);
		mLocMarker.setTitle(LOCATION_MARKER_FLAG);
	}
}
