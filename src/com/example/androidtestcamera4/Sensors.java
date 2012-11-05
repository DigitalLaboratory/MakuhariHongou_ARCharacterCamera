package com.example.androidtestcamera4;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


public class Sensors {

	private final static String TAG = Sensors.class.getSimpleName();

	private Context context;
	private LocationManager locationManager;
	private SensorManager sensorManager;
	private Sensor accelerometer;
	private Sensor magneticFieldSensor;
	private Sensor gyroscope;
	private Geocoder geocoder;

	public Sensors(Context context) {
		this.context = context;
		locationManager = (LocationManager)context.getSystemService(Activity.LOCATION_SERVICE);
		sensorManager = (SensorManager)context.getSystemService(Activity.SENSOR_SERVICE);
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		Log.d(TAG, "accelerometer: " + accelerometer.getName());
		Log.d(TAG, "magneticFieldSensor: " + magneticFieldSensor.getName());
		Log.d(TAG, "gyroscope: " + gyroscope.getName());
		geocoder = new Geocoder(context, Locale.getDefault());
	}

	public void start() {
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0.0f, locationListener);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0.0f, locationListener);
		sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_UI);
		sensorManager.registerListener(sensorEventListener, magneticFieldSensor, SensorManager.SENSOR_DELAY_UI);
		sensorManager.registerListener(sensorEventListener, gyroscope, SensorManager.SENSOR_DELAY_UI);
	}

	public void stop() {
		locationManager.removeUpdates(locationListener);
		sensorManager.unregisterListener(sensorEventListener, accelerometer);
		sensorManager.unregisterListener(sensorEventListener, magneticFieldSensor);
		sensorManager.unregisterListener(sensorEventListener, gyroscope);
	}

	private Location location = null;

	private LocationListener locationListener = new LocationListener() {
		@Override
		public void onLocationChanged(Location location) {
			String sProvider = location.getProvider();
			Log.d(TAG, String.format("location provider %s changed the location.", sProvider));
			synchronized (locationListener) {
				Sensors.this.location = location;
			}
			String s = String.format("latitude:%+.3f\nlongitude:%+.3f\naltitude:%+.3f\naccuracy:%+.3f\n%s",
					location.getLatitude(),
					location.getLongitude(),
					location.getAltitude(),
					location.getAccuracy(),
					new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(location.getTime())));
//			Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onProviderDisabled(String sProvider) {
			Log.d(TAG, "onProviderDisabled: " + sProvider);
			Toast.makeText(context, sProvider + " is disabled.", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onProviderEnabled(String sProvider) {
			Log.d(TAG, "onProviderEnabled: " + sProvider);
			Toast.makeText(context, sProvider + " is enabled.", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	};

	private SensorEventListener sensorEventListener = new SensorEventListener() {
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			Log.d(TAG, String.format("sensor %s changed the accuracy: %d", sensor.getName(), accuracy));
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			String sSensorName = event.sensor.getName();
			if (accelerometer.getName().equals(sSensorName)) {
				
			} else if (magneticFieldSensor.getName().equals(sSensorName)) {
				
			} else if (gyroscope.getName().equals(sSensorName)) {
				
			} else {
				Log.e(TAG, "unknown sensor: " + sSensorName);
				return;
			}
		}
	};

	public Location getLocation() {
		synchronized (locationListener) {
			return location;
		}
	}

	public String[] getAddressLines() {
		synchronized (locationListener) {
			if (location == null) {
				return null;
			}
			try {
				List<Address> addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
				if (addressList == null) {
					return null;
				}
				Address address = addressList.get(0);
				ArrayList<String> sAddressLineList = new ArrayList<String>();
				for (int i = 0;  ;  ++i) {
					String sAddressLine = address.getAddressLine(i);
					if (sAddressLine != null) {
						sAddressLineList.add(sAddressLine);
					} else {
						break;
					}
				}
				return sAddressLineList.toArray(new String[sAddressLineList.size()]);
			} catch (IOException e) {
				Log.e(TAG, e.getMessage(), e);
				return null;
			}
		}
	}
}
