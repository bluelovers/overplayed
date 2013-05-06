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
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;

/**
 * Game controller directional pad.
 * 
 * @author Steven T. Ramzel
 * @see GameControllerView
 */
@SuppressLint("ViewConstructor")
public class DPad extends GameControllerView{
	//Paint used to create d-pad lines
	protected static final Paint strokePaint = new Paint();
	{
		strokePaint.setARGB(255, 128, 128, 128);
		strokePaint.setStyle(Paint.Style.STROKE);
		strokePaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		strokePaint.setStrokeWidth(4);
	}
	//Bitmap used for rendering d-pad
	private Bitmap dpadBitmap;
	//Circle shape for d-pad image generation
	private ShapeDrawable dpadShape = new ShapeDrawable();
	private int deadZone;//How far from center until key press registers
	private int downIndex; //Button to map Down to
	private int leftIndex; //Button to map Left to
	private int rightIndex; //Button to map Right to
	private int upIndex; //Button to map Up to
	
	/**
	 * @param context Application context.
	 * @param radiusScale Radius of d-pad as multiple of auto-generated standard button radius.
	 * @param upIndex Button to Up map to.
	 * @param downIndex Button to Down map to.
	 * @param leftIndex Button to Left map to.
	 * @param rightIndex Button to Right map to.
	 * @param gravity Placement of button within the screen as per Gravity. 
	 * @param xOffset X position for this button.
	 * @param yOffset Y position for this button.
	 */
	public DPad(Context context, float radiusScale, byte upIndex, byte downIndex, byte leftIndex, byte rightIndex, int gravity, float xOffset, float yOffset) {
		super(context);
		this.upIndex = upIndex;
		this.downIndex = downIndex;
		this.leftIndex = leftIndex;
		this.rightIndex = rightIndex;
		set(radiusScale, gravity, xOffset, yOffset);
}
	@Override
	public void onDraw(Canvas canvas){
		//If down, draw opaque. Otherwise draw transparent.
		canvas.drawBitmap(dpadBitmap, 0f, 0f, isDown ? downPaint : upPaint);
	}
	
	/**
	 * Generates bitmap used for drawing d-pad.
	 * @param radius Radius of d-pad in pixels.
	 */
	@Override
	void generateBitmap(int radius) {
		//Size and shape
		dpadShape.setShape(new OvalShape());
		dpadShape.setBounds(new Rect(padding,padding,radius*2-padding,radius*2-padding));

		//Create d-pad bitmap and render
		dpadBitmap = Bitmap.createBitmap(radius*2, radius*2, Bitmap.Config.ARGB_8888);
		Canvas dpadCanvas = new Canvas(dpadBitmap);
		dpadShape.getPaint().set(fillPaint);
		dpadShape.draw(dpadCanvas);
		deadZone = radius/4;
		float sideOffset = GameControllerView.circleY(radius - 2, deadZone);
		dpadCanvas.drawLine(radius - sideOffset, radius + deadZone, radius - deadZone, radius + deadZone, strokePaint);
		dpadCanvas.drawLine(radius - sideOffset, radius - deadZone, radius - deadZone, radius - deadZone, strokePaint);
		dpadCanvas.drawLine(radius + deadZone, radius + deadZone, radius + sideOffset, radius + deadZone, strokePaint);
		dpadCanvas.drawLine(radius + deadZone, radius - deadZone, radius + sideOffset, radius - deadZone, strokePaint);
		dpadCanvas.drawLine(radius + deadZone, radius + deadZone, radius + deadZone, radius + sideOffset, strokePaint);
		dpadCanvas.drawLine(radius - deadZone, radius + deadZone, radius - deadZone, radius + sideOffset, strokePaint);
		dpadCanvas.drawLine(radius + deadZone, radius - sideOffset, radius + deadZone, radius - deadZone, strokePaint);
		dpadCanvas.drawLine(radius - deadZone, radius - sideOffset, radius - deadZone, radius - deadZone, strokePaint);
	}
	
	/**
	 * Updates d-pad status for next network send.
	 * @param screenX X coordinate of touch with respect to entire screen.
	 * @param screenY Y coordinate of touch with respect to entire screen.
	 */
	@Override
	public void updateStatus(float screenX, float screenY){
		screenY -= radius + yOffset;
		screenX -= radius + xOffset;
		GameControllerView.buttons[upIndex] = screenY < -deadZone;
		GameControllerView.buttons[downIndex] = screenY > deadZone;
		GameControllerView.buttons[rightIndex] = screenX > deadZone;
		GameControllerView.buttons[leftIndex] = screenX < -deadZone;
	}
}
