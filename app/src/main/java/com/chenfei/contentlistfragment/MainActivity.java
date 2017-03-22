package com.chenfei.contentlistfragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_baseContent).setOnClickListener(v -> startActivity(new Intent(this, BannerListActivity.class)));
        findViewById(R.id.btn_content).setOnClickListener(v -> startActivity(new Intent(this, ListActivity.class)));
    }
}
