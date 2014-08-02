package com.bobamason.airdrummer;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;

public class DrumUI {

	private Paint highlightPaint;

	private long[] lastMillis = new long[5];

	private float stepPerMilli;

	private long duration = 400;

	private float minR = 10;

	private RectF oval = new RectF();

	private float[] angles = new float[3];

	private float radius = 90;

	private float strokeWidth = 20;

	private Matrix matrix = new Matrix();

	private int drumCount;

	private float span;

	private float width;

	private float height;

	private Paint linePaint;

	private Paint textPaint;

	private PointF center = new PointF();

	private Paint arcPaint;

	public DrumUI(DrumKit drumKit) {
		drumCount = drumKit.getCount();
		span = drumKit.getSpan();
		linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		linePaint.setStyle(Paint.Style.STROKE);
		linePaint.setStrokeCap(Paint.Cap.ROUND);
		linePaint.setStrokeJoin(Paint.Join.ROUND);
		linePaint.setColor(Color.WHITE);
		linePaint.setStrokeWidth(4f);

		textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		textPaint.setTextSize(20f);
		textPaint.setColor(Color.WHITE);

		arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		arcPaint.setStyle(Paint.Style.STROKE);
		arcPaint.setStrokeCap(Paint.Cap.BUTT);
		arcPaint.setStrokeJoin(Paint.Join.ROUND);

		highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		highlightPaint.setStyle(Paint.Style.STROKE);
		highlightPaint.setStrokeCap(Paint.Cap.BUTT);
		highlightPaint.setStrokeJoin(Paint.Join.ROUND);
		highlightPaint.setColor(0x80e3e3e3);
	}

	public void setSpan(float s) {
		span = s;
	}

	public void setAngles(float[] angles) {
		this.angles = angles.clone();
	}

	public void setCount(int count) {
		drumCount = count;
	}

	public void setUpDrumsDisplay(float w, float h) {
		width = w;
		height = h;
		radius = height;
		Log.d("radius", "r = " + String.valueOf(radius));
		// strokeWidth = FloatMath.sin(span / 2 * deg2rad) * radius * 2;
		strokeWidth = radius * 0.75f;
		Log.d("strokeWidth", "sw = " + String.valueOf(radius));
		arcPaint.setStrokeWidth(strokeWidth);
		oval.set(-radius, -radius, radius, radius);
		center.set(width * 0.5f, radius + height * 0.4f);
	}

	// ----
	// ----
	// RUNS ON SurfaceThread
	public void draw(Canvas c) {
		matrix.setTranslate(center.x, center.y);
		matrix.preRotate(-angles[0]);
		c.save();
		c.setMatrix(matrix);
		for (int i = 0; i < drumCount; i++) {
			drawDrum(c, i, angles[0]);
		}
		c.restore();
		// c.drawText(String.valueOf(angles[0]), 50, 50, textPaint);
	}

	private void drawDrum(Canvas c, int pos, float angle) {
		float start = (-span * drumCount * 0.5f) + pos * span;
		float end = start + span;
		float sw = strokeWidth * scale();
		arcPaint.setStrokeWidth(sw);
		if (angle > start && angle < end) {
			arcPaint.setColor(Constants.DEFAULT_DRUM_COLORS[pos]);
		} else {
			arcPaint.setColor(0xff2F393F);
		}
		c.drawArc(oval, 270f + start + 1f, span - 2f, false, arcPaint);

		long millis = System.currentTimeMillis();
		if (millis - lastMillis[pos] < duration) {
			stepPerMilli = (sw - minR) / duration;
			highlightPaint.setStrokeWidth(stepPerMilli
					* (millis - lastMillis[pos]));
			c.drawArc(oval, 270f + start + 1f, span - 2f, false, highlightPaint);
		}
	}

	public void hit(int pos) {
		lastMillis[pos] = System.currentTimeMillis();
	}

	private float scale() {
		float a = -angles[1];
		float min = -5f;
		float max = 70f;
		a = a > max ? max : a;
		a = a < min ? min : a;
		float step = (1f - 0.5f) / (max - min);
		return 1f - a * step;
	}
}
