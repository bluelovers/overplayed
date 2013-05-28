package org.gphrost.Overplayed;


import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.SeekBar;

public class AlphaSeekBar extends android.widget.SeekBar {

	public AlphaSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		setProgress((int) (GameControllerView.alpha * getMax()));
		setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				GameControllerView.updateAlpha((float) arg1 / arg0.getMax());
				AlphaDialog.drawview.invalidate();
			}

			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub

			}

			public void onStopTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub

			}

		});
	}
	protected void onLayout (boolean changed, int left, int top, int right, int bottom){
		for (GameControllerView control : GameControllerView.mView) {
			control.onLayout(changed, left, top, right, bottom);
		}
	}

	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		for (GameControllerView control : GameControllerView.mView) {
			control.onConfigurationChanged(newConfig);
			control.updateOffset();
		}
		GameControllerView.hideButton.onConfigurationChanged(newConfig);
		GameControllerView.stopButton.onConfigurationChanged(newConfig);
		GameControllerView.alphaButton.onConfigurationChanged(newConfig);
		AlphaDialog.drawview.invalidate();
	}
}
