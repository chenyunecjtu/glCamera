package com.cody.glcamera;


import android.hardware.Camera;
import android.util.Log;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by cody on 16/6/22-下午7:49.
 * email: 1159807248@qq.com
 */
public class Utils {
    private static CameraSizeComparator sizeComparator = new CameraSizeComparator();

    public static final int TYPE_4_3  = 0; //4:3
    public static final int TYPE_16_9 = 1; //16:9


    //选择尺寸最小值 camera width
    public static Camera.Size getBestSize(List<Camera.Size> list, int minWidth, int type ) {
        if (list == null) {
            return null;
        }
        Collections.sort(list, sizeComparator);
        float rate = 1.33f;
        if (type == TYPE_16_9) {
            rate = 1.7777f;
        }

        int i = 0;
        for(Camera.Size s:list){
            if((s.width > minWidth) && equalRate(s, rate)){
                Log.i("Utils", "最终尺寸:w = " + s.width + "h = " + s.height);
                break;
            }
            i++;
        }

        return list.get(i);
    }
    public static boolean equalRate(Camera.Size s, float rate){
        float r = (float)(s.width)/(float)(s.height);
        if(Math.abs(r - rate) <= 0.2)
        {
            return true;
        }
        else{
            return false;
        }
    }


    public static int[] findClosestFpsRange(int expectedFps, List<int[]> fpsRanges) {
        expectedFps *= 1000;
        int[] closestRange = fpsRanges.get(0);
        int measure = Math.abs(closestRange[0] - expectedFps) + Math.abs(closestRange[1] - expectedFps);
        for (int[] range : fpsRanges) {
            if (range[0] <= expectedFps && range[1] >= expectedFps) {
                int curMeasure = Math.abs(range[0] - expectedFps) + Math.abs(range[1] - expectedFps);
                if (curMeasure < measure) {
                    closestRange = range;
                    measure = curMeasure;
                }
            }
        }
        return closestRange;
    }

    public static class CameraSizeComparator implements Comparator<Camera.Size> {
        //按升序排列
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            // TODO Auto-generated method stub
            if(lhs.width == rhs.width){
                return 0;
            }
            else if(lhs.width > rhs.width){
                return 1;
            }
            else{
                return -1;
            }
        }

    }

    static {
        System.loadLibrary("xiaoyao");
    }

    public static native void YUVtoRBGA(byte[] yuv, int width, int height, int[] out);

    public static native void YUVtoARBG(byte[] yuv, int width, int height, int[] out);
}
