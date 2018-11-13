package com.example.billy.billyvideoplayer.record;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.example.billy.billyvideoplayer.R;
import com.example.billy.billyvideoplayer.databinding.ActivityPrepareBinding;

import cn.jzvd.Jzvd;

/**
 * Created by Billy_Cui on 2018/11/12.
 * Describe:
 */

public class PrepareActivity extends AppCompatActivity{

    public final static String KEY_VIDEO_URL = "key";
    private ActivityPrepareBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_prepare);
        initVideo();
    }

    private void initVideo() {
        String url = getIntent().getStringExtra(KEY_VIDEO_URL);
        binding.videoPlayer.setUp(url,"", Jzvd.SCREEN_WINDOW_NORMAL);
        binding.videoPlayer.startVideo();
    }
}
