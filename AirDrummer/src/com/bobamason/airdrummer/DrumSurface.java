package com.bobamason.airdrummer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

@SuppressLint("FloatMath")
public class DrumSurface extends SurfaceView implements SurfaceHolder.Callback {

	private Context context;

	private SurfaceThread sthread;

	private Paint linePaint;

	private Paint textPaint;

	private DrumKit drumKit;

	private int bgColor;

	public DrumSurface(Context ctx, AttributeSet attrSet) {
		super(ctx, attrSet);
		context = ctx;
		SurfaceHolder holder = this.getHolder();
		holder.addCallback(this);
		init();
	}

	private void init() {
		bgColor = context.getResources().getColor(R.color.main_bg_color);

		linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		linePaint.setStyle(Paint.Style.STROKE);
		linePaint.setStrokeCap(Paint.Cap.ROUND);
		linePaint.setStrokeJoin(Paint.Join.ROUND);
		linePaint.setColor(Color.GRAY);
		linePaint.setStrokeWidth(4f);

		textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		textPaint.setTextSize(14);
		textPaint.setColor(Color.LTGRAY);

		drumKit = ((MainActivity) context).getDrumKit();
	}

	public void surfaceCreated(SurfaceHolder sholder) {
		// TODO: Implement this method
		sthread = new SurfaceThread(sholder, context, this);
		sthread.setRunning(true);
		sthread.start();
	}

	public void surfaceChanged(SurfaceHolder p1, int p2, int width, int height) {
		drumKit.setScreenSize(width, height);
	}

	public void surfaceDestroyed(SurfaceHolder p1) {
		// TODO: Implement this method
		sthread.setRunning(false);
		boolean retry = true;
		while (retry) {
			try {
				sthread.join();
				retry = false;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void drawSurface(Canvas c) {
		c.drawColor(bgColor);
		drumKit.draw(c);
	}

	private class SurfaceThread extends Thread {
		boolean mRun;
		SurfaceHolder holder;
		DrumSurface mSurfacePanel;
		Canvas canvas;

		public SurfaceThread(SurfaceHolder sholder, Context ctx,
				DrumSurface spanel) {
			holder = sholder;
			mSurfacePanel = spanel;
			mRun = false;
		}

		public void setRunning(boolean run) {
			mRun = run;
		}

		@Override
		public void run() {
			super.run();
			while (mRun) {
				canvas = holder.lockCanvas();
				if (canvas != null) {
					mSurfacePanel.drawSurface(canvas);
					holder.unlockCanvasAndPost(canvas);
				}
			}
		}
	}
}
