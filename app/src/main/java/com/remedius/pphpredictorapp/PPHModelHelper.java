package com.remedius.pphpredictorapp;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class PPHModelHelper {
    private static final String MODEL_NAME = "pph_model.tflite";
    private Interpreter tflite;

    public PPHModelHelper(AssetManager assetManager) {
        try {
            tflite = new Interpreter(loadModelFile(assetManager));
        } catch (IOException e) {
            Log.e("PPHModelHelper", "Error loading model", e);
        }
    }

    private MappedByteBuffer loadModelFile(AssetManager assetManager) throws IOException {
        AssetFileDescriptor fileDescriptor = assetManager.openFd(MODEL_NAME);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long length = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, length);
    }

    public float predict(float[] inputData) {
        float[][] input = new float[1][6];
        input[0] = inputData;
        float[][] output = new float[1][1];
        tflite.run(input, output);
        return output[0][0];
    }
}
