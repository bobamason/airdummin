package com.bobamason.airdrummer;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class GLSurface extends GLSurfaceView {

	private GLRenderer renderer;

	public GLSurface(Context context, AttributeSet attrs) {
		super(context, attrs);
		setEGLContextClientVersion(2);
		renderer = new GLRenderer();
		renderer.setContext(context);
		setRenderer(renderer);
		setRenderMode(RENDERMODE_CONTINUOUSLY);
	}

	public GLRenderer getRenderer() {
		return renderer;
	}
}
