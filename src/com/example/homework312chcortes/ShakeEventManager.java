package com.example.homework312chcortes;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class ShakeEventManager implements SensorEventListener {

	private static final int FORCE_THRESHOLD = 350;
	  private static final int TIME_THRESHOLD = 100;
	  private static final int SHAKE_TIMEOUT = 500;
	  private static final int SHAKE_DURATION = 1000;
	  private static final int SHAKE_COUNT = 3;

	  private SensorManager mSensorMgr;
	  private float mLastX=-1.0f, mLastY=-1.0f, mLastZ=-1.0f;
	  private long mLastTime;
	  private OnShakeListener mShakeListener;
	  private Context mContext;
	  private int mShakeCount = 0;
	  private long mLastShake;
	  private long mLastForce;

	  public interface OnShakeListener
	  {
	    public void onShake();
	  }

	  public ShakeEventManager(Context context) 
	  { 
	    mContext = context;
	    resume();
	  }
	  
	  public void resume() {
		mSensorMgr = (SensorManager)mContext.getSystemService(Context.SENSOR_SERVICE);
		if (mSensorMgr == null) {
		   throw new UnsupportedOperationException("Sensors not supported");
		}
		
		boolean supported = mSensorMgr.registerListener(this, mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
		
		if (!supported) {
			mSensorMgr.unregisterListener(this);
		  throw new UnsupportedOperationException("Accelerometer not supported");
		}
	  }
	  
	  public void pause() {
		 if (mSensorMgr != null) {
		   mSensorMgr.unregisterListener(this);
		   mSensorMgr = null;
		 }
	  }

	  public void setOnShakeListener(OnShakeListener listener)
	  {
	    mShakeListener = listener;
	  }
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// This page intentionally left blank
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) return;
	    long now = System.currentTimeMillis();

	    if ((now - mLastForce) > SHAKE_TIMEOUT) {
	      mShakeCount = 0;
	    }

	    if ((now - mLastTime) > TIME_THRESHOLD) {
	      long diff = now - mLastTime;
	      float speed = Math.abs(event.values[0] + event.values[1] + event.values[2] - mLastX - mLastY - mLastZ) / diff * 10000;
	      if (speed > FORCE_THRESHOLD) {
	        if ((++mShakeCount >= SHAKE_COUNT) && (now - mLastShake > SHAKE_DURATION)) {
	          mLastShake = now;
	          mShakeCount = 0;
	          if (mShakeListener != null) { 
	            mShakeListener.onShake(); 
	          }
	        }
	        mLastForce = now;
	      }
	      mLastTime = now;
	      mLastX = event.values[0];
	      mLastY = event.values[1];
	      mLastZ = event.values[2];
	    }

		
	}
	

	
	

}
