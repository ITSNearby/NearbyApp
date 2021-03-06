package com.progetto.nearby;

import java.util.ArrayList;
import java.util.HashSet;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.progetto.nearby.R;
import com.progetto.nearby.home.HomeActivity;
import com.progetto.nearby.models.Offerta;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;


public class GpsService extends Service {
	private static final String TAG = "GpsService";
	private static LocationManager mLocationManager = null;
	private static final int LOCATION_INTERVAL = 20000; // 20 secondi
	private static final float LOCATION_DISTANCE = 20; //20 metri

	private static Location mLastLocation;
	private static ArrayList<LocationListener> mLocationListeners = new ArrayList<LocationListener>();
	
	public static final int NOTIFICATION_ID = 9999;

	private final IBinder mBinder = new LocalBinder();
	
	public class LocalBinder extends Binder {
        public GpsService getService() {
            return GpsService.this;
        }
    }
	
	
	private class OfferteLocationListener implements LocationListener {
	    
		private HashSet<Integer> lstOfferteVicine = new HashSet<Integer>();
		
	    @Override
	    public void onLocationChanged(Location location)
	    {
	        Log.w(TAG, "onLocationChanged: " + location);
	        mLastLocation.set(location);
	        //Toast.makeText(getApplicationContext(), "cerco offerte", Toast.LENGTH_SHORT).show();
	        
	        if(Tools.isNetworkEnabled(getApplicationContext())) {
				AsyncHttpClient client = new AsyncHttpClient();
				
				int range = getApplicationContext()
						.getSharedPreferences(Tools.PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)
						.getInt(Tools.PREFERNCES_DISTANZA, Tools.FILTRO_DISTANZA_DEFAULT);
				
				String url = Tools.OFFERS_BY_GPS_URL +
						mLastLocation.getLatitude() +
						"&" + mLastLocation.getLongitude() +
						"&" + range;
				
				client.get(url, new JsonHttpResponseHandler(){
					@Override
					public void onSuccess(int statusCode, Header[] headers,	JSONArray response) {
						if(response.length() > 0) {
							JSONObject offerta;
							int idOfferta;
							int counterNuoveOfferte = 0;
							HashSet<Integer> lstNuoveOfferte = new HashSet<Integer>(response.length());
							try {
								for (int i = 0; i < response.length(); i++) {
									offerta = response.getJSONObject(i);
									idOfferta = offerta.getInt(Offerta.tag_id);
									if(!lstOfferteVicine.contains(idOfferta)) {
										counterNuoveOfferte++;
									}
									lstNuoveOfferte.add(idOfferta);
								}
							} catch (JSONException e) { e.printStackTrace(); }
							
							lstOfferteVicine.clear();
							lstOfferteVicine = lstNuoveOfferte;
							
							if (counterNuoveOfferte > 0) {
								showNotification(response.length());
							}
						}
					}	
					
					@Override
					public void onFailure(int statusCode, Header[] headers,
							String responseString, Throwable throwable) {
						// TODO toast solo per debug
						Toast.makeText(getApplicationContext(), "Errore nel recupero dei dati", Toast.LENGTH_LONG).show();
						super.onFailure(statusCode, headers, responseString, throwable);
					}
				});
			} else {
				Toast.makeText(getApplicationContext(), "Nessuna connessione disponibile!", Toast.LENGTH_LONG).show();
			}
	    }
	    
	    private void showNotification(int numOfferte) {
	    	Intent notificationIntent = new Intent(getApplicationContext(), HomeActivity.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);

			String message = (numOfferte > 1 ? "Ci sono " + numOfferte + " nuove offerte nella tua zona!" : "C'� una nuova offerta nella tua zona");
			NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			Notification notification = new Notification.Builder(getApplicationContext())
				.setContentTitle("Nuove offerte!")
				.setContentText(message)
		        .setSmallIcon(R.drawable.ic_local_offer_white_24dp)
		        .setContentIntent(pendingIntent)
		        .setAutoCancel(true)
		        .build();
		    
		    mNotificationManager.notify(NOTIFICATION_ID, notification);
	    }
	    
	    @Override
	    public void onProviderDisabled(String provider) { }
	    @Override
	    public void onProviderEnabled(String provider) { }
	    @Override
	    public void onStatusChanged(String provider, int status, Bundle extras) { }
	} 
	
	
	
	@Override
	public IBinder onBind(Intent arg0)
	{
	    return mBinder;
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
	    super.onStartCommand(intent, flags, startId);       
	    return START_STICKY;
	}
	@Override
	public void onCreate()
	{
	    initializeLocationManager();
	    
	    registerLocationListener(new OfferteLocationListener(), LocationManager.NETWORK_PROVIDER);
	    registerLocationListener(new OfferteLocationListener(), LocationManager.GPS_PROVIDER);
	}
	@Override
	public void onDestroy()
	{
	    Log.w(TAG, "onDestroy");
	    super.onDestroy();
	    if (mLocationManager != null) {
	    	for (LocationListener listener : mLocationListeners) {
	    		try {
	                mLocationManager.removeUpdates(listener);
	            } catch (Exception ex) {
	                ex.printStackTrace();
	            }
			}
	    	mLocationListeners.clear();
	    }
	}
	private void initializeLocationManager() {
	    if (mLocationManager == null) {
	        mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
	        mLastLocation = new Location(LocationManager.NETWORK_PROVIDER);
	    }
	}
	
	
	public static boolean isLocationEnabled() {
		return mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
	}
	
	public static LatLng getLastKnownLocation() {
		String provider = "";
		if(mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			provider = LocationManager.GPS_PROVIDER;
		} else if(mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			provider = LocationManager.NETWORK_PROVIDER;
		}
		Location location = mLocationManager.getLastKnownLocation(provider);
		return new LatLng(location.getLatitude(), location.getLongitude());
	}
	
	public static double getLatitude() {
		return mLastLocation.getLatitude();
	}
	
	public static double getLongitude() {
		return mLastLocation.getLongitude();
	}
	
	public static void registerLocationListener(LocationListener listener, String provider) {
		try {
	        mLocationManager.requestLocationUpdates(provider, LOCATION_INTERVAL, LOCATION_DISTANCE, listener);
	        mLocationListeners.add(listener);
	    } catch (java.lang.SecurityException ex) {
	        Log.w(TAG, "register fail", ex);
	    } catch (IllegalArgumentException ex) {
	        Log.w(TAG, "" + ex.getMessage());
	    }
	}
	
	public static void unregisterListener(LocationListener listener) {
		mLocationManager.removeUpdates(listener);
		mLocationListeners.remove(listener);
	}
}
