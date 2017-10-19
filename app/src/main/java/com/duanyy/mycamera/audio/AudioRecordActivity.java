package com.duanyy.mycamera.audio;

import android.Manifest;
import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.duanyy.mycamera.R;
import com.duanyy.mycamera.utils.PermissionHelper;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 使用 AudioRecord 完成音频数据的采集，并实现读写音频 wav 文件
 * 参考：//https://github.com/Jhuster/AudioDemo
 */
public class AudioRecordActivity extends Activity {

    private static final String TAG = "AudioRecordActivity";

    private AudioRecord mAudioRecord;
    private Button mBtnRecord;
    private TextView mTvRecordTime;
    private TextView mTvRecordPath;
    private TextView mTvRecordSize;

    private File mAudioFile;
    private boolean isRecording;
    private byte[] mAudioData;
    private int mDataCount;

    private WriteThread mWriteThread;

    private static final String START_RECORD = "Start Record";
    private static final String STOP_RECOEED = "Stop Record";
    private int mSampleSize;
    private int mChannelConfig;
    private DataOutputStream mDataOutputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_record);

        initAudioRecorder();
        initView();

        checkPermission();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAudioRecord != null) {
            mAudioRecord.release();
            mAudioRecord = null;
        }
    }

    private void initAudioRecorder(){
        int audioSource = MediaRecorder.AudioSource.MIC;
        mSampleSize = 44100;
        mChannelConfig = AudioFormat.CHANNEL_IN_MONO;
        int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        int bufferSize = AudioRecord.getMinBufferSize(mSampleSize, mChannelConfig,audioFormat);

        mAudioRecord = new AudioRecord(audioSource, mSampleSize, mChannelConfig,audioFormat,bufferSize);
        mAudioData = new byte[bufferSize/2];
        Log.e(TAG,"initAudioRecorder bufferSize="+bufferSize);
    }

    private void initView(){
        mBtnRecord = findViewById(R.id.mBtnRecord);
        mBtnRecord.setOnClickListener(mOnClickListener);

        mTvRecordTime = findViewById(R.id.mRecordTimeTV);
        mTvRecordPath = findViewById(R.id.mRecordPathTV);
        mTvRecordSize = findViewById(R.id.mRecordSizeTV);
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isRecording){
                stopRecord();
                mBtnRecord.setText(START_RECORD);
            }else {
                startRecord();
                mBtnRecord.setText(STOP_RECOEED);
            }
        }
    };

    private void startRecord(){
        isRecording = true;
        mAudioRecord.startRecording();
        startWriteFile();
        Toast.makeText(this,"start Record~",Toast.LENGTH_SHORT).show();
    }

    private void stopRecord(){
        isRecording = false;
        if (mAudioRecord != null) {
            mAudioRecord.stop();
        }
        stopWriteFile();
        Toast.makeText(this,"stop Record~",Toast.LENGTH_SHORT).show();
    }

    private void startWriteFile(){
        mAudioFile = new File(getSavePath());
        if (mWriteThread == null) {
            mWriteThread = new WriteThread();
            mWriteThread.start();
        }
    }

    private void stopWriteFile(){
        writeDataSize();
        if (mDataOutputStream != null) {
            try {
                mDataOutputStream.flush();
                mDataOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mDataOutputStream = null;
        }
        mWriteThread = null;
    }

    private class WriteThread extends Thread{
        @Override
        public void run() {
            super.run();
            try {
                FileOutputStream fos = new FileOutputStream(mAudioFile);
                mDataOutputStream = new DataOutputStream(fos);
                writeHeader(mDataOutputStream,0,0,0);
                while (isRecording){
                    int read = mAudioRecord.read(mAudioData, 0, mAudioData.length);
                    if (read > 0){
                        mDataOutputStream.write(mAudioData,0,read);
                        mDataCount += read;
                    }
                    Log.e(TAG,"read.size="+read+", mDataCount="+mDataCount);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private String getSavePath(){
        String name = "MyAudio_"+System.currentTimeMillis()+".wav";
        File file = new File(Environment.getExternalStorageDirectory()+"/MyCamera",name);
        if (!file.getParentFile().exists()){
            file.mkdirs();
        }
        if (file.exists()){
            file.delete();
        }
        String path = file.getAbsolutePath();
        Log.e(TAG,"getSavePath path="+path);
        return path;
    }

    private boolean writeHeader(DataOutputStream mDataOutputStream,int  sampleRateInHz, int channels, int bitsPerSample) {
        if (mDataOutputStream == null) {
            return false;
        }

        WavFileHeader header = new WavFileHeader();

        try {
            mDataOutputStream.writeBytes(header.mChunkID);
            mDataOutputStream.write(intToByteArray(header.mChunkSize), 0, 4);
            mDataOutputStream.writeBytes(header.mFormat);
            mDataOutputStream.writeBytes(header.mSubChunk1ID);
            mDataOutputStream.write(intToByteArray(header.mSubChunk1Size), 0, 4);
            mDataOutputStream.write(shortToByteArray(header.mAudioFormat), 0, 2);
            mDataOutputStream.write(shortToByteArray(header.mNumChannel), 0, 2);
            mDataOutputStream.write(intToByteArray(header.mSampleRate), 0, 4);
            mDataOutputStream.write(intToByteArray(header.mByteRate), 0, 4);
            mDataOutputStream.write(shortToByteArray(header.mBlockAlign), 0, 2);
            mDataOutputStream.write(shortToByteArray(header.mBitsPerSample), 0, 2);
            mDataOutputStream.writeBytes(header.mSubChunk2ID);
            mDataOutputStream.write(intToByteArray(header.mSubChunk2Size), 0, 4);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private boolean writeDataSize() {
        if (mDataOutputStream == null) {
            return false;
        }

        try {
            RandomAccessFile wavFile = new RandomAccessFile(mAudioFile, "rw");
            wavFile.seek(WavFileHeader.WAV_CHUNKSIZE_OFFSET);
            wavFile.write(intToByteArray((mDataCount + WavFileHeader.WAV_CHUNKSIZE_EXCLUDE_DATA)), 0, 4);
            wavFile.seek(WavFileHeader.WAV_SUB_CHUNKSIZE2_OFFSET);
            wavFile.write(intToByteArray((mDataCount)), 0, 4);
            wavFile.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private static byte[] intToByteArray(int data) {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(data).array();
    }

    private static byte[] shortToByteArray(short data) {
        return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(data).array();
    }


    public class WavFileHeader {

        public static final int WAV_FILE_HEADER_SIZE = 44;
        public static final int WAV_CHUNKSIZE_EXCLUDE_DATA = 36;

        public static final int WAV_CHUNKSIZE_OFFSET = 4;
        public static final int WAV_SUB_CHUNKSIZE1_OFFSET = 16;
        public static final int WAV_SUB_CHUNKSIZE2_OFFSET = 40;

        public String mChunkID = "RIFF";
        public int mChunkSize = 0;
        public String mFormat = "WAVE";

        public String mSubChunk1ID = "fmt ";
        public int mSubChunk1Size = 16;
        public short mAudioFormat = 1;
        public short mNumChannel = 1;
        public int mSampleRate = 44100;
        public int mByteRate = 0;
        public short mBlockAlign = 0;
        public short mBitsPerSample = 16;

        public String mSubChunk2ID = "data";
        public int mSubChunk2Size = 0;

        public WavFileHeader() {
            mByteRate = mSampleRate * mNumChannel * mBitsPerSample / 8;
            mBlockAlign = (short) (mNumChannel * mBitsPerSample / 8);
        }

        public WavFileHeader(int sampleRateInHz, int channels, int bitsPerSample) {
            mSampleRate = sampleRateInHz;
            mBitsPerSample = (short) bitsPerSample;
            mNumChannel = (short) channels;
            mByteRate = mSampleRate * mNumChannel * mBitsPerSample / 8;
            mBlockAlign = (short) (mNumChannel * mBitsPerSample / 8);
        }
    }

    private void checkPermission(){
        String[] permissions = {
                Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        PermissionHelper helper = new PermissionHelper(this);
        boolean result = helper.checkPermissions(permissions);
        if (!result){
            ActivityCompat.requestPermissions(this,permissions,1);
        }
    }
}
