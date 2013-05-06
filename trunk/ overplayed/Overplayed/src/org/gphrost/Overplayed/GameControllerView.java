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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

/**
 * Game controller parent class.
 * 
 * @author Steven T. Ramzel
 */
abstract class GameControllerView extends View {
	// The visibility state of the game controller
	protected static boolean active = true;
	// Analog values for the network packet
	protected static short[] analog = new short[] { 16383, 16383, 16383, 16383 };
	// Digital button values for the network packet
	protected static boolean[] buttons = new boolean[] { false, false, false,
			false, false, false, false, false, false, false, false, false,
			false, false, false, false, false };
	private static Rect display = new Rect(); // Used to hold display
												// measurements
	// Opaque paint for rendering button bitmaps
	protected static final Paint downPaint = new Paint();
	// Paint used for creating button bitmaps
	protected static final Paint fillPaint = new Paint();
	// scale
	static GameControllerView mView[]; // Collection of controls
	protected static final int padding = 2; // Padding for drawing circles

	static MainService parentService; // Handle for the MainService
	protected static long refresh = 1000; // Last network ping
	private static int standardRadius; // Radius used for proper sizing and
	static GameControllerView stopButton; // Button used to quit
	static NetworkThread thread; // Handle for the thread
	// Transparent paint for rendering button bitmaps
	protected static final Paint upPaint = new Paint();
	static WindowManager wm; // Handle for the WindowManager

	/**
	 * Function of a circle
	 * 
	 * @param radius
	 *            Radius of the circle
	 * @param x
	 *            X input to function
	 * @return Y coordinate of circle
	 */
	@SuppressLint("FloatMath")
	public static float circleY(float radius, float x) {
		return FloatMath.sqrt(radius * radius - x * x);
	}

	/**
	 * @param x
	 *            X coordinate in pixels with respect to the center of the
	 *            button
	 * @param y
	 *            Y coordinate in pixels with respect to the center of the
	 *            button
	 * @return
	 */
	public static boolean inRadius(float radius, float x, float y) {
		float xWidth = GameControllerView.circleY(radius, y);
		float yWidth = GameControllerView.circleY(radius, x);
		return (((x < xWidth) && (x > -xWidth) && ((y < yWidth)) && (y > -yWidth)));
	}

	/**
	 * Make the controller visible by adding the control views to the
	 * WindowManager. This does not check whether or not the views being
	 * manipulated are already added to the WindowManager and thus might throw
	 * an exception.
	 */
	public static void setActive() {
		active = true;
		for (GameControllerView view : mView) {
			// Update layout params, the standardRadius may have changed since
			// it was set inactive.
			view.updateParams(view.radiusScale, view.xOffsetScale,
					view.yOffsetScale);
			view.generateBitmap((int) view.radius);
			view.invalidate();
			wm.addView(view, view.params);
		}
		wm.removeView(stopButton); // Hide the quit button
	}

	/**
	 * Make the controller invisible by removing the control views from the
	 * WindowManager. This does not check whether or not the views being
	 * manipulated are already removed from the WindowManager and thus might
	 * throw an exception.
	 */
	public static void setInactive() {
		active = false;
		for (GameControllerView view : mView) {
			wm.removeView(view);
		}
		// Update layout params for the stop button, the standardRadius may have
		// changed since it was last hidden.
		stopButton.updateParams(stopButton.radiusScale,
				stopButton.xOffsetScale, stopButton.yOffsetScale);
		stopButton.generateBitmap((int) stopButton.radius);
		stopButton.invalidate();
		wm.addView(stopButton, stopButton.params); // Show the quit button
	}

	/**
	 * Updates the standard radius so buttons are scaled to appropriately fit on
	 * screen
	 * 
	 * @param resource
	 *            Resources object used to retrieve display information
	 */
	static void updateStandardRadius(Resources resource) {
		DisplayMetrics metrics = resource.getDisplayMetrics();
		standardRadius = (int) Math.min(
				// The standardRadius is smallest of either 1/7th of an inch,
				// 1/14th the screen height , or 1/20th the screen width
				// This is so everything fits on the screen
				Math.min(metrics.xdpi / 7f, metrics.widthPixels / 20f),
				Math.min(metrics.ydpi / 7f, metrics.heightPixels / 14f));
	}

	protected boolean isDown = false; // State of control
	// WindowManager
	private long nextRenderTime; // Next time to render
	protected LayoutParams params; // Parameters used to attach view to
	// pressed by the same finger as another
	// button
	private int pointerID = -1; // Current index of MotionEvent
								// screen
	protected float radius; // Radius of button in pixels
	private float radiusScale; // Radius of button in multiples of
								// standardRadius with respect to gravity
	private boolean secondary = false; // Flag whether or not this is a button

	protected int xOffset; // X position of control with respect of entire

	// standardRadius
	private float xOffsetScale; // X position of control as multiples of

	// screen
	protected int yOffset; // Y position of control with respect of entire

	// standardRadius with respect to gravity
	private float yOffsetScale; // Y position of control as multiples of

	{
		downPaint.setARGB(255, 128, 128, 255);
		upPaint.setARGB(128, 255, 255, 255);
		fillPaint.setARGB(255, 192, 192, 192);
		fillPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		fillPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
	}

	public GameControllerView(Context context) {
		super(context);
	}

	/**
	 * Sets the control as down.
	 * 
	 * @param view
	 *            The control to set as down.
	 */
	void down(MotionEvent event, GameControllerView view, int p, int pID) {
		// Haptick feedback
		performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
		view.pointerID = pID;
		view.isDown = true;
		view.updateStatus(event.getX(p) + xOffset, event.getY(p) + yOffset);
		view.invalidate();
	}

	/**
	 * Put the MotionEvent coordinates in respect to the center of the control
	 * and check if in button radius with inRadius()
	 * 
	 * @param control
	 *            The control to check
	 * @param event
	 *            The MotionEvent to use
	 * @param p
	 *            This pointer index of the coordinates in the MotionEvent to be
	 *            checked
	 * @return true if the pointer is within control's radius
	 */
	public boolean inBounds(GameControllerView control, MotionEvent event, int p) {
		return inRadius(control.radius, event.getX(p) + xOffset
				- control.xOffset - control.radius, event.getY(p) + yOffset
				- control.yOffset - control.radius);
	}

	@Override
	public void invalidate() {
		super.invalidate();
		thread.changed = true;
	}

	@Override
	protected void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		updateStandardRadius(getResources()); // standardRadius may need to be
												// recalculated
		updateParams(radiusScale, xOffsetScale, yOffsetScale); // Update layout
																// params with
																// new
																// standardRadius
		generateBitmap((int) radius); // Update rendering bitmap with new
									// standardRadius
		invalidate(); // Imediately redraw the control
		wm.updateViewLayout(this, params); // Update the control layout
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// Keyboard buttons 1-9 are bound the to their respective button indices
		if (event.isPrintingKey()) {
			int num = Character.getNumericValue(event.getDisplayLabel());
			if (0 <= num && num <= 9)
				buttons[num] = true;
			return true;
		}

		// Handle DPAD
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_CENTER:
			buttons[10] = true;
			return true;
		case KeyEvent.KEYCODE_ENTER:
			buttons[11] = true;
			return true;
		case KeyEvent.KEYCODE_DPAD_DOWN:
			buttons[12] = true;
			return true;
		case KeyEvent.KEYCODE_DPAD_LEFT:
			buttons[13] = true;
			return true;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			buttons[14] = true;
			return true;
		case KeyEvent.KEYCODE_DPAD_UP:
			buttons[15] = true;
			return true;
		case KeyEvent.KEYCODE_SPACE:
			buttons[16] = true;
		default:
			return super.onKeyDown(keyCode, event);
		}

	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// Keyboard buttons 1-9 are bound the to their respective button indices
		if (event.isPrintingKey()) {
			int num = Character.getNumericValue(event.getDisplayLabel());
			if (0 <= num && num <= 9)
				buttons[num] = false;
			return true;
		}

		// Handle DPAD
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_CENTER:
			buttons[10] = false;
			return true;
		case KeyEvent.KEYCODE_ENTER:
			buttons[11] = false;
			return true;
		case KeyEvent.KEYCODE_DPAD_DOWN:
			buttons[12] = false;
			return true;
		case KeyEvent.KEYCODE_DPAD_LEFT:
			buttons[13] = false;
			return true;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			buttons[14] = false;
			return true;
		case KeyEvent.KEYCODE_DPAD_UP:
			buttons[15] = false;
			return true;
		case KeyEvent.KEYCODE_SPACE:
			buttons[16] = false;
			return true;
		default:
			return super.onKeyDown(keyCode, event);
		}
	}

	@Override
	public void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		updateOffset();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getActionMasked();

		if (action == MotionEvent.ACTION_DOWN) {// An untouched control view is
												// now touched
			// Check if it's within the controls radius (event.getX() and getY()
			// is in respect to control view,
			// so just subtract the radius so coordinates are in respect to axis
			// origin
			if (inRadius(radius, event.getX() - radius, event.getY() - radius)) {
				// A little haptick feedback
				performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
				pointerID = event.getPointerId(0);// Set the pointer ID
				isDown = true;// Update button state
				updateStatus(event.getX() + xOffset, event.getY() + yOffset);
				// Immediately invalidate
				invalidate();
			}
		} else
			// We don't know which control the touch has interacted with so
			// check them all
			for (GameControllerView view : mView) {
				if (action < 4) {
					// The action is either ACTION_MOVE or ACTION_UP
					// These actions can be analyzed by using pointer IDs less
					// than getPointerCount()
					for (int p = 0; p < event.getPointerCount(); p++) {
						// Get the global pointer ID for comparison with the
						// control's saved index
						int pID = event.getPointerId(p);
						if (action == MotionEvent.ACTION_MOVE) {
							// An already down pointer has moved
							if (view.pointerID == pID) {
								// The pointer belongs to this control
								if (view.secondary && !inBounds(view, event, p))
									// This is a button pressed by a pointer
									// that started its touch from another
									// button, but the pointer has moved from
									// this button's radius so change its state
									// to not down
									up(view);
								else {
									// This is a pointer thats already pressing
									// a control and has moved so update its
									// status
									view.updateStatus(event.getX(p) + xOffset,
											event.getY(p) + yOffset);
									// Update when reasonable
									view.restrictedInvalidate();
								}
							} else if (getClass() == Button.class
									&& view.getClass() == Button.class
									&& !view.isDown && inBounds(view, event, p)) {
								// This pointer has moved from the calling
								// button-view to this button, so we can set
								// this
								// to down and flag it as a secondary press
								down(event, view, pID, pID);
								view.secondary = true;
							}
						} else if (action == MotionEvent.ACTION_UP
								&& view.pointerID == pID)
							// The pointer attached to this control has been
							// lifted, so update accordingly
							up(view);
					}
				} else { // This is a multi-touch action
					// Multi-touch actions have the index given by
					// getActionIndex()
					int p = event.getActionIndex();
					// Get the global pointer ID for comparison with control's
					// saved index
					int pID = event.getPointerId(p);

					if (action == MotionEvent.ACTION_POINTER_DOWN
							&& inBounds(view, event, p))
						// An untouched control view is now touched
						down(event, view, pID, pID);
					else if (action == MotionEvent.ACTION_POINTER_UP
							&& view.pointerID == pID)
						// The pointer attached to this control has been lifted,
						// so update accordingly
						up(view);
				}
			}

		return true;
	}

	/**
	 * Invalidate only if the amount of time equal to the network ping has
	 * elapsed. Overloaded by Buttons class, which do not redraw during being
	 * pressed.
	 */
	public void restrictedInvalidate() {
		if (System.currentTimeMillis() > nextRenderTime) {
			invalidate();
			nextRenderTime = System.currentTimeMillis() + refresh;
		}
	}

	/**
	 * @param radiusScale
	 *            Radius of button as multiple of auto-generated standard button
	 *            radius.
	 * @param gravity
	 *            Placement of button within the screen as per Gravity.
	 * @param xOffset
	 *            X position for this button.
	 * @param yOffset
	 *            Y position for this button.
	 */
	void set(float radiusScale, int gravity, float xOffset, float yOffset) {
		this.setHapticFeedbackEnabled(true);
		this.radiusScale = radiusScale;
		params = new WindowManager.LayoutParams(
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
				// Go on top of everything, I would do TYPE_SYSTEM_ALERT but it
				// messes with the notification bar.
				WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
				// Let touch events pass to other apps
				WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
				// Let the background of controls be transparent
				PixelFormat.TRANSLUCENT);
		params.gravity = gravity;
		updateParams(radiusScale, xOffset, yOffset);
		generateBitmap((int) radius);
	}

	/**
	 * Sets the control as not down.
	 * 
	 * @param view
	 *            The control to set as not down.
	 */
	void up(GameControllerView view) {
		// Haptick feedback when the pointer is lifted
		performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
		view.pointerID = -1;
		view.isDown = false;
		view.secondary = false;
		// Set the input to the center of the control (for d-pads and joysticks)
		view.updateStatus(view.radius + view.xOffset, view.radius
				+ view.yOffset);
		view.invalidate();
	}

	/**
	 * Updates bitmap used for drawing control.
	 * 
	 * @param radius
	 *            Radius of control in pixels.
	 */
	abstract void generateBitmap(int radiusScale);

	/**
	 * Calculates xOffset and yOffset based on gravity and coordinates from
	 * layout params
	 */
	void updateOffset() {
		getRootView().getWindowVisibleDisplayFrame(display);
		if ((params.gravity & Gravity.CENTER) == Gravity.CENTER) {
			xOffset = (display.right - display.left - params.width) / 2
					+ params.x;
			yOffset = (display.bottom - display.top - params.height) / 2
					+ params.y;
		}

		if ((params.gravity & Gravity.LEFT) == Gravity.LEFT)
			xOffset = display.left + params.x;
		else if ((params.gravity & Gravity.RIGHT) == Gravity.RIGHT)
			xOffset = display.right - params.height - params.x;

		if ((params.gravity & Gravity.TOP) == Gravity.TOP)
			yOffset = display.top + params.y;
		else if ((params.gravity & Gravity.BOTTOM) == Gravity.BOTTOM) {
			yOffset = display.bottom - params.width - params.y;
		}
	}

	/**
	 * @param radiusScale
	 *            Radius of button as multiple of auto-generated standard button
	 *            radius.
	 * @param xOffsetScale
	 *            X position for this button in terms of standardRadius.
	 * @param yOffsetScale
	 *            Y position for this button in terms of standardRadius.
	 */
	private void updateParams(float radiusScale, float xOffsetScale,
			float yOffsetScale) {
		this.radius = radiusScale * standardRadius;
		this.xOffsetScale = xOffsetScale;
		this.yOffsetScale = yOffsetScale;
		params.x = (int) (xOffsetScale * standardRadius + .5f);// +.5 to round
		params.y = (int) (yOffsetScale * standardRadius + .5f);

		params.height = (int) (2f * radiusScale * standardRadius + .5f);
		params.width = (int) (2f * radiusScale * standardRadius + .5f);
	}

	/**
	 * Updates control status for next network send.
	 * 
	 * @param screenX
	 *            X coordinate of touch with respect to entire screen.
	 * @param screenY
	 *            Y coordinate of touch with respect to entire screen.
	 */
	abstract void updateStatus(float x, float y);
}