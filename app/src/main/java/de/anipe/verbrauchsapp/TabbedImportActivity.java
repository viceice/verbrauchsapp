package de.anipe.verbrauchsapp;

import android.os.Bundle;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.Toast;

import de.anipe.verbrauchsapp.adapters.ImportPagerAdapter;
import de.anipe.verbrauchsapp.fragments.ImportFragment;

public class TabbedImportActivity extends AppCompatActivity {

    private ImportPagerAdapter pagerAdapter;
    private ViewPager viewPager;

    private int loader = 0;
    private ImageView iv;
    private RotateAnimation rotation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_import);

        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        pagerAdapter = new ImportPagerAdapter(this);
        viewPager = findViewById(R.id.pager);
        viewPager.setAdapter(pagerAdapter);

        iv = (ImageView) getLayoutInflater().inflate(R.layout.iv_refresh, null);
        rotation = (RotateAnimation) AnimationUtils.loadAnimation(this, R.anim.rotate);
        rotation.setRepeatCount(Animation.INFINITE);
        iv.startAnimation(rotation);

        ActionBar ab = getSupportActionBar();
        if (ab != null)
            ab.setDisplayHomeAsUpEnabled(true);
        else
            Toast.makeText(this, "Missing actionbar!", Toast.LENGTH_LONG).show();
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

