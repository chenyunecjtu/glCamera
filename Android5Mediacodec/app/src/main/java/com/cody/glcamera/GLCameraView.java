package com.cody.glcamera;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.cody.glcamera.filter.GPUImageFilter;

import java.util.List;

/**
 * Created by cody on 16/6/23-上午10:04.
 * email: 1159807248@qq.com
 */
public class GLCameraView extends GLSurfaceView implements IGLCameraRender {
    private static final String TAG = "GLCameraView";

    private GPUImageFilter imageFilter;

    private Camera camera = null;
    private static int cameraID = Camera.CameraInfo.CAMERA_FACING_BACK;
    private GLCameraRender glCameraRender;

    public GLCameraView(Context context) {
        this(context, null);
    }

    public GLCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        _init();
    }

    private void _init() {
        imageFilter = new GPUImageFilter(GPUImageFilter.NO_FILTER_VERTEX_SHADER, GPUImageFilter.NO_FILTER_FRAGMENT_SHADER);
        glCameraRender = new GLCameraRender(imageFilter, this);
        glCameraRender.setView(this);
        setKeepScreenOn(true);

        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        getHolder().setFormat(PixelFormat.RGBA_8888);
        setRenderer(glCameraRender);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        requestRender();
    }

    public void switchCamera() {
        if (cameraID == Camera.CameraInfo.CAMERA_FACING_BACK && hasFrontCamera()) {
            cameraID = Camera.CameraInfo.CAMERA_FACING_FRONT;
        } else {
            cameraID = Camera.CameraInfo.CAMERA_FACING_FRONT;
        }
        stopPreview();
        startPreview(glCameraRender);
    }

    public void stopPreview() {
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        super.surfaceCreated(holder);
        startPreview(glCameraRender);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
        stopPreview();

    }

    private void startPreview(GLCameraRender render) {
        camera = openCamera(cameraID);
        if (camera == null) {
            Log.e(TAG, "camera open failed");
            return;
        }
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPictureFormat(ImageFormat.JPEG);
        parameters.set("jpeg-quality", 80);//照片质量

//        parameters.getSupportedPreviewFormats();
        int defaultPreviewFormat = parameters.getPreviewFormat();
        if ((defaultPreviewFormat != ImageFormat.YV12) && (defaultPreviewFormat != ImageFormat.NV21)) {
            parameters.setPreviewFormat(ImageFormat.NV21);
        }

        parameters.set("orientation", "portrait");//竖屏
        camera.setDisplayOrientation(90);
        parameters.setRotation(90);

        /* 获取摄像头支持的PreviewSize列表 */
        List<Camera.Size> previewSizeList = parameters.getSupportedPreviewSizes();
        Camera.Size size = Utils.getBestSize(previewSizeList, 640, Utils.TYPE_16_9);
        if (size != null) {
            parameters.setPreviewSize(size.width, size.height);
        }

        int[] range = Utils.findClosestFpsRange(20, parameters.getSupportedPreviewFpsRange());
        parameters.setPreviewFpsRange(range[0], range[1]);

        camera.setParameters(parameters);
        render.startPreview(camera);
    }


    public static Boolean hasFrontCamera() {
        if (Camera.getNumberOfCameras() > 2) {
            return true;
        }
        return false;
    }

    private Camera openCamera(int id) {
        Camera camera = null;
        if (camera != null) {
            camera.release();
        }

        camera = Camera.open(getCameraId(id));
        return camera;
    }

    private int getCameraId(final int id) {
        int cameraNums = Camera.getNumberOfCameras();
        Camera.CameraInfo info = new Camera.CameraInfo();

        for (int i = 0; i < cameraNums; i++) {
            Camera.getCameraInfo(i, info);
            if (info.facing == id) {
                return i;
            }
        }
        return -1;
    }


    @Override
    public void onGLCameraRenderCreate() {
//        startPreview(glCameraRender);
    }

    @Override
    public void onGLCameraRenerDestory() {
//        stopPreview();
    }
}
