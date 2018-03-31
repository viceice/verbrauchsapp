package de.anipe.verbrauchsapp;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import de.anipe.verbrauchsapp.adapters.CarArrayAdapter;
import de.anipe.verbrauchsapp.db.ConsumptionDataSource;
import de.anipe.verbrauchsapp.fragments.CarContentFragment;
import de.anipe.verbrauchsapp.fragments.CarListFragment;
import de.anipe.verbrauchsapp.objects.Car;

/**
 *
 */
public class MainActivity extends Activity {

    private final CarListFragment mCarListFragment = new CarListFragment();
    private ConsumptionDataSource dataSource;
    private ArrayAdapter<Car> adapter;
    public static final String STORAGE_DIR = "VerbrauchsApp";
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private Car mCar;
    private ActionBarDrawerToggle mDrawerToggle;
    private final Map<Car, Fragment> mFragments = new HashMap<Car, Fragment>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        dataSource = ConsumptionDataSource.getInstance(this);
        try {
            dataSource.open();
        } catch (SQLException e) {
            Toast.makeText(MainActivity.this,
                    "Fehler beim Öffnen der Datenbank!", Toast.LENGTH_LONG)
                    .show();
        }

        adapter = new CarArrayAdapter(this, R.layout.drawer_car, dataSource.getCarList());

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerList = findViewById(R.id.left_drawer);

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerList.setAdapter(adapter);
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectCar(adapter.getItem(position));
                mDrawerLayout.closeDrawer(mDrawerList);
            }
        });

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, mCarListFragment)
                .commit();

        if (savedInstanceState != null) {
            long carid = savedInstanceState.getLong("carid");

            if (carid > 0) {
                selectCar(dataSource.getCarForId(carid));
            }
        }
//
//        mDrawerToggle = new ActionBarDrawerToggle(
//                this,                  /* host Activity */
//                mDrawerLayout,         /* DrawerLayout object */
//                R.string.drawer_open,  /* "open drawer" description for accessibility */
//                R.string.drawer_close  /* "close drawer" description for accessibility */
//        ) {
//            public void onDrawerClosed(View view) {
//                //getActionBar().setTitle(mTitle);
//                //invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
//            }
//
//            public void onDrawerOpened(View drawerView) {
//                //getActionBar().setTitle(mDrawerTitle);
//                //invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
//            }
//        };
//
//        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        final FragmentManager fm = getFragmentManager();
        fm.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            public int mCount;

            @Override
            public void onBackStackChanged() {
                int c = mCount;
                mCount = fm.getBackStackEntryCount();
                if (c < mCount)
                    return;

                Fragment f = fm.findFragmentById(R.id.content_frame);
                Bundle args = f.getArguments();
                if (args == null) {
                    mCar = null;
                    mDrawerList.clearChoices();
                    mDrawerList.requestLayout();
                    return;
                }
                int carid = args.getInt("carid");
                mCar = dataSource.getCarForId(carid);
                mDrawerList.setItemChecked(adapter.getPosition(mCar), true);
            }
        });

        mDrawerToggle.syncState();
    }

    @Override
    protected void onPause() {
        dataSource.close();
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        try {
            dataSource.open();
        } catch (SQLException e) {
            Toast.makeText(MainActivity.this,
                    "Fehler beim Öffnen der Datenbank!", Toast.LENGTH_LONG)
                    .show();
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        try {
            dataSource.open();
        } catch (SQLException e) {
            Toast.makeText(MainActivity.this,
                    "Fehler beim Öffnen der Datenbank!", Toast.LENGTH_LONG)
                    .show();
        }
        adapter.clear();
        adapter.addAll(dataSource.getCarList());
        adapter.notifyDataSetChanged();

        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            dataSource.open();
        } catch (SQLException e) {
            Toast.makeText(MainActivity.this,
                    "Fehler beim Öffnen der Datenbank!", Toast.LENGTH_LONG)
                    .show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mCar != null)
            outState.putLong("carid", mCar.getCarId());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        //MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.main_menubar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.action_add_car:
                createAddCarActivity();
                return true;
            case R.id.action_import_car:
                createImportCarActivity();
                return true;
            case R.id.action_quit:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void createImportCarActivity() {
//		Intent intent = new Intent(MainActivity.this, ImportActivity.class);
//		intent.putExtra("iscarimport", true);

        Intent intent = new Intent(MainActivity.this, TabbedImportActivity.class);
        MainActivity.this.startActivity(intent);
    }

    private void createAddCarActivity() {
        Intent intent = new Intent(MainActivity.this, CarInputActivity.class);
        MainActivity.this.startActivity(intent);
    }

    public void selectCar(Car car) {
        if (car != null && mCar != null && car.equals(mCar))
            return;

        mCar = car;
        int pos = adapter.getPosition(car);
        Fragment fragment;

        if (car != null) {
            fragment = mFragments.get(mCar);
            if (fragment == null) {
                Bundle args = new Bundle();
                args.putLong("carid", car.getCarId());
                fragment = new CarContentFragment();
                fragment.setArguments(args);
                mFragments.put(mCar, fragment);
            }
        } else
            fragment = mCarListFragment;

        mDrawerList.setItemChecked(pos, true);

        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                .replace(R.id.content_frame, fragment)
                .addToBackStack(null)
                .commit();
    }
}
