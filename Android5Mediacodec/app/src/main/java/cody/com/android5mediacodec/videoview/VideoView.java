package cody.com.android5mediacodec.videoview;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.FileDescriptor;

/**
 * Created by cody on 16/6/17-下午3:08.
 * email: 1159807248@qq.com
 */
public class VideoView extends SurfaceView implements SurfaceHolder.Callback {
    private static final int HANDLER_MSG_START = 0X00;
    private static final int HANDLER_MSG_STOP = 0X01;
    private SurfaceHolder holder;
    private long nativeObj = 0;
    private String url = null;

//    FileDescriptor fileDescriptor;
//    {
//        jclass cFileDescriptor = env->FindClass("java/io/FileDescriptor");
//        jmethodID iFileDescriptor = env->GetMethodID(cFileDescriptor, "<init>", "()V");
//        jfieldID descriptorID = env->GetFieldID(cFileDescriptor, "descriptor", "I");
//        mFileDescriptor = env->NewObject(cFileDescriptor, iFileDescriptor);
//        env->SetIntField(mFileDescriptor, descriptorID, (jint)fd);
//    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_MSG_START:
                    if ((holder != null)&& (url != null)) {
                        nativeStart();
                    }
                    break;
                case HANDLER_MSG_STOP:
                    nativeRelease();
                    break;
                default:
                    break;

            }
        }
    };


    public VideoView(Context context) {
        this(context, null);
    }

    public VideoView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public VideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        nativeObj = 0;
        setKeepScreenOn(true);
        getHolder().addCallback(this);
    }

    public void start(String url) {
        this.url = url;
        handler.sendEmptyMessage(HANDLER_MSG_START);
    }



    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        this.holder = holder;
        handler.sendEmptyMessage(HANDLER_MSG_START);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        handler.sendEmptyMessage(HANDLER_MSG_STOP);
    }

    private void nativeStart() {
        if (nativeObj != 0) {
            nativeRelease(nativeObj);
        }
        nativeObj = nativeCreate();
        nativeSetSurface(nativeObj, holder.getSurface());
        nativeInit(nativeObj);
        nativeStart(nativeObj, url);
    }

    private void nativeRelease() {
        if (nativeObj != 0) {
            nativeRelease(nativeObj);
            nativeObj = 0;
        }
    }


    static {
        System.loadLibrary("xiaoyao");
    }

    private native static long nativeCreate();
    private native static void nativeSetSurface(long obj, Surface surface);
    private native static int nativeInit(long obj);
    private native static int nativeStart(long obj, String url);

    private native static void nativeRelease(long obj);
}
