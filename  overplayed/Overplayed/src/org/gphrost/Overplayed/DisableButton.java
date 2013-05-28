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
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.WindowManager;

/**
 * Button used to toggle controller visibility.
 * 
 * @author Steven T. Ramzel
 * @see GameControllerView
 */
// Button is not called from xml, so not implementing proper constructor
@SuppressLint("ViewConstructor")
public class DisableButton extends Button {

	/**
	 * @param context
	 *            Application context.
	 * @param radiusScale
	 *            Radius of button as multiple of auto-generated standard button
	 *            radius.
	 * @param buttonIndex
	 *            Not used.
	 * @param gravity
	 *            Placement of button within the screen as per Gravity.
	 * @param xOffset
	 *            X position for this button.
	 * @param yOffset
	 *            Y position for this button.
	 * @param label
	 *            Text on button face.
	 */
	public DisableButton(Context context, float radiusScale, byte buttonIndex,
			int gravity, int xOffset, int yOffset, String label) {
		super(context, radiusScale, buttonIndex, gravity, xOffset, yOffset,
				label);
		params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
				| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getActionMasked();
		if (action == MotionEvent.ACTION_DOWN) {
			if (active)
				setInactive();
			else
				setActive();
		}
		return true;
	}
	
	public void onDraw(Canvas canvas) {
		canvas.drawBitmap(buttonBitmap, 0f, 0f, halfPaint);
	}
}
