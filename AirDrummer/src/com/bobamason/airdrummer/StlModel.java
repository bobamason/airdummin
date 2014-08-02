package com.bobamason.airdrummer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.AsyncTask;
import android.util.Log;

public class StlModel {
	private FloatBuffer vertexBuffer;

	static final int positionDataSize = 3;

	static final int normalDataSize = 3;

	int vertexStride = 6 * 4;

	private int mProgram;

	private int mPositionHandle;

	private int mColorHandle;

	private final int mNormalOffset = 3;

	private int mMVPMatrixHandle;

	private boolean loaded = false;

	private int vertexCount;

	private List<Float> verticesList = new ArrayList<Float>();

	private float[] vertices;

	private Context context;

	private float[] minVals = { 0f, 0f, 0f };

	private float[] maxVals = { 0f, 0f, 0f };

	private int mMVMatrixHandle;

	private int mLightPosHandle;

	private int mNormalHandle;

	private float[] mvpMatrix = new float[16];

	private int mLightStrengthHandle;

	private String filename;

	private StlModel.LoadStatusListener mLoadStatusListener;

	private float[] projectionMatrix = new float[16];

	private float[] mvMatrix = new float[16];

	private float[] modelMatrix = new float[16];

	private float lightStrength = 1f;

	private Vector3 currentTrans = new Vector3();

	public StlModel(Context ctx, String filename, int program) {

		context = ctx;
		this.filename = filename;
		mProgram = program;
		new LoadModelTask().execute(filename);

		setIdentity();
	}

	public StlModel(GLRenderer render, Context ctx, String filename, int program) {
		if (render instanceof StlModel.LoadStatusListener) {
			mLoadStatusListener = render;
		}

		context = ctx;
		this.filename = filename;
		mProgram = program;
		new LoadModelTask().execute(filename);

		setIdentity();
	}

	public boolean isLoaded() {
		return loaded;
	}

	public void setProjectionMatrix(float[] pMatrix) {
		projectionMatrix = pMatrix;
	}

	public void setProgram(int p) {
		mProgram = p;
	}

	public void draw(float[] viewMatrix, float[] lightPos, float[] color) {
		if (!loaded)
			return;

		GLES20.glUseProgram(mProgram);

		mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "u_MVPMatrix");
		mMVMatrixHandle = GLES20.glGetUniformLocation(mProgram, "u_MVMatrix");
		mLightPosHandle = GLES20.glGetUniformLocation(mProgram, "u_LightPos");
		mPositionHandle = GLES20.glGetAttribLocation(mProgram, "a_Position");
		mColorHandle = GLES20.glGetUniformLocation(mProgram, "u_Color");
		mNormalHandle = GLES20.glGetAttribLocation(mProgram, "a_Normal");
		mLightStrengthHandle = GLES20.glGetUniformLocation(mProgram,
				"u_LightStrength");

		vertexBuffer.position(0);
		GLES20.glVertexAttribPointer(mPositionHandle, positionDataSize,
				GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);
		GLES20.glEnableVertexAttribArray(mPositionHandle);

		vertexBuffer.position(mNormalOffset);
		GLES20.glVertexAttribPointer(mNormalHandle, normalDataSize,
				GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);
		GLES20.glEnableVertexAttribArray(mNormalHandle);

		GLES20.glUniform3f(mLightPosHandle, lightPos[0], lightPos[1],
				lightPos[2]);
		GLES20.glUniform1f(mLightStrengthHandle, lightStrength);

		GLES20.glUniform4fv(mColorHandle, 1, color, 0);

		Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, modelMatrix, 0);

		GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mvMatrix, 0);

		Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvMatrix, 0);
		GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

		GLES20.glDisableVertexAttribArray(mPositionHandle);
		GLES20.glDisableVertexAttribArray(mColorHandle);
	}

	private boolean loadModel(String filename) {
		BufferedReader reader = null;
		int i, index;
		float[] tempNorms = new float[3];
		boolean isOk = false;

		try {
			reader = new BufferedReader(new InputStreamReader(context
					.getAssets().open(filename)));
			reader.readLine();

			String line = reader.readLine();
			while (line != null) {
				if (line.contains("facet normal")) {
					String[] split = line.split(" ");
					i = 0;
					for (String s : split) {
						if (!s.equals("facet") && !s.equals("normal")
								&& !s.equals("")) {
							try {
								float n = Float.parseFloat(s);
								tempNorms[i] = n;
							} catch (NumberFormatException e) {
								e.printStackTrace();
							}
							i++;
						}
					}

				} else if (line.contains("vertex ")) {
					String[] split = line.split(" ");
					for (String s : split) {
						if (!s.equals("vertex") && !s.equals("")) {
							try {
								float v = Float.parseFloat(s);
								index = verticesList.size() % 3;
								minVals[index] = v < minVals[index] ? v
										: minVals[index];
								maxVals[index] = v > maxVals[index] ? v
										: maxVals[index];
								verticesList.add(v);

								if (verticesList.size() % 6 == 3) {
									verticesList.add(tempNorms[0]);
									verticesList.add(tempNorms[1]);
									verticesList.add(tempNorms[2]);
								}
							} catch (NumberFormatException e) {
								e.printStackTrace();
							}
						}
					}
				}

				line = reader.readLine();
			}

			vertices = new float[verticesList.size()];

			for (i = 0; i < verticesList.size(); i++) {
				vertices[i] = verticesList.get(i);
				// ------- debug only with shape02.stl
				// Log.d("array", i + " | " + vertices[i]);
			}
			if (vertices.length > 12) {
				for (i = 0; i < 12; i++) {
					Log.d(" 2 triangle sample ", i + " | " + vertices[i]
							+ (i % 6 >= 3 ? " : normal" : " : vertex"));
				}
			}

			isOk = true;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return isOk;
	}

	public float getWidth() {
		if (loaded)
			return maxVals[0] - minVals[0];
		else
			return 0;
	}

	public float getHeight() {
		if (loaded)
			return maxVals[1] - minVals[1];
		else
			return 0;
	}

	public float getDepth() {
		if (loaded)
			return maxVals[2] - minVals[2];
		else
			return 0;
	}

	public float getLargestDimen() {
		if (getWidth() > getHeight()) {
			if (getWidth() > getDepth())
				return getWidth();
			else
				return getDepth();
		} else {
			if (getHeight() > getDepth())
				return getHeight();
			else
				return getDepth();
		}
	}

	public void setLightStrength(float strength) {
		lightStrength = strength;
	}

	public void setIdentity() {
		currentTrans.set(0f, 0f, 0f);
		Matrix.setIdentityM(modelMatrix, 0);
	}

	public void translate(float x, float y, float z) {
		currentTrans.add(x, y, z);
		Matrix.translateM(modelMatrix, 0, x, y, z);
	}

	public void translate(Vector3 v) {
		currentTrans.add(v);
		Matrix.translateM(modelMatrix, 0, v.x, v.y, v.z);
	}

	public void rotateEuler(float z, float x, float y) {
		Matrix.rotateM(modelMatrix, 0, z, 0f, 0f, 1f);
		Matrix.rotateM(modelMatrix, 0, x, 1f, 0f, 0f);
		Matrix.rotateM(modelMatrix, 0, y, 0f, 1f, 0f);
	}

	public void rotateAxis(float a, float x, float y, float z) {
		Matrix.rotateM(modelMatrix, 0, a, x, y, z);
	}

	public void scale(float s) {
		Matrix.scaleM(modelMatrix, 0, s, s, s);
	}

	public void scale(float sx, float sy, float sz) {
		Matrix.scaleM(modelMatrix, 0, sx, sy, sz);
	}

	public void getCenter(float[] vec4) {
		if (vec4.length != 4)
			throw new IllegalArgumentException("array must have lenght of 3");
		if (loaded) {
			vec4[0] = (maxVals[0] + minVals[0]) / 2f;
			vec4[1] = (maxVals[1] + minVals[1]) / 2f;
			vec4[2] = (maxVals[2] + minVals[2]) / 2f;
			vec4[3] = 1f;
		} else {
			vec4[0] = 0f;
			vec4[1] = 0f;
			vec4[2] = 0f;
			vec4[3] = 1f;
		}
	}

	public Vector3 getCenterVec() {
		if (loaded) {
			Vector3 v = new Vector3((maxVals[0] + minVals[0]) / 2f,
					(maxVals[1] + minVals[1]) / 2f,
					(maxVals[2] + minVals[2]) / 2f);
			v.add(currentTrans);
			return v;
		} else
			return new Vector3();
	}

	private class LoadModelTask extends AsyncTask<String, Void, Boolean> {

		@Override
		protected void onPreExecute() {
			if (mLoadStatusListener != null)
				mLoadStatusListener.started();
		}

		@Override
		protected Boolean doInBackground(String... args) {
			boolean b = loadModel(args[0]);
			return b;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (!result)
				throw new RuntimeException("stl model failed to load");
			vertexCount = vertices.length / 6;
			Log.d("lenght", vertices.length + "");
			Log.d("count", vertexCount + "");

			ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
			bb.order(ByteOrder.nativeOrder());

			vertexBuffer = bb.asFloatBuffer();
			vertexBuffer.put(vertices);
			vertexBuffer.position(0);

			Log.d("buffer", vertexBuffer.capacity() + "");
			Log.d("stride", vertexStride + "");

			loaded = result;
			Log.d("loaded", String.valueOf(loaded) + " " + filename);

			if (mLoadStatusListener != null)
				mLoadStatusListener.completed();
			System.gc();
		}
	}

	public static interface LoadStatusListener {
		public abstract void started();

		public abstract void completed();
	}
}
