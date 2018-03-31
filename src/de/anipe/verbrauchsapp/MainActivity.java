package de.anipe.verbrauchsapp;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.sql.SQLException;

import de.anipe.verbrauchsapp.adapters.CarArrayAdapter;
import de.anipe.verbrauchsapp.db.ConsumptionDataSource;
import de.anipe.verbrauchsapp.fragments.CarContentFragment;
import de.anipe.verbrauchsapp.fragments.CarListFragment;
import de.anipe.verbrauchsapp.objects.Car;

/**
 *
 */
public class MainActivity extends ActionBarActivity {

    private ConsumptionDataSource dataSource;
    private ArrayAdapter<Car> adapter;
    public static final String STORAGE_DIR = "VerbrauchsApp";
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private Car mCar;
    private ActionBarDrawerToggle mDrawerToggle;

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

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerList.setAdapter(adapter);
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectCar(adapter.getItem(position), position);
                mDrawerLayout.closeDrawer(mDrawerList);
            }
        });

        if (mCar == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_frame, new CarListFragment())
                    .commit();
        }


        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                //getActionBar().setTitle(mTitle);
                //invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                //getActionBar().setTitle(mDrawerTitle);
                //invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        final FragmentManager fm = getSupportFragmentManager();
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
                if (args == null){
                    mCar = null;
                    mDrawerList.clearChoices();
                    mDrawerList.requestLayout();
                    return;
                }
                int pos = args.getInt("pos");
                mCar = adapter.getItem(pos);
                mDrawerList.setItemChecked(pos, true);
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

    public void selectCar(Car car, int pos) {
        mCar = car;
        Fragment fragment;

        if (car != null) {
            Bundle args = new Bundle();
            args.putLong("carid", car.getCarId());
            args.putInt("pos", pos);
            fragment = new CarContentFragment();
            fragment.setArguments(args);

        } else
            fragment = new CarListFragment();

        mDrawerList.setItemChecked(pos, true);

        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                .replace(R.id.content_frame, fragment, "car_" + pos)
                .addToBackStack(null)
                .commit();
        //Intent intent = new Intent(this, CarContentActivity.class);
        //intent.putExtra("carid", car.getCarId());

        //startActivityForResult(intent, 999);
    }
}
