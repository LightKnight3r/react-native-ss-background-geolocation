package com.ss.geolocation;

import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.ReactMethod;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.content.IntentFilter;
import java.lang.Exception;
import android.location.Location;
import android.os.Bundle;
import android.os.Build;
import android.support.v4.content.ContextCompat;


public class GeoLocationModule extends ReactContextBaseJavaModule {
  public GeoLocationModule(ReactApplicationContext reactContext) {
    super(reactContext);
    BroadcastReceiver geoLocationReceiver = new BroadcastReceiver() {
      public void onReceive(Context context, Intent intent) {
        Location message = intent.getParcelableExtra("message");
        GeoLocationModule.this.sendEvent(message);
      }
    };
    LocalBroadcastManager.getInstance(getReactApplicationContext())
      .registerReceiver(geoLocationReceiver, new IntentFilter("GeoLocationUpdate"));
  }

  @Override
  public String getName() {
    return "GeoLocationModule";
  }

  @ReactMethod
  public void startService(String serverAddr, String memberToken, int time, int distance, String accuracy) {
    Bundle data = new Bundle();

    data.putString("serverAddr", serverAddr);
    data.putString("memberToken", memberToken);
    data.putInt("time", time);
    data.putInt("distance", distance);
    data.putString("accuracy", accuracy);

    Intent intent = new Intent(GeoLocationService.FOREGROUND);
    intent.putExtras(data);
    intent.setClass(this.getReactApplicationContext(), GeoLocationService.class);
    if (Build.VERSION.SDK_INT >= 26) {
      getReactApplicationContext().startForegroundService(intent);
    } else {
      getReactApplicationContext().startService(intent);
    }
  }

  @ReactMethod
  public void stopService(Promise promise) {
    String result = "Success";
    try {
      Intent intent = new Intent(GeoLocationService.FOREGROUND);
      intent.setClass(this.getReactApplicationContext(), GeoLocationService.class);
      this.getReactApplicationContext().stopService(intent);
    } catch (Exception e) {
      promise.reject(e);
      return;
    }
    promise.resolve(result);
  }

  private void sendEvent(Location message) {
    WritableMap map = Arguments.createMap();
    WritableMap coordMap = Arguments.createMap();
    coordMap.putDouble("latitude", message.getLatitude());
    coordMap.putDouble("longitude", message.getLongitude());
    coordMap.putDouble("accuracy", message.getAccuracy());
    coordMap.putDouble("altitude", message.getAltitude());
    coordMap.putDouble("heading", message.getBearing());
    coordMap.putDouble("speed", message.getSpeed());

    map.putMap("coords", coordMap);
    map.putDouble("timestamp", message.getTime());

    getReactApplicationContext()
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
      .emit("updateLocation", map);
  }
}
