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

import com.gphrost.Overplayed.R;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.view.Gravity;
import android.view.WindowManager;

/**
 * Service used to manage GameControllerViews and the NetworkThread.
 * 
 * @author Steven T. Ramzel
 */
public class MainService extends Service {
	public class LocalBinder extends Binder {
		MainService getService() {
			// Return this instance of LocalService so clients can call public
			// methods
			return MainService.this;
		}
	}

	static final String EXTRA_ADDRESS = "EXTRA_ADDRESS";
	// GameControllerViews
	public static final String EXTRA_PORT = "EXTRA_PORT";
	static Intent intent; // Intent used to call this service
	static NetworkThread thread; // Thread for network routine to run on
	// Binder used to bind to main activity
	private final IBinder mBinder = new LocalBinder();

	@Override
	public IBinder onBind(Intent intent) {
		MainService.intent = intent;

		// Create notification to call startForegroud()
		Notification note = new Notification(R.drawable.overplayed_logo,
				"Overplayed is running ...", System.currentTimeMillis());

		PendingIntent pi = PendingIntent.getService(this, 0, intent, 0);

		note.setLatestEventInfo(this, "Overplayed", "Overplayed is running", pi);

		// Notification stays until app is done
		note.flags |= Notification.FLAG_NO_CLEAR;

		// Start the service in the foreground so it stays until manually closed
		startForeground(1, note);

		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		// Create all the controls
		GameControllerView mView[] = new GameControllerView[15];
		GameControllerView.parentService = this;
		GameControllerView.updateStandardRadius(getResources());

		mView[0] = new Joystick(this, 3, (byte) 0, (byte) 1, Gravity.LEFT
				+ Gravity.BOTTOM, 0, 0);

		mView[1] = new Joystick(this, 3, (byte) 2, (byte) 3, Gravity.RIGHT
				+ Gravity.BOTTOM, 0, 0);

		mView[2] = new Button(this, 1f, (byte) 0, Gravity.RIGHT
				+ Gravity.BOTTOM, 2f, 6f, "A");// A - Cross
		mView[3] = new Button(this, 1f, (byte) 1, Gravity.RIGHT
				+ Gravity.BOTTOM, 0f, 8f, "B");// B - Circle
		mView[4] = new Button(this, 1f, (byte) 2, Gravity.RIGHT
				+ Gravity.BOTTOM, 4f, 8f, "X");// X - Square
		mView[5] = new Button(this, 1f, (byte) 3, Gravity.RIGHT
				+ Gravity.BOTTOM, 2f, 10f, "Y");// Y - Triangle

		mView[6] = new Button(this, 1f, (byte) 4,
				Gravity.LEFT + Gravity.BOTTOM, 4f, 12f, "L1");// L1
		mView[7] = new Button(this, 1f, (byte) 6,
				Gravity.LEFT + Gravity.BOTTOM, 0f, 12f, "L2");// L2
		mView[8] = new Button(this, 1f, (byte) 5, Gravity.RIGHT
				+ Gravity.BOTTOM, 4f, 12f, "R1");// R1
		mView[9] = new Button(this, 1f, (byte) 7, Gravity.RIGHT
				+ Gravity.BOTTOM, 0f, 12f, "R2");// R2

		mView[10] = new Button(this, 1f, (byte) 10, Gravity.LEFT
				+ Gravity.BOTTOM, 6f, 0f, "L3");// L3
		mView[11] = new Button(this, 1f, (byte) 11, Gravity.RIGHT
				+ Gravity.BOTTOM, 6f, 0f, "R3");// R3

		mView[12] = new Button(this, 1f, (byte) 9, Gravity.CENTER
				| Gravity.BOTTOM, 1f, 0, ">");// Start
		mView[13] = new Button(this, 1f, (byte) 8, Gravity.CENTER
				| Gravity.BOTTOM, -1f, 0, "<");// Select

		mView[14] = new DPad(this, 3f, (byte) 12, (byte) 13, (byte) 14,
				(byte) 15, Gravity.LEFT + Gravity.BOTTOM, 0f, 6f);// DPad

		GameControllerView.mView = mView;

		GameControllerView.stopButton = new StopButton(this, 1f, (byte) -1,
				Gravity.CENTER | Gravity.TOP, 0, 6, "QUIT");

		GameControllerView.hideButton = new DisableButton(this, 1f, (byte) -1, Gravity.CENTER
				| Gravity.TOP, 0, 0, "HIDE");
		
		GameControllerView.alphaButton = new AlphaButton(this, 1f, (byte) -1, Gravity.CENTER
				| Gravity.TOP, 0, 3, "ALPHA");
	}

	@Override
	public void onDestroy() {
		// Stop the NetworkThread
		thread.running = false;
		try {
			thread.join();
			thread = null;
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		if (GameControllerView.active)
			GameControllerView.setInactive();

		// Remove the last views and get out of Dodge
		GameControllerView.wm.removeView(GameControllerView.hideButton);
		GameControllerView.wm.removeView(GameControllerView.alphaButton);
		GameControllerView.wm.removeView(GameControllerView.stopButton);
		// This let's this service know it's not running
		GameControllerView.wm = null;
		// This tells UDPlay whether or not this service is running
		Overplayed.mService = null;
		
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// Extract the network address from the intent
		String address = intent.getStringExtra(EXTRA_ADDRESS);
		int port = intent.getIntExtra(EXTRA_PORT, 30000);

		// If the thread isn't running, RUN IT!!!
		if (thread == null) {
			thread = new NetworkThread(address, port);
			thread.running = true;
			thread.start();
			// Let the GameControllerViews know about it so they can interact
			// with it
			GameControllerView.thread = thread;
		}

		// This means the service was not already running, otherwise don't touch
		// anything
		if (GameControllerView.wm == null) {
			GameControllerView.wm = (WindowManager) getSystemService(WINDOW_SERVICE);
			// Add the hideButton so when we activate the controller we don't
			// get an exception
			GameControllerView.wm.addView(GameControllerView.hideButton, GameControllerView.hideButton.params);
			GameControllerView.wm.addView(GameControllerView.alphaButton, GameControllerView.alphaButton.params);
			// Also add the stop button because, god forbid, we want to stop
			// playing video games
			GameControllerView.wm.addView(GameControllerView.stopButton,
					GameControllerView.stopButton.params);
			GameControllerView.setActive();
		}

		return Service.START_NOT_STICKY;// When we press quit, the service
										// should die
	}
}