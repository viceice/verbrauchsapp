package de.anipe.verbrauchsapp;

import java.sql.SQLException;

import de.anipe.verbrauchsapp.adapters.CarArrayAdapter;
import de.anipe.verbrauchsapp.db.ConsumptionDataSource;
import de.anipe.verbrauchsapp.objects.Car;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

/**
 *
 */
public class MainActivity extends ActionBarActivity implements AdapterView.OnItemClickListener {

    private ConsumptionDataSource dataSource;
    private CarArrayAdapter adapter;
    public static final String STORAGE_DIR = "VerbrauchsApp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_car_listview);

        dataSource = ConsumptionDataSource.getInstance(this);
        try {
            dataSource.open();
        } catch (SQLException e) {
            Toast.makeText(MainActivity.this,
                    "Fehler beim Öffnen der Datenbank!", Toast.LENGTH_LONG)
                    .show();
        }

        adapter = new CarArrayAdapter(this, R.layout.layout_car_view,
                dataSource.getCarList());

        ListView view = (ListView) findViewById(android.R.id.list);
        view.setAdapter(adapter);
        view.setOnItemClickListener(this);
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
        adapter.update(dataSource.getCarList());
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menubar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Car item = (Car) parent.getAdapter().getItem(position);
        Intent intent = new Intent(MainActivity.this, CarContentActivity.class);
        intent.putExtra("carid", item.getCarId());
        intent.putExtra("listid", view.getId());

        MainActivity.this.startActivityForResult(intent, 999);
    }
}
