package cody.com.android5mediacodec;

import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.cody.glcamera.GLCameraView;

import java.io.FileDescriptor;

import cody.com.android5mediacodec.videoview.VideoView;

public class MainActivity extends AppCompatActivity {
    VideoView videoView;
    private GLCameraView glCameraView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        glCameraView = (GLCameraView) findViewById(R.id.cameraView);
//        videoView = (VideoView) findViewById(R.id.video);
//        videoView.start("http://testlive.365yf.com/live/xpg4.flv");
    }





}
