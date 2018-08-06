package com.ss.geolocation;

import android.app.Service;
import android.location.LocationManager;
//import android.location.LocationListener;
import android.location.Location;
import android.support.v4.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.content.LocalBroadcastManager;
import java.lang.Exception;
import android.content.Intent;
import android.os.IBinder;
import android.app.Notification;
import android.support.v4.app.NotificationCompat;
import android.graphics.BitmapFactory;
import java.lang.System;
import android.app.PendingIntent;
import android.os.Bundle;
import android.util.Log;
import android.os.Build;
import android.app.NotificationManager;
import android.content.Context;
import android.app.NotificationChannel;

import java.io.InputStream;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONObject;
import org.apache.http.entity.StringEntity;
import org.apache.http.HttpResponse;
import android.os.AsyncTask;
import android.widget.Toast;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import com.ss.geolocation.R;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


public class GeoLocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener{
  public static final String FOREGROUND = "com.ss.bggeo.FOREGROUND";
  //LocationManager locationManager = null;
  private static int GEOLOCATION_NOTIFICATION_ID = 12345689;
  private static final String TAG = GeoLocationService.class.getSimpleName();
  GoogleApiClient mLocationClient;
  LocationRequest mLocationRequest = new LocationRequest();
  double lat;
  double lng;
  public String serverAddr;
  public int time;
  static String memberToken;
  public int distance;
  public String accuracy;
  private static final String CHANNEL_ID = "channel_sysnotify";
  private static final String EXTRA_STARTED_FROM_NOTIFICATION = "extra_from_notify";

  // LocationListener locationListener = new LocationListener() {
  //   public void onLocationChanged(Location location) {
  //     lat = location.getLatitude();
  //     lng = location.getLongitude();
  //     //Log.d("ahihi onLocationChanged",serverAddr);
  //
  //     new HttpAsyncTask().execute(serverAddr);
  //
  //     //new HttpAsyncTask().execute("http://103.63.109.155:3000");
  //   }
  //
  //   public void onStatusChanged(String s, int i, Bundle bundle) {}
  //
  //   public void onProviderEnabled(String s) {}
  //
  //   public void onProviderDisabled(String s) {}
  // };

  public void onCreate() {
    super.onCreate();
  }
  public static String POST(String url, Location location){
    InputStream inputStream = null;
    String result = "";
    try {

        HttpClient httpclient = new DefaultHttpClient();

        HttpPost httpPost = new HttpPost(url);

        String json = "";
        //Log.d("ahihi POST", memberToken);
        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("latitude", location.getLatitude());
        jsonObject.accumulate("longitude", location.getLongitude());
        jsonObject.accumulate("memberToken", memberToken);
        jsonObject.accumulate("user", "mindaptroai");
        json = jsonObject.toString();


        StringEntity se = new StringEntity(json);

        httpPost.setEntity(se);

        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");

        HttpResponse httpResponse = httpclient.execute(httpPost);

        inputStream = httpResponse.getEntity().getContent();

        if(inputStream != null)
            result = convertInputStreamToString(inputStream);
        else
            result = "Did not work!";

    } catch (Exception e) {
        Log.d("InputStream", e.getLocalizedMessage());
    }

    return result;
  }

  private static String convertInputStreamToString(InputStream inputStream) throws IOException{
      BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
      String line = "";
      String result = "";
      while((line = bufferedReader.readLine()) != null)
          result += line;

      inputStream.close();
      return result;

  }
  private class HttpAsyncTask extends AsyncTask<String, Void, String> {
    @Override
      protected String doInBackground(String... urls) {

          Location location = new Location("");
          location.setLatitude(lat);
          location.setLongitude(lng);

          return POST(urls[0],location);
      }
      // onPostExecute displays the results of the AsyncTask.
      @Override
      protected void onPostExecute(String result) {
          Toast.makeText(getBaseContext(), "Data Sent!"+"["+lat+","+lng+"]", Toast.LENGTH_LONG).show();
     }
  }

  // @Override
  // public void onDestroy() {
  //   super.onDestroy();
  //   locationManager.removeUpdates(locationListener);
  // }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {

    super.onStartCommand(intent, flags, startId);

    serverAddr = (String) intent.getExtras().get("serverAddr");
    time = (int) intent.getExtras().get("time");
    distance = (int) intent.getExtras().get("distance");
    memberToken = (String) intent.getExtras().get("memberToken");
    accuracy = (String) intent.getExtras().get("accuracy");

    Log.d("ahihiserverAddr",serverAddr);
    Log.d("ahihimemberToken",memberToken);
    Log.d("time","ahihi"+time);
    Log.d("distance","ahihi"+distance);
    Log.d("ahihiaccuracy", accuracy);

    mLocationClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

    mLocationRequest.setSmallestDisplacement(distance);
    mLocationRequest.setInterval(time);
    mLocationRequest.setFastestInterval(time/2);
    int priority;
    if( accuracy.equals("BALANCED_POWER_ACCURACY") ) {
      priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
      Log.d("BALANCED_POWER_ACCURACY","ahihi"+priority);
    } else if(accuracy.equals("HIGH_ACCURACY")) {
      priority = LocationRequest.PRIORITY_HIGH_ACCURACY;
      Log.d("HIGH_ACCURACY","ahihi"+priority);
    } else if(accuracy.equals("LOW_POWER")) {
      priority = LocationRequest.PRIORITY_LOW_POWER;
    } else if(accuracy.equals("NO_POWER")) {
      priority = LocationRequest.PRIORITY_NO_POWER;
    } else {
      priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
      Log.d("BALANCED_POWER_ACCURACY2","ahihi"+priority);
    }
    // locationManager = getSystemService(LocationManager.class);
    // int permissionCheck = ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION);
    // if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
    //   locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, time, distance, locationListener);
    // }
    mLocationRequest.setPriority(priority);
    mLocationClient.connect();

    if (Build.VERSION.SDK_INT >= 26) {
      startForeground(GEOLOCATION_NOTIFICATION_ID, getCompatNotification());
    }
    return  START_REDELIVER_INTENT;
  }

  public IBinder onBind(Intent intent) {
    return null;
  }

  private void createChannel() {
      NotificationManager mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
      // The id of the channel.
      String id = CHANNEL_ID;
      // The user-visible name of the channel.
      CharSequence name = "Location update";
      // The user-visible description of the channel.
      String description = "Location update control";
      int importance = NotificationManager.IMPORTANCE_LOW;
      NotificationChannel mChannel = new NotificationChannel(id, name, importance);
      // Configure the notification channel.
      mChannel.setDescription(description);
      mChannel.setShowBadge(false);
      mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
      mNotificationManager.createNotificationChannel(mChannel);
  }

  /*
   * LOCATION CALLBACKS
   */
  @Override
  public void onConnected(Bundle dataBundle) {
      if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
          // TODO: Consider calling
          //    ActivityCompat#requestPermissions
          // here to request the missing permissions, and then overriding
          //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
          //                                          int[] grantResults)
          // to handle the case where the user grants the permission. See the documentation
          // for ActivityCompat#requestPermissions for more details.

          Log.d(TAG, "== Error On onConnected() Permission not granted");
          //Permission not granted by user so cancel the further execution.

          return;
      }
      LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, mLocationRequest, this);

      Log.d(TAG, "Connected to Google API");
  }

  /*
   * Called by Location Services if the connection to the
   * location client drops because of an error.
   */
  @Override
  public void onConnectionSuspended(int i) {
      Log.d(TAG, "Connection suspended");
  }

  //to get the location change
  @Override
  public void onLocationChanged(Location location) {
      Log.d(TAG, "Location changed");


      if (location != null) {
          Log.d(TAG, "== location != null");
          lat = location.getLatitude();
          lng = location.getLongitude();
          //Log.d("ahihi onLocationChanged",serverAddr);

          new HttpAsyncTask().execute(serverAddr);
          //Send result to activities
          //sendMessageToUI(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
      }
  }


  @Override
  public void onConnectionFailed(ConnectionResult connectionResult) {
      Log.d(TAG, "Failed to connect to Google API");

  }

  @Override
  public void onDestroy() {
      if (mLocationClient != null  && mLocationClient.isConnected()) {
          LocationServices.FusedLocationApi.removeLocationUpdates(mLocationClient, this);
          mLocationClient.disconnect();
          mLocationClient = null;
      }
      Log.d(TAG, "onDestroy LocationService");
      super.onDestroy();
  }

  private Notification getCompatNotification() {
    createChannel();

    Intent startIntent = new Intent(getApplicationContext(), GeoLocationService.class);
    PendingIntent contentIntent = PendingIntent.getActivity(this, 0, startIntent, 0);
    NotificationCompat.Builder builder = new NotificationCompat.Builder(this,CHANNEL_ID);
    String str = "Is using your location in the background";
    builder
      .setSmallIcon(R.mipmap.ic_launcher)
      .setPriority(Notification.PRIORITY_MIN)
      .setContentTitle("Săn Ship")
      .setContentIntent(contentIntent)
      .setContentText(str)
      .setTicker(str)
      .setAutoCancel(false)
      .setWhen(System.currentTimeMillis());

    startIntent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true);
    builder.setChannelId(CHANNEL_ID);

    return builder.build();
  }
}
