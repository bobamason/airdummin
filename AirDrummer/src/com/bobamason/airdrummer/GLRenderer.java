package com.bobamason.airdrummer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

public class GLRenderer implements GLSurfaceView.Renderer,
		StlModel.LoadStatusListener {

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
			+ "   gl_FragColor = u_Color * diffuse; \n"

			+ "}";

	private float[] mProjectionMatrix = new float[16];

	private float[] mViewMatrix = new float[16];

	private float[] mAngles = new float[3];

	private float[] mQuaternion = new float[4];

	private Context context;

	private float[] mRotationMatrix = new float[16];

	private float[] mLightModelMatrix = new float[16];

	private float[] mLightPos = { 0f, 0f, 0f, 1f };

	private float[] mLightPosInModelSpace = new float[4];

	private float[] mLightPosInEyeSpace = new float[4];

	private int mProgram;

	private StlModel drumCenter, drumFrame, drumStick;

	private LoadingAnimation loadAnim;

	private float[] color = { 0.8f, 0.0f, 0.0f, 1f };

	private boolean allLoaded = false;

	private int startedCount = 0;

	private int completedCount = 0;

	private static final float lightStrength = 8f;

	public void setContext(Context context) {
		this.context = context;
	}

	@Override
	public void onSurfaceCreated(GL10 unused, EGLConfig config) {
		GLES20.glClearColor(0.05f, 0.2f, 0.25f, 1f);

		int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
		if (vertexShader == 0)
			throw new RuntimeException("error creating vertex shader");
		int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,
				fragmentShaderCode);
		if (fragmentShader == 0)
			throw new RuntimeException("error creating fragment shader");

		mProgram = GLES20.glCreateProgram();
		GLES20.glAttachShader(mProgram, vertexShader);
		GLES20.glAttachShader(mProgram, fragmentShader);

		GLES20.glLinkProgram(mProgram);

		GLES20.glEnable(GLES20.GL_CULL_FACE);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);

		// contruct model and set lightStrength
		loadAnim = new LoadingAnimation();
		drumCenter = new StlModel(this, context, "drum3center.stl", mProgram);
		drumCenter.setLightStrength(lightStrength);
		drumFrame = new StlModel(this, context, "drum3frame.stl", mProgram);
		drumFrame.setLightStrength(lightStrength);
		drumStick = new StlModel(this, context, "drumstick1.stl", mProgram);
		drumStick.setLightStrength(lightStrength);
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
			Matrix.setLookAtM(mViewMatrix, 0, 0.0f, 0.0f, -1.0f, 0f, 0f, 0f,
					0f, 1f, 0f);
			Matrix.setIdentityM(mLightModelMatrix, 0);
			Matrix.multiplyMV(mLightPosInModelSpace, 0, mLightModelMatrix, 0,
					mLightPos, 0);
			Matrix.multiplyMV(mLightPosInEyeSpace, 0, mViewMatrix, 0,
					mLightPosInModelSpace, 0);

			// model transformations
			// STLModel.draw(float[] viewMatrix, float[] lightPos, float[]
			// color)

		} else {
			loadAnim.draw();
		}
	}

	public static int loadShader(int type, String shaderCode) {
		int shader = GLES20.glCreateShader(type);
		GLES20.glShaderSource(shader, shaderCode);
		GLES20.glCompileShader(shader);
		return shader;
	}

	@Override
	public void started() {
		if (allLoaded)
			allLoaded = false;
		startedCount++;
	}

	@Override
	public void completed() {
		completedCount++;
		if (completedCount == startedCount)
			allLoaded = true;
	}
}
