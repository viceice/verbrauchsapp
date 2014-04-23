package de.anipe.verbrauchsapp;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.anipe.verbrauchsapp.io.CSVHandler;
import de.anipe.verbrauchsapp.io.FileSystemAccessor;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class CSVImportActivity extends ListActivity {
	
	private FileSystemAccessor accessor;
	private CSVHandler csvImporter;
	private Map<String, File> fileMapping;
	private long carId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.csvimport_layout);
	    
	    Intent intent = getIntent();
	    Bundle bundle = intent.getExtras();
	    carId = bundle.getLong("carid");
	
	    ArrayList<String> filesList = new ArrayList<String>();
	    fileMapping = new HashMap<String, File>();
	    		
	    accessor = FileSystemAccessor.getInstance();
	    File [] files = accessor.readFilesFromStorageDir(accessor.createOrGetStorageDir(MainActivity.STORAGE_DIR));
	    if (files != null && files.length > 0) {
	    	for(File f : files) {
	    		String name = f.getName();
	    		if (name.toLowerCase().endsWith(".csv")) {
	    			filesList.add(f.getName());
		    		fileMapping.put(f.getName(), f);
	    		}
	    	}
	    } else {
	    	Toast.makeText(this, "Zielordner existiert nicht oder ist leer!", Toast.LENGTH_LONG).show();
	    }
		ListAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, filesList);
		setListAdapter(adapter);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		String item = (String) getListAdapter().getItem(position);
		Toast.makeText(this, "Importiere Datensätze aus Datei " + item, Toast.LENGTH_LONG).show();
		
		csvImporter = new CSVHandler(this);
		csvImporter.importCSVDataForCar(carId, fileMapping.get(item));
		
		finish();
	}
}
