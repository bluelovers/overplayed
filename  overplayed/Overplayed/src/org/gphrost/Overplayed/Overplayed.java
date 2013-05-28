/* Copyright (c) 2013 All Right Reserved Steven T. Ramzel
 *
 *	This file is part of Overplayed.
 *
 *	Overplayed is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU Lesser General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	Overplayed is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU Lesser General Public License for more details.
 *
 *	You should have received a copy of the GNU Lesser General Public License
 *	along with Overplayed.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.gphrost.Overplayed;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

import org.gphrost.Overplayed.MainService.LocalBinder;
import org.json.JSONArray;
import org.json.JSONException;

import com.gphrost.Overplayed.R;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

/**
 * Overplayed main Activity
 * 
 * @author Steven T. Ramzel
 */
public class Overplayed extends Activity {

	/**
	 * AsyncTask that checks the validity of a hostname
	 * 
	 * @author Steven T. Ramzel
	 */
	private class isNetGood extends AsyncTask<String, Integer, Boolean> {
		@Override
		protected Boolean doInBackground(String... address) {
			try {
				InetAddress.getByName(address[0]);
			} catch (UnknownHostException e) {
				return false;
			}
			return true;
		}

	}

	static MainService mService;
	Intent i;
	static final String HOST_HIST_PREFS_NAME = "HostHistory";

	/** Defines callbacks for service binding, passed to bindService() */
	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder service) {
			LocalBinder binder = (LocalBinder) service;
			mService = binder.getService();
			Overplayed.this.unbindService(mConnection);
			Overplayed.this.finish();
		}

		public void onServiceDisconnected(ComponentName arg0) {
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (mService != null) {
			// Hey! the program's already started!
			finish();
			return;
		}
		setContentView(R.layout.main);

		// Restore preferences
		SharedPreferences settings = getSharedPreferences(HOST_HIST_PREFS_NAME,
				0);
		String lastHost = settings.getString("lastHost", "");
		if (lastHost.length() > 0)
			((EditText) findViewById(R.id.edit_message)).setText(lastHost);

		String lastPort = settings.getString("lastPort", getResources()
				.getString(R.string.port_default));
		((EditText) findViewById(R.id.edit_port)).setText(lastPort);
		GameControllerView.alpha = settings.getFloat("alpha", .5f);
		getStringArrayPref(this, "boundButtons",
				GameControllerView.boundButtons);
		getStringArrayPref(this, "boundAxis", GameControllerView.boundAxis);

		final EditText editText = (EditText) findViewById(R.id.edit_port);
		editText.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId,
					android.view.KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_GO) {
					try {
						startController(editText);
						return true;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				return false;
			}
		});
	}

	public void startController(View view) throws InterruptedException,
			ExecutionException {
		i = new Intent(this, MainService.class);
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);

		String portString = ((EditText) findViewById(R.id.edit_port)).getText()
				.toString();
		if (portString.length() > 0)
			i.putExtra(MainService.EXTRA_PORT, Integer.parseInt(portString));

		String addressString = ((EditText) findViewById(R.id.edit_message))
				.getText().toString();
		if (addressString.length() > 0
				&& new isNetGood().execute(addressString).get()) {
			// We need an Editor object to make preference changes.
			SharedPreferences settings = getSharedPreferences(
					HOST_HIST_PREFS_NAME, 0);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString("lastHost", addressString);
			editor.putString("lastPort", portString);
			// Commit the edits!
			editor.commit();

			// Start
			i.putExtra(MainService.EXTRA_ADDRESS, addressString);
			startService(i);
			bindService(i, mConnection, BIND_AUTO_CREATE);
		} else
			Toast.makeText(this, "Invalid address", Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if (item.getItemId() == R.id.config) {
			Intent i = new Intent(this, JoystickConfig.class);
			this.startActivity(i);
			return true;
		}
		return false;
	}

	public static void setStringArrayPref(Context context, String key,
			int[] values) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		JSONArray a = new JSONArray();
		for (int i = 0; i < values.length; i++) {
			a.put(values[i]);
		}
		if (values.length > 0)
			editor.putString(key, a.toString());
		else
			editor.putString(key, null);
		editor.commit();
	}

	public static void getStringArrayPref(Context context, String key,
			int[] values) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		String json = prefs.getString(key, null);
		if (json != null) {
			try {
				JSONArray a = new JSONArray(json);
				for (int i = 0; i < a.length(); i++) {
					int value = a.optInt(i, GameControllerView.boundButtons[i]);
					values[i] = value;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
}
