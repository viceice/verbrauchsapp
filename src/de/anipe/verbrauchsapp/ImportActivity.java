package de.anipe.verbrauchsapp;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.anipe.verbrauchsapp.io.CSVHandler;
import de.anipe.verbrauchsapp.io.FileSystemAccessor;
import de.anipe.verbrauchsapp.io.XMLHandler;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class ImportActivity extends ListActivity {

	private FileSystemAccessor accessor;
	private CSVHandler csvImporter;
	private XMLHandler xmlImporter;
	private Map<String, File> fileMapping;
	private long carId;
	private boolean isCarImport = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.csvimport_layout);

		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		carId = bundle.getLong("carid");
		isCarImport = bundle.getBoolean("iscarimport");

		ArrayList<String> filesList = new ArrayList<String>();
		fileMapping = new HashMap<String, File>();

		accessor = FileSystemAccessor.getInstance();
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
		ListAdapter adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, filesList);
		setListAdapter(adapter);


        getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		String item = (String) getListAdapter().getItem(position);
		MyAsyncTask task = new MyAsyncTask();
		task.item = item;
		task.execute();
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

	protected int loadData(String item) {
		int dataSets = 0;
		if (isCarImport) {
			xmlImporter = new XMLHandler(this);
			long carId = xmlImporter.importXMLCarData(fileMapping.get(item));
			dataSets = xmlImporter.importXMLConsumptionDataForCar(carId,
					fileMapping.get(item));
		} else {
			if (fileMapping.get(item).getName().toLowerCase().endsWith(".csv")) {
				csvImporter = new CSVHandler(this);
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
		String item;

		@Override
		protected void onPreExecute() {
			myprogsdial = ProgressDialog.show(ImportActivity.this,
					"Datensatz-Import", "Bitte warten ...", true);
		}

		@Override
		protected Void doInBackground(Void... params) {
			dataSets = loadData(item);

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			myprogsdial.dismiss();

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
			
			finish();
		}
	}
}
