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

import com.gphrost.Overplayed.R;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
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
			//Hey! the program's already started!
			finish();
			return;
		}
		setContentView(R.layout.main);
		
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
		
		String address = ((EditText) findViewById(R.id.edit_message)).getText()
				.toString();
		if (address.length() > 0 && new isNetGood().execute(address).get()) {
			i.putExtra(MainService.EXTRA_ADDRESS, address);
			startService(i);
			bindService(i, mConnection, BIND_AUTO_CREATE);
		} else
			Toast.makeText(this, "Invalid address", Toast.LENGTH_SHORT).show();
	}
}
