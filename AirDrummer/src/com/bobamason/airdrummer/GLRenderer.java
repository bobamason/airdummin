package com.bobamason.airdrummer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.*;

public class GLRenderer implements GLSurfaceView.Renderer,
LoadStatusListener {

	private final String vertexShaderCode = "uniform mat4 u_MVPMatrix;\n"
	+ "uniform mat4 u_MVMatrix;\n" + "attribute vec4 a_Position;\n"
	+ "attribute vec3 a_Normal;\n" + "varying vec3 v_Position;\n"
	+ "varying vec3 v_Normal;\n"

	+ "void main()\n" + "{ \n"

	+ "   v_Position = vec3(u_MVMatrix * a_Position);\n"
	+ "   v_Normal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));\n"
	+ "   gl_Position = u_MVPMatrix * a_Position;\n"

	+ "}\n";

	private final String fragmentShaderCode = "precision mediump float;\n"

	+ "uniform vec3 u_LightPos;\n"
	+ "uniform float u_LightStrength;\n"
	+ "varying vec3 v_Position;\n"
	+ "uniform vec4 u_Color; \n"
	+ "varying vec3 v_Normal;\n"

	+ "void main()\n"
	+ "{ \n"

	+ "   float distance = length(u_LightPos - v_Position) / u_LightStrength;\n"
	+ "   vec3 lightVector = normalize(u_LightPos - v_Position);\n"
	+ "   vec3 normal = v_Normal / length(v_Normal); \n"
	+ "   float diffuse = max(dot(normal, lightVector), 0.1);\n"
	+ "   diffuse = diffuse * (1.0 / (1.0 + (0.25 * distance * distance)));\n"
	+ "   gl_FragColor = u_Color * diffuse * 0.8 + u_Color * 0.2; \n"

	+ "}";

	private float[] mProjectionMatrix = new float[16];

	private float[] mViewMatrix = new float[16];

	private float[] mAngles = new float[3];

	private float[] tempColor = new float[4];

	private Context context;

	private float[] mLightPos = { 0f, -2f, 0f, 1f };

	private float[] mLightPosInEyeSpace = new float[4];

	private int mProgram;

	private StlModel drumCenter, drumFrame, drumStick;

	private LoadingAnimation loadAnim;

	private float[] centerColor = { 0.4f, 0.4f, 0.4f, 1f };

	private float[] stickColor = { 0.9f, 0.8f, 0.4f, 1f };

	private boolean allLoaded = true;

	private int startedCount = 0;

	private int completedCount = 0;

	private DrumKit drumKit;

	private long[] lastMillis = new long[5];

	private long duration = 600;

	private int currentView = 0;

	private long startTime, endTime;

	public void setContext(Context context) {
		this.context = context;
		drumKit = ((MainActivity)context).getDrumKit();
		drumKit.setOnDrumHitListener(new DrumKit.OnDrumHitListener(){

				@Override
				public void drumHit(int pos) {
					lastMillis[pos] = System.currentTimeMillis();
				}
			});
	}

	@Override
	public void onSurfaceCreated(GL10 unused, EGLConfig config) {
		GLES20.glClearColor(0.05f, 0.05f, 0.05f, 1f);

		int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);

		int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,
										fragmentShaderCode);


		mProgram = GLES20.glCreateProgram();

		if (mProgram != 0) {
			GLES20.glAttachShader(mProgram, vertexShader);
			GLES20.glAttachShader(mProgram, fragmentShader);

			GLES20.glLinkProgram(mProgram);

			// Get the link status.
			final int[] linkStatus = new int[1];
			GLES20.glGetProgramiv(mProgram, GLES20.GL_LINK_STATUS, linkStatus, 0);

			// If the link failed, delete the program.
			if (linkStatus[0] == 0) {				
				Log.e("OpenGL Program", "Error compiling program: " + GLES20.glGetProgramInfoLog(mProgram));
				GLES20.glDeleteProgram(mProgram);
				mProgram = 0;
			}
		}

		if (mProgram == 0) {
			throw new RuntimeException("Error creating program.");
		}

		GLES20.glEnable(GLES20.GL_CULL_FACE);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);

		// contruct models
		loadAnim = new LoadingAnimation();
		drumCenter = new StlModel(this, context, "drum3center.stl", mProgram);
		drumFrame = new StlModel(this, context, "drumframe2.stl", mProgram);
		drumStick = new StlModel(this, context, "drumstick1.stl", mProgram);
	}

	@Override
	public void onSurfaceChanged(GL10 unused, int width, int height) {
		float ratio = (float) width / height;
		GLES20.glViewport(0, 0, width, height);
		Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 1, 20);
		// set STLModel projectionMatrix
		drumCenter.setProjectionMatrix(mProjectionMatrix);
		drumFrame.setProjectionMatrix(mProjectionMatrix);
		drumStick.setProjectionMatrix(mProjectionMatrix);
		loadAnim.setProjection(mProjectionMatrix);
	}

	@Override
	public void onDrawFrame(GL10 unused) {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		if (allLoaded) {
			currentView = drumKit.getViewPos();
			if (currentView == 0) {
				drawView1();
			} else if (currentView == 1) {
				drawView2();
			}
		} else {
			loadAnim.draw();
		}
	}

	private void drawView2() {
		Matrix.setLookAtM(mViewMatrix, 0, 0.0f, 0.0f, -1.0f, 0f, 0f, 0f,
						  0f, 1f, 0f);
		Matrix.rotateM(mViewMatrix, 0, 20, 1f, 0f, 0f);
		Matrix.translateM(mViewMatrix, 0, 0f, 2.6f, 0f);
		setLightPos(-1f, -4f, 0f);
		Matrix.multiplyMV(mLightPosInEyeSpace, 0, mViewMatrix, 0,
						  mLightPos, 0);

		float count = drumKit.getCount();
		float span = drumKit.getSpan();

		drumCenter.setLightStrength(6f);
		drumFrame.setLightStrength(6f);
		drumStick.setLightStrength(4f);

		for (int i = 0; i < count; i++) {
			drumCenter.setIdentity();
			drumFrame.setIdentity();

			drumCenter.translate(0f, -5f, 7f);
			drumFrame.translate(0f, -5f, 7f);

			if (lastMillis[i] > System.currentTimeMillis() - duration) {
				float s = (float)Math.sin(6 * Math.PI * ((double)(System.currentTimeMillis() % duration) / duration)) * 0.3f + 0.3f;
				drumCenter.translate(0f, 0f, s);
				drumFrame.translate(0f, 0f, s);
			}

			float a = (-span * count * 0.5f) + i * span + span * 0.5f;
			drumCenter.rotateEuler(a, 0f, 0f);
			drumFrame.rotateEuler(a, 0f, 0f);

			drumCenter.translate(0f, 7f, 0f);
			drumFrame.translate(0f, 7f, 0f);

			drumCenter.rotateEuler(0f, -10f, 0f);
			drumFrame.rotateEuler(0f, -10f, 0f);

			drumCenter.scale(1.2f);
			drumFrame.scale(1.2f);

			drumCenter.draw(mViewMatrix, mLightPosInEyeSpace, centerColor);
			drumFrame.draw(mViewMatrix, mLightPosInEyeSpace, hexToFloat(Constants.DEFAULT_DRUM_COLORS[i]));
		}

		drumStick.setIdentity();
		drumStick.translate(0f, -5.2f, 6.8f);
		mAngles = drumKit.getAngles();
		drumStick.rotateEuler(mAngles[0], mAngles[1], mAngles[2]);
		drumStick.translate(0f, 2f, 0f);
		drumStick.scale(2.8f);
		drumStick.draw(mViewMatrix, mLightPosInEyeSpace, stickColor);
	}

	private void drawView1() {
		Matrix.setLookAtM(mViewMatrix, 0, 0.0f, 0.0f, -1.0f, 0f, 0f, 0f,
						  0f, 1f, 0f);


		Matrix.translateM(mViewMatrix, 0, 0f, -5f, 0f);
		Matrix.rotateM(mViewMatrix, 0, -mAngles[0], 0f, 0f, 1f);
		Matrix.rotateM(mViewMatrix, 0, -mAngles[1], 1f, 0f, 0f);
		setLightPos(-0.5f, -2f, 0f);
		Matrix.multiplyMV(mLightPosInEyeSpace, 0, mViewMatrix, 0,
						  mLightPos, 0);

		float count = drumKit.getCount();
		float span = drumKit.getSpan();

		drumCenter.setLightStrength(9f);
		drumFrame.setLightStrength(9f);
		drumStick.setLightStrength(4f);

		for (int i = 0; i < count; i++) {
			drumCenter.setIdentity();
			drumFrame.setIdentity();

			drumCenter.translate(0f, 0f, 3f);
			drumFrame.translate(0f, 0f, 3f);

			if (lastMillis[i] > System.currentTimeMillis() - duration) {
				float s = (float)Math.sin(6 * Math.PI * ((double)(System.currentTimeMillis() % duration) / duration)) * 0.4f + 0.4f;
				drumCenter.translate(0f, 0f, s);
				drumFrame.translate(0f, 0f, s);
			}

			float a = (-span * count * 0.5f) + i * span + span * 0.5f;
			mAngles = drumKit.getAngles();
			drumCenter.rotateEuler(a, 0f, 0f);
			drumFrame.rotateEuler(a, 0f, 0f);

			drumCenter.translate(0f, 7f, 0f);
			drumFrame.translate(0f, 7f, 0f);

			drumCenter.rotateEuler(0f, -10f, 0f);
			drumFrame.rotateEuler(0f, -10f, 0f);

			drumCenter.scale(1.2f);
			drumFrame.scale(1.2f);

			drumCenter.draw(mViewMatrix, mLightPosInEyeSpace, centerColor);
			drumFrame.draw(mViewMatrix, mLightPosInEyeSpace, hexToFloat(Constants.DEFAULT_DRUM_COLORS[i]));
		}

		Matrix.setLookAtM(mViewMatrix, 0, 0.0f, 0.0f, -1.0f, 0f, 0f, 0f,
						  0f, 1f, 0f);
		setLightPos(-0.5f, -1f, 0f);
		Matrix.multiplyMV(mLightPosInEyeSpace, 0, mViewMatrix, 0,
						  mLightPos, 0);

		drumStick.setIdentity();
		drumStick.translate(0f, -2f, 2.8f);
		drumStick.scale(2.0f);
		drumStick.draw(mViewMatrix, mLightPosInEyeSpace, stickColor);
	}

	private float[] hexToFloat(int c) {
		tempColor[0] = (c >> 16 & 0xff) / 255f;
		tempColor[1] = (c >> 8 & 0xff) / 255f;
		tempColor[2] = (c & 0xff) / 255f;
		tempColor[3] = 1f;
		return tempColor;
	}

	private void setLightPos(float x, float y, float z) {
		mLightPos[0] = x;
		mLightPos[1] = y;
		mLightPos[2] = z;
		mLightPos[3] = 1f;
	}

	public static int loadShader(int type, String shaderCode) {
		int shaderHandle = GLES20.glCreateShader(type);

		if (shaderHandle != 0) {
			// Pass in the shader source.
			GLES20.glShaderSource(shaderHandle, shaderCode);

			// Compile the shader.
			GLES20.glCompileShader(shaderHandle);

			// Get the compilation status.
			final int[] compileStatus = new int[1];
			GLES20.glGetShaderiv(shaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

			// If the compilation failed, delete the shader.
			if (compileStatus[0] == 0) {
				Log.e("Shader", "Error compiling shader: " + GLES20.glGetShaderInfoLog(shaderHandle));
				GLES20.glDeleteShader(shaderHandle);
				shaderHandle = 0;
			}
		}

		if (shaderHandle == 0) {			
			throw new RuntimeException("Error creating shader.");
		}

		return shaderHandle;
	}

	@Override
	public void started() {
		if (allLoaded) {
			allLoaded = false;
			startTime = System.currentTimeMillis();
		}
		startedCount++;
	}

	@Override
	public void completed() {
		completedCount++;
		if (completedCount == startedCount) {
			allLoaded = true;
			drumKit.setRun(true);
			endTime = System.currentTimeMillis();
			double elapsed = (endTime - startTime) / 1000d;
			Log.d("load time", "time to load: " + String.valueOf(elapsed) + "sec");
		}
	}
}
