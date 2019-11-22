package de.anipe.verbrauchsapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.Toast;

import java.sql.SQLException;

import de.anipe.verbrauchsapp.adapters.ConsumptionArrayAdapter;
import de.anipe.verbrauchsapp.db.ConsumptionDataSource;
import de.anipe.verbrauchsapp.objects.Consumption;

public class ConsumptionListActivity extends AppCompatActivity {

    private ConsumptionDataSource dataSource;
    private ConsumptionArrayAdapter adapter;
    private long carId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_consumption_listview);

        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        carId = bundle.getLong("carid");

        dataSource = ConsumptionDataSource.getInstance(this);
        try {
            dataSource.open();
        } catch (SQLException e) {
            Toast.makeText(ConsumptionListActivity.this,
                "Fehler beim Öffnen der Datenbank!", Toast.LENGTH_LONG)
                .show();
        }

        adapter = new ConsumptionArrayAdapter(this,
            R.layout.layout_consumption_view,
            dataSource.getConsumptionCycles(carId));
        ListView view = findViewById(android.R.id.list);
        view.setAdapter(adapter);

        registerForContextMenu(view);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume() {
        try {
            dataSource.open();
        } catch (SQLException e) {
            Toast.makeText(ConsumptionListActivity.this,
                "Fehler beim Öffnen der Datenbank!", Toast.LENGTH_LONG)
                .show();
        }
        adapter.notifyDataSetChanged();
        super.onResume();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
            .getMenuInfo();
        switch (item.getItemId()) {
            case R.id.delete_item:
                Consumption cons = adapter.getItem(
                    info.position);
                dataSource.deleteConsumption(cons.getId());
                adapter.update(dataSource.getConsumptionCycles(carId));
                adapter.notifyDataSetChanged();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                navigateUpTo(this.getParentActivityIntent());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
