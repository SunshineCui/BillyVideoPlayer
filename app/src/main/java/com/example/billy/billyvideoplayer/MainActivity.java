package com.example.billy.billyvideoplayer;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.billy.billyvideoplayer.databinding.ActivityMainBinding;
import com.example.billy.billyvideoplayer.list.ListActivity;
import com.example.billy.billyvideoplayer.record.RecordActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        initView();
    }

    private void initView() {
        binding.setOnClick(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.text_view_record:
                startActivity(new Intent(this, RecordActivity.class));
                break;
            case R.id.text_view_list:
                startActivity(new Intent(this, ListActivity.class));
                break;
        }
    }
}
