package de.anipe.verbrauchsapp;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import de.anipe.verbrauchsapp.adapters.ImportPagerAdapter;
import de.anipe.verbrauchsapp.fragments.ImportFragment;

public class TabbedImportActivity extends ActionBarActivity {

    private ImportPagerAdapter pagerAdapter;
    private ViewPager viewPager;

    private int loader = 0;
    private ImageView iv;
    private RotateAnimation rotation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_import);

        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        pagerAdapter = new ImportPagerAdapter(this);
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(pagerAdapter);

        iv = (ImageView) getLayoutInflater().inflate(R.layout.iv_refresh, null);
        rotation = (RotateAnimation) AnimationUtils.loadAnimation(this, R.anim.rotate);
        rotation.setRepeatCount(Animation.INFINITE);
        iv.startAnimation(rotation);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.car_import, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                refreshFragment();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        refreshFragment();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem m = menu.findItem(R.id.action_refresh);
        if (loader > 0) {
            iv.startAnimation(rotation);
            m.setActionView(iv);
        } else {
            iv.clearAnimation();
            m.setActionView(null);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    private void refreshFragment() {
        ++loader;
        invalidateOptionsMenu();

        ((ImportFragment) pagerAdapter.getItem(viewPager.getCurrentItem())).refresh();
    }

    public void endRefreshFragment() {
        if (loader > 0)
            --loader;
        invalidateOptionsMenu();
    }
}

