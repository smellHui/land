package com.hawk.demo;

import android.app.Application;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.services.core.ServiceSettings;

/**
 * Created on 2018/11/26 20:43
 *
 * @author WingHawk
 */
public class App extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		String amapKey = "0d7e4d6a577fcbb0e75e6318a3527115";
		ServiceSettings.getInstance().setApiKey(amapKey);
		AMapLocationClient.setApiKey(amapKey);
	}
}
