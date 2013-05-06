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
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;

/**
 * Game controller button.
 * 
 * @author Steven T. Ramzel
 * @see GameControllerView
 */
// Button is not called from xml, so not implementing proper constructor
@SuppressLint("ViewConstructor")
public class Button extends GameControllerView {
	// Paint used to printing text on button
	static final Paint textPaint = new Paint();

	// Bitmap used for rendering button
	protected Bitmap buttonBitmap;
	private int buttonIndex; // Button to map to
	// Circle shape for button image generation
	private ShapeDrawable buttonShape = new ShapeDrawable();
	private String label; // Text on button face

	/**
	 * @param context
	 *            Application context.
	 * @param radiusScale
	 *            Radius of button as multiple of auto-generated standard button
	 *            radius.
	 * @param buttonIndex
	 *            Button to map to.
	 * @param gravity
	 *            Placement of button within the screen as per Gravity.
	 * @param xOffset
	 *            X position for this button.
	 * @param yOffset
	 *            Y position for this button.
	 * @param label
	 *            Text on button face.
	 */
	public Button(Context context, float radiusScale, byte buttonIndex,
			int gravity, float xOffset, float yOffset, String label) {
		super(context);
		this.label = label;
		this.buttonIndex = buttonIndex;
		set(radiusScale, gravity, xOffset, yOffset);
	}

	@Override
	public void onDraw(Canvas canvas) {
		// If down, draw opaque. Otherwise draw transparent.
		canvas.drawBitmap(buttonBitmap, 0f, 0f, isDown ? downPaint : upPaint);
	}

	/**
	 * Buttons dont need to be updated while being pressed. This does nothing
	 * for buttons.
	 */
	@Override
	public void restrictedInvalidate() {
	}

	/**
	 * Generates bitmap used for drawing button.
	 * 
	 * @param radius
	 *            Radius of button in pixels.
	 */
	@Override
	void generateBitmap(int radius) {
		//Size and shape
		buttonShape.setShape(new OvalShape());
		buttonShape.setBounds(new Rect(padding, padding, radius * 2 - padding, radius * 2 - padding));

		//Create button bitmap and render shape
		buttonBitmap = Bitmap.createBitmap(radius * 2, radius * 2,
				Bitmap.Config.ARGB_8888);
		Canvas handleCanvas = new Canvas(buttonBitmap);
		buttonShape.getPaint().set(fillPaint);
		buttonShape.draw(handleCanvas);

		//Set text style
		textPaint.setStyle(Paint.Style.FILL);
		textPaint.setTypeface(Typeface.DEFAULT_BOLD);
		textPaint.setTextAlign(Paint.Align.CENTER);
		textPaint.setColor(Color.BLACK);
		//Set size (one character has a standard design, more has dynamic size)
		if (label.length() == 1)
			textPaint.setTextSize(radius);
		else
			textPaint.setTextSize(radius / label.length()*2.5f);
		
		//Set paint to clear text from button
		textPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
		
		//Render the button
		handleCanvas.drawText(label, radius, radius
				+ (textPaint.getTextSize() * .33333f), textPaint);
	}

	/**
	 * Updates button status for next network send.
	 * 
	 * @param screenX
	 *            Not used.
	 * @param screenY
	 *            Not used.
	 */
	@Override
	public void updateStatus(float screenX, float screenY) {
		GameControllerView.buttons[buttonIndex] = isDown;
	}
}
