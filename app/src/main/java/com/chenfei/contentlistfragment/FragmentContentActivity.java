package com.chenfei.contentlistfragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

/**
 * 单个Fragment的Activity
 * Created by MrFeng on 2016/12/8.
 */
public abstract class FragmentContentActivity<F extends Fragment> extends AppCompatActivity {
    protected F mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView();

        if (savedInstanceState != null) {
            @SuppressWarnings("unchecked") F f = (F) getSupportFragmentManager().findFragmentByTag("Fragment");
            this.mFragment = f;
        }
        if (mFragment == null) {
            mFragment = createFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content, mFragment, "Fragment")
                    .commit();
        }
    }

    protected void setContentView() {
        setContentView(R.layout.unite_activity);
    }

    @NonNull
    protected abstract F createFragment();
}
