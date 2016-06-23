package com.cody.glcamera;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.cody.glcamera.filter.GPUImageFilter;
import com.cody.glcamera.utils.OpenGlUtils;
import com.cody.glcamera.utils.Rotation;
import com.cody.glcamera.utils.TextureRotationUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.Queue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by cody on 16/6/23-上午10:05.
 * email: 1159807248@qq.com
 */
public class GLCameraRender implements GLSurfaceView.Renderer, Camera.PreviewCallback{

    public static final int NO_IMAGE = -1;
    static final float CUBE[] = {
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f,
    };

    private Queue<Runnable> mRunOnDraw = null;
    private Queue<Runnable> mRunOnDrawEnd = null;

    private int mGLTextureId = NO_IMAGE;

    private GPUImageFilter mFilter;

    private SurfaceTexture mSurfaceTexture;

    private int mOutputWidth;
    private int mOutputHeight;
    private int imageWidth = 0;
    private int imagHeight = 0;
    private final FloatBuffer mGLCubeBuffer; //二维坐标
    private final FloatBuffer mGLTextureBuffer;//纹理坐标数据
    private IntBuffer rgbaBuffer;

    private Rotation mRotation;


    private IGLCameraRender iglCameraRender;

    private  GLCameraView glCameraView;

    public GLCameraRender(GPUImageFilter filter, IGLCameraRender l) {
        mFilter = filter;
        iglCameraRender = l;
        mRunOnDraw    = new LinkedList<>();
        mRunOnDrawEnd = new LinkedList<>();
        mGLCubeBuffer = ByteBuffer.allocateDirect(CUBE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mGLCubeBuffer.put(CUBE).position(0);

        mGLTextureBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.TEXTURE_NO_ROTATION.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        setRotation(Rotation.ROTATION_90);
    }

    public void setView(final GLCameraView view) {
        glCameraView = view;
    }

    public void setRotation(Rotation rotation) {
        mRotation = rotation;
    }


    @Override
    public void onPreviewFrame(final byte[] data, final Camera camera) {
        Log.v("CAMERA", "onPreviewFrame");
        final Camera.Size size = camera.getParameters().getPreviewSize();
        if (imageWidth != size.width || imagHeight != size.height) {
            rgbaBuffer = IntBuffer.allocate(size.width*size.height);
            imageWidth = size.width;
            imagHeight = size.height;
        }
        if (mRunOnDraw.isEmpty()) {

            runOnDraw(new Runnable() {
                @Override
                public void run() {
                    Log.v("CAMERA1", "onPreviewFrame11111111");
                    Utils.YUVtoRBGA(data, imageWidth, imagHeight, rgbaBuffer.array());
                    mGLTextureId = OpenGlUtils.loadTexture(rgbaBuffer, size, mGLTextureId);
//                    iglCameraRender.CameraDataReadly();
                    adjustImageScaling();
                }
            });
        }
//        camera.addCallbackBuffer(data);
        glCameraView.requestRender();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0, 0, 0, 1);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        mFilter.init();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        mOutputWidth = width;
        mOutputHeight = height;
        GLES20.glViewport(0, 0, width, height);
        GLES20.glUseProgram(mFilter.getProgram());
        mFilter.onOutputSizeChanged(width, height);

    }


    @Override
    public void onDrawFrame(GL10 gl) {
        Log.v("gl", "onDrawFrame");
        runAll(mRunOnDraw);
        mFilter.onDraw(mGLTextureId, mGLCubeBuffer, mGLTextureBuffer);
        runAll(mRunOnDrawEnd);
//        if (mSurfaceTexture != null) {
//            mSurfaceTexture.updateTexImage();
//        }
    }



    public void startPreview(final Camera camera) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                try {
                    int[] id = new int[1];
                    GLES20.glGenTextures(1, id, 0);
                    mSurfaceTexture = new SurfaceTexture(id[0]);
                    camera.setPreviewTexture(mSurfaceTexture);
                    camera.setPreviewCallback(GLCameraRender.this);

                    camera.startPreview();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


    }

    private void adjustImageScaling() {
        float outputWidth = mOutputWidth;
        float outputHeight = mOutputHeight;

        if (mRotation == Rotation.ROTATION_270 || mRotation == Rotation.ROTATION_90) {
            outputWidth = mOutputHeight;
            outputHeight = mOutputWidth;
        }

//        float ratio1 = outputWidth / mImageWidth;
//        float ratio2 = outputHeight / mImageHeight;
//        float ratioMax = Math.max(ratio1, ratio2);
//        int imageWidthNew = Math.round(mImageWidth * ratioMax);
//        int imageHeightNew = Math.round(mImageHeight * ratioMax);
//
//        float ratioWidth = imageWidthNew / outputWidth;
//        float ratioHeight = imageHeightNew / outputHeight;

        float[] cube = CUBE;
        float[] textureCords = TextureRotationUtil.getRotation(mRotation, false, false);
//        if (mScaleType == GPUImage.ScaleType.CENTER_CROP) {
//            float distHorizontal = (1 - 1 / ratioWidth) / 2;
//            float distVertical = (1 - 1 / ratioHeight) / 2;
//            textureCords = new float[]{
//                    addDistance(textureCords[0], distHorizontal), addDistance(textureCords[1], distVertical),
//                    addDistance(textureCords[2], distHorizontal), addDistance(textureCords[3], distVertical),
//                    addDistance(textureCords[4], distHorizontal), addDistance(textureCords[5], distVertical),
//                    addDistance(textureCords[6], distHorizontal), addDistance(textureCords[7], distVertical),
//            };
//        } else {
//            cube = new float[]{
//                    CUBE[0] / ratioHeight, CUBE[1] / ratioWidth,
//                    CUBE[2] / ratioHeight, CUBE[3] / ratioWidth,
//                    CUBE[4] / ratioHeight, CUBE[5] / ratioWidth,
//                    CUBE[6] / ratioHeight, CUBE[7] / ratioWidth,
//            };
//        }

        mGLCubeBuffer.clear();
        mGLCubeBuffer.put(cube).position(0);
        mGLTextureBuffer.clear();
        mGLTextureBuffer.put(textureCords).position(0);
    }


    private void runAll(Queue<Runnable> queue) {
        synchronized (queue) {
            while (!queue.isEmpty()) {
                queue.poll().run();
            }
        }
    }



    protected void runOnDraw(final Runnable runnable) {
        synchronized (mRunOnDraw) {
            mRunOnDraw.add(runnable);
        }
    }

    protected void runOnDrawEnd(final Runnable runnable) {
        synchronized (mRunOnDrawEnd) {
            mRunOnDrawEnd.add(runnable);
        }
    }
}
