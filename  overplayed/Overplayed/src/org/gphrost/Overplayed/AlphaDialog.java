package org.gphrost.Overplayed;

import com.gphrost.Overplayed.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;

public class AlphaDialog extends Activity implements OnCancelListener {
	static View drawview;
	AlertDialog ad;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) {
			startActivity(new Intent(this, Overplayed.class));
			finish();
		} else {
			GameControllerView.updateStandardRadius(getResources());
			for (GameControllerView control : GameControllerView.mView) {
				control.updateParams(control.radiusScale, control.xOffsetScale, control.yOffsetScale);
				control.generateBitmap((int) control.radius);
			}
			GameControllerView.wm.removeView(GameControllerView.hideButton);
			GameControllerView.wm.removeView(GameControllerView.stopButton);
			GameControllerView.wm.removeView(GameControllerView.alphaButton);

			LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT);
			drawview = new View(this) {
				Rect rectgle = new Rect();
				private int StatusBarHeight;

				@SuppressLint("WrongCall")
				@Override
				public void onDraw(Canvas canvas) {
					for (GameControllerView control : GameControllerView.mView) {
						control.onDraw(canvas, control.xOffset, control.yOffset
								- StatusBarHeight);
					}
				}

				protected void onLayout(boolean changed, int left, int top,
						int right, int bottom) {

					Window window = getWindow();
					window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
					StatusBarHeight = rectgle.top;
				}

			};
			this.setContentView(drawview, params);

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			LayoutInflater inflater = this.getLayoutInflater();
			View view = inflater.inflate(R.layout.alert, null);
			builder.setView(view)
					.setTitle("Transparency")
					.setNegativeButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id2) {
									Overplayed.editor.putFloat("alpha",
											GameControllerView.alpha);
									// Commit the edits!
									Overplayed.editor.commit();
									AlphaDialog.this.finish();
								}
							}).setOnCancelListener(this);
			ad = builder.create();
			ad.show();
		}
	}

	protected void onPause() {
		super.onPause();
		GameControllerView.wm.addView(GameControllerView.hideButton, GameControllerView.hideButton.params);
		GameControllerView.wm.addView(GameControllerView.stopButton, GameControllerView.stopButton.params);
		GameControllerView.wm.addView(GameControllerView.alphaButton,GameControllerView.alphaButton.params);
		ad.dismiss();
		finish();
	}

	public void onCancel(DialogInterface arg0) {
		GameControllerView.alpha = Overplayed.settings.getFloat("alpha", .5f);
		GameControllerView.updateAlpha(GameControllerView.alpha);
		AlphaDialog.drawview.invalidate();
		finish();
	}

}