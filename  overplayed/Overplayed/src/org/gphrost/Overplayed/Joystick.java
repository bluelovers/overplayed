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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;

/**
 * Game controller joystick.
 * 
 * @author Steven T. Ramzel
 * @see GameControllerView
 */
@SuppressLint("ViewConstructor")
public class Joystick extends GameControllerView {
	private Bitmap gate; // Bitmap used for drawing the joystick gate
	// Half of the radius, for rendering
	private float halfRadius;
	private Bitmap handle; // Bitmap used for drawing the joystick handle
	private byte joyXIndex; // Axis index to map x-axis to
	private byte joyYIndex; // Axis index to map y-axis to
	private float screenX; // X coordinate of joystick handle
	private float screenY; // Y coordinate of joystick handle

	/**
	 * @param context
	 *            Application context.
	 * @param radiusScale
	 *            Radius of button as multiple of auto-generated standard button
	 *            radius.
	 * @param indexX
	 *            Axis index to map x-axis to
	 * @param indexY
	 *            Axis index to map y-axis to
	 * @param gravity
	 *            Placement of button within the screen as per Gravity.
	 * @param xOffset
	 *            X position for this button.
	 * @param yOffset
	 *            Y position for this button.
	 */
	public Joystick(Context context, int radiusScale, byte indexX, byte indexY,
			int gravity, int xOffset, int yOffset) {
		super(context);
		joyXIndex = indexX;
		joyYIndex = indexY;
		set(radiusScale, gravity, xOffset, yOffset);
		updateStatus(radius + xOffset, radius + yOffset);
	}

	/**
	 * Generates bitmaps used for drawing joystick.
	 * 
	 * @param radius
	 *            Radius of joystick in pixels.
	 */
	@Override
	void generateBitmap(int radius) {
		// Size and shape of joystick gate
		ShapeDrawable gateShape = new ShapeDrawable();
		gateShape.setShape(new OvalShape());
		gateShape.setBounds(padding, padding, radius * 2 - padding, radius * 2
				- padding);

		// Size and shape of joystick handle
		ShapeDrawable handleShape = new ShapeDrawable();
		handleShape.setShape(new OvalShape());
		handleShape.setBounds(new Rect(padding, padding, radius - padding,
				radius - padding));

		// Create joystick gate bitmap and render shape
		gate = Bitmap.createBitmap(radius * 2, radius * 2,
				Bitmap.Config.ARGB_4444);
		Canvas gateCanvas = new Canvas(gate);
		gateShape.getPaint().set(fillPaint);
		gateShape.draw(gateCanvas);

		// Create joystick handle bitmap and render shape
		handle = Bitmap.createBitmap(radius, radius, Bitmap.Config.ARGB_4444);
		Canvas handleCanvas = new Canvas(handle);
		handleShape.getPaint().set(fillPaint);
		handleShape.draw(handleCanvas);

		// Used to draw
		halfRadius = radius * .5f;
	}

	@Override
	public void onDraw(Canvas canvas, float xOffset, float yOffset) {
		// Draw the joystick gate behind the handle
		canvas.drawBitmap(gate, xOffset, yOffset, upPaint);
		// If down, draw handle opaque. Otherwise draw transparent.
		canvas.drawBitmap(handle, screenX + halfRadius + xOffset, screenY + halfRadius + yOffset,
				isDown ? downPaint : upPaint);
	}

	/**
	 * Updates joystick status for next network send.
	 * 
	 * @param screenX
	 *            X coordinate of touch with respect to entire screen.
	 * @param screenY
	 *            Y coordinate of touch with respect to entire screen.
	 */
	@Override
	public void updateStatus(float x, float y) {
		x -= radius + xOffset;
		y -= radius + yOffset;
		if (x == 0 && y == 0) {
			GameControllerView.analog.set(joyXIndex, (short) (Short.MAX_VALUE / 2));
			GameControllerView.analog.set(joyYIndex, (short) (Short.MAX_VALUE / 2));
		} else {
			// Distance between origin and touch coordinates
			float length = (float) Math.sqrt(y * y + x * x);
			// Sin and Cos are between 0 and radius
			float sin = y / length * radius;
			float cos = x / length * radius;
			float absY = Math.abs(y);
			float absX = Math.abs(x);
			float absCos = Math.abs(cos);
			float absSin = Math.abs(sin);

			// If outside radius than stick to the rim of the handle
			if (absSin < absY)
				y = sin;
			if (absCos < absX)
				x = cos;

			// Map the square coordinates to circular coordinates
			float maxLength = (absY > absX) ? absSin : absCos;

			// Update the network data, normalize to short value
			GameControllerView.analog.set(joyXIndex, (short) (((x / maxLength) + 1f) * .5f * Short.MAX_VALUE));
			GameControllerView.analog.set(joyYIndex, (short) (((y / maxLength) + 1f) * .5f * Short.MAX_VALUE));
		}

		// Update drawing info
		screenY = y;
		screenX = x;
	}
}
