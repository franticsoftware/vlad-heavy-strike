
package com.amine;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;

class JNIView extends GLSurfaceView
{
    private static String TAG = "VLAD";
    private static final boolean DEBUG = false;
	public static JNIActivity sActivity;

    public JNIView(Context context)
	{
        super(context);
		
        setEGLContextFactory(new ContextFactory());        
		setEGLConfigChooser(new ConfigChooser(8, 8, 8, 0, 16, 0));		
        setRenderer(new Renderer());
		setKeepScreenOn(true);
    }
	
    private static class ContextFactory implements GLSurfaceView.EGLContextFactory
	{
        private static int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
        public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig eglConfig)
		{
            Log.w(TAG, "creating OpenGL ES 1.1 context");
            checkEglError("Before eglCreateContext", egl);           
            EGLContext context = egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT, null);
            checkEglError("After eglCreateContext", egl);			
			JNILib.createContext();			
            return context;
        }

        public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context)
		{
			JNILib.destroyContext();
            egl.eglDestroyContext(display, context);
        }
    }

    private static void checkEglError(String prompt, EGL10 egl)
	{
        int error;
        while ((error = egl.eglGetError()) != EGL10.EGL_SUCCESS)
		{
            Log.e(TAG, String.format("%s: EGL error: 0x%x", prompt, error));
        }
    }

    private static class ConfigChooser implements GLSurfaceView.EGLConfigChooser
	{
        public ConfigChooser(int r, int g, int b, int a, int depth, int stencil)
		{
            mRedSize = r;
            mGreenSize = g;
            mBlueSize = b;
            mAlphaSize = a;
            mDepthSize = depth;
            mStencilSize = stencil;
        }
        
		private static int s_configAttribs[] =
		{			
			EGL10.EGL_BLUE_SIZE, 8,
			EGL10.EGL_GREEN_SIZE, 8,
			EGL10.EGL_RED_SIZE, 8,
			EGL10.EGL_DEPTH_SIZE, 16,
			EGL10.EGL_NONE
		};       

        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display)
		{           
            int[] num_config = new int[1];
            egl.eglChooseConfig(display, s_configAttribs, null, 0, num_config);

            int numConfigs = num_config[0];

            if (numConfigs <= 0)
			{
                throw new IllegalArgumentException("No configs match configSpec");
            }
           
            EGLConfig[] configs = new EGLConfig[numConfigs];
            egl.eglChooseConfig(display, s_configAttribs, configs, numConfigs, num_config);

            if (DEBUG)
			{
                 printConfigs(egl, display, configs);
            }
            
            return chooseConfig(egl, display, configs);
        }

        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display, EGLConfig[] configs)
		{
            for(EGLConfig config : configs)
			{
                int d = findConfigAttrib(egl, display, config,
                        EGL10.EGL_DEPTH_SIZE, 0);
                int s = findConfigAttrib(egl, display, config,
                        EGL10.EGL_STENCIL_SIZE, 0);

                // We need at least mDepthSize and mStencilSize bits
                if (d < mDepthSize || s < mStencilSize)
                    continue;

                // We want an *exact* match for red/green/blue/alpha
                int r = findConfigAttrib(egl, display, config,
                        EGL10.EGL_RED_SIZE, 0);
                int g = findConfigAttrib(egl, display, config,
                            EGL10.EGL_GREEN_SIZE, 0);
                int b = findConfigAttrib(egl, display, config,
                            EGL10.EGL_BLUE_SIZE, 0);
                int a = findConfigAttrib(egl, display, config,
                        EGL10.EGL_ALPHA_SIZE, 0);

                if (r == mRedSize && g == mGreenSize && b == mBlueSize && a == mAlphaSize)
                    return config;
            }
            return null;
        }

        private int findConfigAttrib(EGL10 egl, EGLDisplay display, EGLConfig config, int attribute, int defaultValue)
		{
			if (egl.eglGetConfigAttrib(display, config, attribute, mValue))
			{
                return mValue[0];
            }
            return defaultValue;
        }

        private void printConfigs(EGL10 egl, EGLDisplay display, EGLConfig[] configs)
		{
            int numConfigs = configs.length;
            Log.w(TAG, String.format("%d configurations", numConfigs));
            for (int i = 0; i < numConfigs; i++)
			{
                Log.w(TAG, String.format("Configuration %d:\n", i));
                printConfig(egl, display, configs[i]);
            }
        }

        private void printConfig(EGL10 egl, EGLDisplay display, EGLConfig config)
		{
            int[] attributes = 
			{
                    EGL10.EGL_BUFFER_SIZE,
                    EGL10.EGL_ALPHA_SIZE,
                    EGL10.EGL_BLUE_SIZE,
                    EGL10.EGL_GREEN_SIZE,
                    EGL10.EGL_RED_SIZE,
                    EGL10.EGL_DEPTH_SIZE,
                    EGL10.EGL_STENCIL_SIZE,
                    EGL10.EGL_CONFIG_CAVEAT,
                    EGL10.EGL_CONFIG_ID,
                    EGL10.EGL_LEVEL,
                    EGL10.EGL_MAX_PBUFFER_HEIGHT,
                    EGL10.EGL_MAX_PBUFFER_PIXELS,
                    EGL10.EGL_MAX_PBUFFER_WIDTH,
                    EGL10.EGL_NATIVE_RENDERABLE,
                    EGL10.EGL_NATIVE_VISUAL_ID,
                    EGL10.EGL_NATIVE_VISUAL_TYPE,
                    0x3030, // EGL10.EGL_PRESERVED_RESOURCES,
                    EGL10.EGL_SAMPLES,
                    EGL10.EGL_SAMPLE_BUFFERS,
                    EGL10.EGL_SURFACE_TYPE,
                    EGL10.EGL_TRANSPARENT_TYPE,
                    EGL10.EGL_TRANSPARENT_RED_VALUE,
                    EGL10.EGL_TRANSPARENT_GREEN_VALUE,
                    EGL10.EGL_TRANSPARENT_BLUE_VALUE,
                    0x3039, // EGL10.EGL_BIND_TO_TEXTURE_RGB,
                    0x303A, // EGL10.EGL_BIND_TO_TEXTURE_RGBA,
                    0x303B, // EGL10.EGL_MIN_SWAP_INTERVAL,
                    0x303C, // EGL10.EGL_MAX_SWAP_INTERVAL,
                    EGL10.EGL_LUMINANCE_SIZE,
                    EGL10.EGL_ALPHA_MASK_SIZE,
                    EGL10.EGL_COLOR_BUFFER_TYPE,
                    EGL10.EGL_RENDERABLE_TYPE,
                    0x3042 // EGL10.EGL_CONFORMANT
            };
			
            String[] names = 
			{
                    "EGL_BUFFER_SIZE",
                    "EGL_ALPHA_SIZE",
                    "EGL_BLUE_SIZE",
                    "EGL_GREEN_SIZE",
                    "EGL_RED_SIZE",
                    "EGL_DEPTH_SIZE",
                    "EGL_STENCIL_SIZE",
                    "EGL_CONFIG_CAVEAT",
                    "EGL_CONFIG_ID",
                    "EGL_LEVEL",
                    "EGL_MAX_PBUFFER_HEIGHT",
                    "EGL_MAX_PBUFFER_PIXELS",
                    "EGL_MAX_PBUFFER_WIDTH",
                    "EGL_NATIVE_RENDERABLE",
                    "EGL_NATIVE_VISUAL_ID",
                    "EGL_NATIVE_VISUAL_TYPE",
                    "EGL_PRESERVED_RESOURCES",
                    "EGL_SAMPLES",
                    "EGL_SAMPLE_BUFFERS",
                    "EGL_SURFACE_TYPE",
                    "EGL_TRANSPARENT_TYPE",
                    "EGL_TRANSPARENT_RED_VALUE",
                    "EGL_TRANSPARENT_GREEN_VALUE",
                    "EGL_TRANSPARENT_BLUE_VALUE",
                    "EGL_BIND_TO_TEXTURE_RGB",
                    "EGL_BIND_TO_TEXTURE_RGBA",
                    "EGL_MIN_SWAP_INTERVAL",
                    "EGL_MAX_SWAP_INTERVAL",
                    "EGL_LUMINANCE_SIZE",
                    "EGL_ALPHA_MASK_SIZE",
                    "EGL_COLOR_BUFFER_TYPE",
                    "EGL_RENDERABLE_TYPE",
                    "EGL_CONFORMANT"
            };
			
            int[] value = new int[1];
			
            for (int i = 0; i < attributes.length; i++)
			{
                int attribute = attributes[i];
                String name = names[i];
                if ( egl.eglGetConfigAttrib(display, config, attribute, value))
				{
                    Log.w(TAG, String.format("  %s: %d\n", name, value[0]));
                }
				else
				{
                    // Log.w(TAG, String.format("  %s: failed\n", name));
                    while (egl.eglGetError() != EGL10.EGL_SUCCESS);
                }
            }
        }

        // Subclasses can adjust these values:
        protected int mRedSize;
        protected int mGreenSize;
        protected int mBlueSize;
        protected int mAlphaSize;
        protected int mDepthSize;
        protected int mStencilSize;
        private int[] mValue = new int[1];
    }

    private static class Renderer implements GLSurfaceView.Renderer
	{
        public void onDrawFrame(GL10 gl)
		{
			if(JNILib.isExitRequested())
			{
				Log.w(TAG, "sActivity.finish()..");
				sActivity.finish();
			}
			else
			{
				if(sActivity.mIsTrial != sActivity.mPreviousIsTrial)
				{
					JNILib.setTrialMode(sActivity.mIsTrial);
					sActivity.mPreviousIsTrial = sActivity.mIsTrial;
				}
			
				if(JNILib.isBuyFullGameRequested())
				{
					Log.w(TAG, "isBuyFullGameRequested..");
					sActivity.purchase("com.frantic.vlad.full_game");
					JNILib.resetBuyFullGameRequest();
				}
				JNILib.step();
			}
        }

        public void onSurfaceChanged(GL10 gl, int width, int height)
		{
            JNILib.resizeScreen(width, height);
        }

        public void onSurfaceCreated(GL10 gl, EGLConfig config)
		{            
        }
    }
}