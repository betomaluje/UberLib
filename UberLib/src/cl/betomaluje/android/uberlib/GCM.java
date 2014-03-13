package cl.betomaluje.android.uberlib;

import java.io.IOException;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.util.Log;

public class GCM {

	public final String TAG = "GCM";
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";

	private GCMRegisterListener gcmListener = null;

	public interface GCMRegisterListener {
		/**
		 * Returns the register id from Google server
		 * 
		 * @param newRegister
		 *            : true if the register id from Google is new, false
		 *            otherwise
		 * @param registerId
		 *            : the retrieved register id from Google
		 */
		public void onRegisterCompleted(boolean newRegister, String registerId);

		/**
		 * Returns the error (if occurred) when trying to register to the Google
		 * servers
		 * 
		 * @param error
		 *            : the error found
		 */
		public void onError(String error);
	}

	private Context context;

	private GoogleCloudMessaging gcm;

	private String GCM_SENDER_ID = "";

	private String regid = "";

	public GCM(Context c, String senderId) {
		this.context = c;
		this.GCM_SENDER_ID = senderId;
	}

	public GCM(Context c) {
		this.context = c;
	}

	public void setSenderId(String s) {
		this.GCM_SENDER_ID = s;
	}

	public void setGCMListener(GCMRegisterListener l) {
		this.gcmListener = l;
	}

	public void register() {
		gcm = GoogleCloudMessaging.getInstance(context);
		regid = getRegistrationId(context);

		if (regid.equals("") || regid == null) {
			registerInBackground();
		} else {
			if (gcmListener != null)
				gcmListener.onRegisterCompleted(false, regid);
		}
	}

	/**
	 * Gets the current registration ID for application on GCM service.
	 * <p>
	 * If result is empty, the app needs to register.
	 * 
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	public String getRegistrationId(Context context) {
		final SharedPreferences prefs = getGCMPreferences(context);
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if (registrationId.equals("")) {
			Log.i(TAG, "Registration not found.");
			return "";
		}
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION,
				Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion) {
			Log.i(TAG, "App version changed.");
			return "";
		}
		return registrationId;
	}

	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and app versionCode in the application's
	 * sqlite database.
	 */
	private void registerInBackground() {

		if (GCM_SENDER_ID.equals(""))
			throw new NullPointerException(
					"You must provide a Server Id. Use setSenderId(String s) function first.");

		Log.i(TAG, "Starting registration process...");

		new AsyncTask<Void, Void, Boolean>() {
			@Override
			protected Boolean doInBackground(Void... params) {
				if (gcm == null)
					gcm = GoogleCloudMessaging.getInstance(context);

				try {
					regid = gcm.register(GCM_SENDER_ID);

					storeRegistrationId(context, regid);

					if (gcmListener != null)
						gcmListener.onRegisterCompleted(true, regid);

					return true;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					if (gcmListener != null)
						gcmListener.onError("Error: " + e.toString());
				}
				return false;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				Log.i(TAG, "onPostExecute: " + result);
			}
		}.execute(null, null, null);
	}

	/**
	 * Stores the registration ID and app versionCode in the application's
	 * sqlite database
	 * 
	 * @param context
	 *            application's context.
	 * @param regId
	 *            registration ID
	 */
	private void storeRegistrationId(Context context, String regId) {
		final SharedPreferences prefs = getGCMPreferences(context);
		int appVersion = getAppVersion(context);
		Log.i(TAG, "Saving regId on app version " + appVersion);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_REG_ID, regId);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
		editor.commit();
	}

	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private SharedPreferences getGCMPreferences(Context context) {
		// This sample app persists the registration ID in shared preferences,
		// but
		// how you store the regID in your app is up to you.
		return context.getSharedPreferences(context.getClass().getSimpleName(),
				Context.MODE_PRIVATE);
	}

	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

}
