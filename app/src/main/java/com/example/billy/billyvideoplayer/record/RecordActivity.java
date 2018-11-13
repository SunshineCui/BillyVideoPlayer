package com.example.billy.billyvideoplayer.record;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Toast;

import com.example.billy.billyvideoplayer.R;
import com.example.billy.billyvideoplayer.databinding.ActivityRecordBinding;
import com.example.billy.billyvideoplayer.view.RecordButtonView;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by Billy_Cui on 2018/11/12.
 * Describe:
 */

public class RecordActivity extends AppCompatActivity implements SurfaceHolder.Callback, View.OnClickListener, RecordButtonView.OnLongClickListener {

    private ActivityRecordBinding binding;
    private SurfaceHolder surfaceHolder;
    private Camera camera;
    private MediaRecorder mediaRecorder;
    //当前打开的摄像头标记 1--后,2--前
    private int currentCameraType = -1;
    //播放状态位
    private boolean isRecording;
    private File temFile;
    //    private MyTimer myTimer;
    private static final long TIME_MAX = 15 * 1000;
    private static final long TIME_INTERVAL = 500;

    private static final int RC_STORAGE = 1001;


    private Camera.Size size;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_record);
        binding.setOnClick(this);
        binding.recordButton.setOnLongClickListener(this);
        initView();
    }

    private void initView() {
        surfaceHolder = binding.svRecord.getHolder();
        surfaceHolder.addCallback(this);
        //设置一些参数方便后面绘图
        binding.svRecord.setFocusable(true);
        binding.svRecord.setKeepScreenOn(true);
        binding.svRecord.setFocusableInTouchMode(true);

//        binding.pbRecord.setMax(100);
//        binding.pbRecord.setProgress(100);
    }


    /**
     * 预览回调
     *
     * @param holder
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        surfaceHolder = holder;
        startPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        surfaceHolder = holder;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //停止预览并释放摄像头资源
        stopPreview();
        //停止录制
        stopRecord(true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_switch:
                if (isRecording) {
                    showText("录制中不可切换镜头!");
                    break;
                }
                switchCamera();
                break;
        }
    }

    /**
     * 旋转摄像头
     */
    private void switchCamera() {
        stopPreview();
        if (currentCameraType == 1) {
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            currentCameraType = 2;
            binding.btnSwitch.setText("Front");
        } else {
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            currentCameraType = 1;
            binding.btnSwitch.setText("Back");
        }
        startPreview();
    }

    /**
     * 开始预览
     */
    private void startPreview() {
        if (binding.svRecord == null || surfaceHolder == null) {
            return;
        }

        if (camera == null) {
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            currentCameraType = 1;
            binding.btnSwitch.setText("Back");
        }

        try {
            camera.setPreviewDisplay(surfaceHolder);
            Camera.Parameters parameters = camera.getParameters();
            camera.setDisplayOrientation(90);

            //实现Camera自动对焦
            List<String> focusModes = parameters.getSupportedFocusModes();
            if (focusModes != null) {
                for (String mode : focusModes) {
                    if (mode.contains("continuous-video"))
                        parameters.setFocusMode(mode);
                }
            }
            List<Camera.Size> sizes = parameters.getSupportedVideoSizes();
            if (sizes.size() > 0) {
                size = sizes.get(sizes.size() - 1);
            }

            camera.setParameters(parameters);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止录制
     *
     * @param delete 是否删除临时文件
     */
    private void stopRecord(boolean delete) {
        if (mediaRecorder == null) {
            return;
        }
//        if (myTimer != null) {
//            myTimer.cancel();
//        }

        mediaRecorder.stop();
        mediaRecorder.reset();
        mediaRecorder.release();
        mediaRecorder = null;
        isRecording = false;
        if (camera != null) {
            camera.lock();
        }

        if (delete) {
            if (temFile != null && temFile.exists()) {
                temFile.delete();
            }
        } else {
            //停止预览
            stopPreview();

            Intent intent = new Intent(RecordActivity.this, PrepareActivity.class);
            intent.putExtra(PrepareActivity.KEY_VIDEO_URL, temFile.getPath());
            startActivity(intent);

        }
    }

    /**
     * 停止预览
     */
    private void stopPreview() {
        //停止预览并释放摄像头资源
        if (camera == null) {
            return;
        }
        camera.setPreviewCallback(null);
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    /**
     * 开始录制
     */
    private void startRecord() {
        if (mediaRecorder == null) {
            mediaRecorder = new MediaRecorder();
        }
        temFile = getTemFile();
        try {
            camera.unlock();
            mediaRecorder.setCamera(camera);
            //以下设置(采集 < 格式 < 编码)要保持顺序
            //从相机采集视频
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            //从麦克风采集音频信息
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        /*------------------------------原始写法,清晰度低--------------------------------------*/
            //输出格式
//            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            //编码格式
//            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
//            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

//            mediaRecorder.setVideoSize(size.width, size.height);
        /*--------------------------------调整后写法------------------------------------*/
            //相机参数配置类
            // 直接采用QUALITY_HIGH,这样可以提高视频的录制质量，但是不能设置编码格式和帧率等参数。
            CamcorderProfile cProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
            mediaRecorder.setProfile(cProfile);
        /*--------------------------------------------------------------------*/
            //每秒的帧数
            mediaRecorder.setVideoFrameRate(30);
            //设置桢频率,越高越清晰了
            mediaRecorder.setVideoEncodingBitRate(2 * 1920 * 1080);

            mediaRecorder.setOutputFile(temFile.getAbsolutePath());
            mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
            //解决录制视频,播放器横向问题
            if (currentCameraType == 1) {
                //后置
                mediaRecorder.setOrientationHint(90);
            } else {
                //前置
                mediaRecorder.setOrientationHint(270);
            }

            mediaRecorder.prepare();
            //录制开始
            mediaRecorder.start();
            isRecording = true;
//            myTimer = new MyTimer(TIME_MAX, TIME_INTERVAL);
//            myTimer.start();
            
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 获取临时文件目录
     *
     * @return
     */
    private File getTemFile() {
        String basePath = Environment.getExternalStorageDirectory().getPath() + "/billyRecord/";
        File baseFile = new File(basePath);
        if (!baseFile.exists()) {
            baseFile.mkdirs();
        }
        return new File(basePath + System.currentTimeMillis() + ".mp4");
    }

    @Override
    public void onLongClick() {
        //开始录制
        if (!isRecording)
            startRecord();
    }

    @Override
    public void onNoMinRecord(int currentTime) {
    }

    @Override
    public void onRecordFinishedListener() {
        if (isRecording)
            stopRecord(false);
    }


//    public class MyTimer extends CountDownTimer {
//        /**
//         * @param millisInFuture    The number of millis in the future from the call
//         *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
//         *                          is called.
//         * @param countDownInterval The interval along the way to receive
//         *                          {@link #onTick(long)} callbacks.
//         */
//        public MyTimer(long millisInFuture, long countDownInterval) {
//            super(millisInFuture, countDownInterval);
//        }
//
//        @Override
//        public void onTick(long millisUntilFinished) {
//            int progress = (int) ((TIME_MAX - millisUntilFinished) / (double) TIME_MAX * 100);
//            binding.pbRecord.setProgress(progress);
//        }
//
//        @Override
//        public void onFinish() {
//            stopRecord(false);
//        }
//    }


    @Override
    protected void onStop() {
        super.onStop();
        stopPreview();
        stopRecord(true);
    }

    private void showText(String content) {
        Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
    }
}
