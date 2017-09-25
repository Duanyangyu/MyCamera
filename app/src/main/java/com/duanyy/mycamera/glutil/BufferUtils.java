package com.duanyy.mycamera.glutil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 * Created by duanyy on 2017/7/16.
 */

public class BufferUtils {

    public static FloatBuffer float2Buffer(float[] array){
        if (array == null)
            return null;
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(array.length*4);
        byteBuffer.order(ByteOrder.nativeOrder());
        FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
        floatBuffer.put(array);
        floatBuffer.position(0);
        return floatBuffer;
    }

    public static IntBuffer int2Buffer(int[] array){
        if (array != null) {
            return null;
        }
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(array.length*4);
        byteBuffer.order(ByteOrder.nativeOrder());
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        intBuffer.put(array);
        intBuffer.position(0);
        return intBuffer;
    }

    public static ShortBuffer short2Buffer(short[] array){
        if (array != null) {
            return null;
        }
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(array.length*2);
        byteBuffer.order(ByteOrder.nativeOrder());
        ShortBuffer shortBuffer = byteBuffer.asShortBuffer();
        shortBuffer.put(array);
        shortBuffer.position(0);
        return shortBuffer;
    }

}
