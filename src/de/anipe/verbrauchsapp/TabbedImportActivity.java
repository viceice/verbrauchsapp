package de.anipe.verbrauchsapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import de.anipe.verbrauchsapp.fragments.GDriveImportFragment;
import de.anipe.verbrauchsapp.fragments.ImportFragment;
import de.anipe.verbrauchsapp.fragments.LocalImportFragment;

public class TabbedImportActivity extends FragmentActivity {

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
        pagerAdapter = new ImportPagerAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(pagerAdapter);

        iv = (ImageView) getLayoutInflater().inflate(R.layout.iv_refresh, null);
        rotation = (RotateAnimation) AnimationUtils.loadAnimation(this, R.anim.rotate);
        rotation.setRepeatCount(Animation.INFINITE);
        iv.startAnimation(rotation);

        getActionBar().setDisplayHomeAsUpEnabled(true);
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

    // Since this is an object collection, use a FragmentStatePagerAdapter,
    // and NOT a FragmentPagerAdapter.
    public class ImportPagerAdapter extends FragmentStatePagerAdapter {

        private final LocalImportFragment localImportFragment;
        private final GDriveImportFragment gDriveImportFragment;

        public ImportPagerAdapter(FragmentManager fm) {
            super(fm);
            localImportFragment = new LocalImportFragment();
            gDriveImportFragment = new GDriveImportFragment();
        }

        @Override
        public Fragment getItem(int i) {

            if (i == 0)
                return localImportFragment;
            else
                return gDriveImportFragment;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return getString(R.string.actionbar_local);
            } else {
                return getString(R.string.actionbar_remote);
            }
        }
    }


}

