package de.anipe.verbrauchsapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.anipe.verbrauchsapp.io.CSVHandler;
import de.anipe.verbrauchsapp.io.FileSystemAccessor;
import de.anipe.verbrauchsapp.io.XMLHandler;

public class ImportActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private Map<String, File> fileMapping;
    private long carId;
    private boolean isCarImport = false;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.csvimport_layout);

        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        carId = bundle.getLong("carid");
        isCarImport = bundle.getBoolean("iscarimport");

        ArrayList<String> filesList = new ArrayList<String>();
        fileMapping = new HashMap<String, File>();

        FileSystemAccessor accessor = FileSystemAccessor.getInstance();
        File[] files = accessor.readFilesFromStorageDir(accessor
            .createOrGetStorageDir(MainActivity.STORAGE_DIR));
        if (files != null && files.length > 0) {
            for (File f : files) {
                String name = f.getName();
                if (name.toLowerCase().endsWith(".xml")) {
                    filesList.add(f.getName());
                    fileMapping.put(f.getName(), f);
                }
                if (name.toLowerCase().endsWith(".csv") && !isCarImport) {
                    filesList.add(f.getName());
                    fileMapping.put(f.getName(), f);
                }
            }
        } else {
            Toast.makeText(this, "Zielordner existiert nicht oder ist leer!",
                Toast.LENGTH_LONG).show();
        }
        adapter = new ArrayAdapter<>(this,
            android.R.layout.simple_list_item_1, filesList);
        ListView view = findViewById(android.R.id.list);
        view.setAdapter(adapter);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String item = adapter.getItem(position);
        MyAsyncTask task = new MyAsyncTask();
        task.item = item;
        task.execute();
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

    protected int loadData(String item) {
        int dataSets = 0;
        XMLHandler xmlImporter;
        if (isCarImport) {
            xmlImporter = new XMLHandler(this);
            long carId = xmlImporter.importXMLCarData(fileMapping.get(item));
            dataSets = xmlImporter.importXMLConsumptionDataForCar(carId,
                fileMapping.get(item));
        } else {
            if (fileMapping.get(item).getName().toLowerCase().endsWith(".csv")) {
                CSVHandler csvImporter = new CSVHandler(this);
                dataSets = csvImporter.importCSVDataForCar(carId,
                    fileMapping.get(item));
            } else if (fileMapping.get(item).getName().toLowerCase()
                .endsWith(".xml")) {
                xmlImporter = new XMLHandler(this);
                dataSets = xmlImporter.importXMLConsumptionDataForCar(carId,
                    fileMapping.get(item));
            }
        }
        return dataSets;
    }

    class MyAsyncTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog myprogsdial;
        int dataSets = 0;
        boolean error = false;
        String item;

        @Override
        protected void onPreExecute() {
            myprogsdial = ProgressDialog.show(ImportActivity.this,
                "Datensatz-Import", "Bitte warten ...", true);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                dataSets = loadData(item);
            } catch (Throwable  e) {
                error = true;
                Toast.makeText(ImportActivity.this,
                    "Fehler: " + e.getLocalizedMessage(), Toast.LENGTH_LONG)
                    .show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            myprogsdial.dismiss();

            if (!error) {
                if (isCarImport) {
                    Toast.makeText(
                        ImportActivity.this,
                        "Fahrzeug mit " + dataSets + " Datensätzen importiert.",
                        Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(ImportActivity.this,
                        dataSets + " Datensätze importiert.", Toast.LENGTH_LONG)
                        .show();
                }
            }

            finish();
        }
    }
}
