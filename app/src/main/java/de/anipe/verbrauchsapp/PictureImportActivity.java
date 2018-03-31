package de.anipe.verbrauchsapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.anipe.verbrauchsapp.db.ConsumptionDataSource;
import de.anipe.verbrauchsapp.io.FileSystemAccessor;

public class PictureImportActivity extends Activity implements AdapterView.OnItemClickListener {

	private static final int MAX_FILE_SIZE = 6000000;
	private ConsumptionDataSource dataSource;
	private FileSystemAccessor accessor;
	private Map<String, File> fileMapping;
	private long carId;
    private ArrayAdapter<String> adapter;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.csvimport_layout);

		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		carId = bundle.getLong("carid");

		dataSource = ConsumptionDataSource.getInstance(this);
		try {
			dataSource.open();
		} catch (SQLException e) {
			Toast.makeText(PictureImportActivity.this,
					"Fehler beim Öffnen der Datenbank!", Toast.LENGTH_LONG)
					.show();
		}

		ArrayList<String> filesList = new ArrayList<>();
		fileMapping = new HashMap<>();

		accessor = FileSystemAccessor.getInstance();
		File[] files = accessor.readFilesFromStorageDir(accessor
				.createOrGetStorageDir(MainActivity.STORAGE_DIR));
		if (files != null && files.length > 0) {
			for (File f : files) {
				String name = f.getName();
				if (isPictureFile(name.toLowerCase())) {
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


        getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	private boolean isPictureFile(String lowerCase) {
		return lowerCase.endsWith(".png") || lowerCase.endsWith(".jpg")
				|| lowerCase.endsWith(".bmp");
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                navigateUpTo(this.getParentActivityIntent());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String item = adapter.getItem(position);
        Bitmap bitMap = accessor.getBitmapForValue(fileMapping.get(item));

        if (bitMap.getByteCount() > MAX_FILE_SIZE) {
            Toast.makeText(this, "Datei darf maximal 1 MBte groß sein!",
                    Toast.LENGTH_LONG).show();
        } else {
            long result = dataSource.storeImageForCar(carId, bitMap);

            if (result > 0) {
                Toast.makeText(this, "Bild erfolgreich gespeichert!",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Fehler beim Speichern der Bilddatei",
                        Toast.LENGTH_LONG).show();
            }

            finish();
        }
    }
}
