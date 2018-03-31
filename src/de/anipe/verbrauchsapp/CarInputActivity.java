package de.anipe.verbrauchsapp;

import java.sql.SQLException;

import de.anipe.verbrauchsapp.db.ConsumptionDataSource;
import de.anipe.verbrauchsapp.io.FileSystemAccessor;
import de.anipe.verbrauchsapp.objects.Brand;
import de.anipe.verbrauchsapp.objects.Car;
import de.anipe.verbrauchsapp.objects.Fueltype;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * 
 */
public class CarInputActivity extends ActionBarActivity {

	private ConsumptionDataSource dataSource;
	private FileSystemAccessor accessor;
	private long carId;
	private boolean update = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		if (bundle != null) {
			carId = bundle.getLong("carid");
			update = bundle.getBoolean("update");
		}

		setContentView(R.layout.inputform_car);

		dataSource = ConsumptionDataSource.getInstance(this);
		try {
			dataSource.open();
		} catch (SQLException e) {
			Toast.makeText(CarInputActivity.this,
					"Fehler beim Öffnen der Datenbank!", Toast.LENGTH_LONG)
					.show();
		}

		accessor = FileSystemAccessor.getInstance();

		Spinner fuelTypeSpinner = (Spinner) findViewById(R.id.fueltypes_spinner);
		ArrayAdapter<CharSequence> fuelAdapter = ArrayAdapter
				.createFromResource(this, R.array.fuel_types,
						android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		fuelAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		fuelTypeSpinner.setAdapter(fuelAdapter);

		Spinner brandSpinner = (Spinner) findViewById(R.id.brand_spinner);
		ArrayAdapter<CharSequence> brandAdapter = ArrayAdapter
				.createFromResource(this, R.array.brands,
						android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		brandAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		brandSpinner.setAdapter(brandAdapter);

		if (update) {
			Car car = dataSource.getCarForId(carId);

			((EditText) findViewById(R.id.car_type_input)).setText(car
					.getType());

			int brandPosition = brandAdapter
					.getPosition(car.getBrand().value());
			brandSpinner.setSelection(brandPosition);

			((EditText) findViewById(R.id.car_numberplate_input)).setText(car
					.getNumberPlate());

			((EditText) findViewById(R.id.car_startkm_input)).setText(String
					.valueOf(car.getStartKm()));

			int spinnerPosition = fuelAdapter.getPosition(car.getFuelType()
					.value());
			fuelTypeSpinner.setSelection(spinnerPosition);
		}

		Button addButton = (Button) findViewById(R.id.button_add_car);
		addButton.setOnClickListener(clickListener);

		if (update) {
			addButton.setText("Datensatz speichern");
		}


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	protected void onPause() {
		dataSource.close();
		super.onPause();
	}

	@Override
	protected void onResume() {
		try {
			dataSource.open();
		} catch (SQLException e) {
			Toast.makeText(CarInputActivity.this,
					"Fehler beim Öffnen der Datenbank!", Toast.LENGTH_LONG)
					.show();
		}
		super.onResume();
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

	// Create an anonymous implementation of OnClickListener
	private OnClickListener clickListener = new OnClickListener() {
		public void onClick(View v) {
			if (verify()) {
				Car car = new Car();
				car.setType(((EditText) findViewById(R.id.car_type_input))
						.getText().toString());
				car.setBrand(Brand
						.fromValue(((Spinner) findViewById(R.id.brand_spinner))
								.getSelectedItem().toString()));
				car.setNumberPlate(((EditText) findViewById(R.id.car_numberplate_input))
						.getText().toString());
				car.setStartKm(Integer
						.parseInt(((EditText) findViewById(R.id.car_startkm_input))
								.getText().toString()));
				car.setFuelType(Fueltype
						.fromValue(((Spinner) findViewById(R.id.fueltypes_spinner))
								.getSelectedItem().toString()));
				car.setIcon(accessor.getBitmapForBrand(getApplicationContext(),
						car.getBrand()));

				if (update) {
					car.setCarId(carId);
					car.setImage(dataSource.getImageForCarId(carId));
					if (dataSource.updateCar(car) > 0) {
						Toast.makeText(CarInputActivity.this,
								"Fahrzeugdatensatz geändert!",
								Toast.LENGTH_LONG).show();
						finish();
					} else {
						Toast.makeText(
								CarInputActivity.this,
								"Fehler beim Speichern des Fahrzeug-Datensatzes!",
								Toast.LENGTH_LONG).show();
					}
				} else {
					if (dataSource.addCar(car) > -1) {
						Toast.makeText(CarInputActivity.this,
								"Fahrzeug hinzugefügt!", Toast.LENGTH_LONG)
								.show();
						finish();
					} else {
						Toast.makeText(
								CarInputActivity.this,
								"Fehler beim Hinzufügen des Fahrzeug-Datensatzes!",
								Toast.LENGTH_LONG).show();
					}
				}
			}
		}

		private boolean verify() {
			String carType = ((EditText) findViewById(R.id.car_type_input))
					.getText().toString();
			if (carType == null || carType.equals("")) {
				Toast.makeText(CarInputActivity.this,
						"Fahrzeugtyp darf nicht leer sein!", Toast.LENGTH_LONG)
						.show();
				return false;
			}
			String carPlate = ((EditText) findViewById(R.id.car_numberplate_input))
					.getText().toString();
			if (carPlate == null || carPlate.equals("")) {
				Toast.makeText(CarInputActivity.this,
						"Kennzeichen darf nicht leer sein!", Toast.LENGTH_LONG)
						.show();
				return false;
			}
			String startKm = ((EditText) findViewById(R.id.car_startkm_input))
					.getText().toString();
			if (startKm == null || startKm.equals("")) {
				Toast.makeText(CarInputActivity.this,
						"Start-Kilometer darf nicht leer sein!",
						Toast.LENGTH_LONG).show();
				return false;
			}
			try {
				Integer.parseInt(startKm);
			} catch (NumberFormatException nfe) {
				Toast.makeText(
						CarInputActivity.this,
						"Start-Kilometer muss ein numerischer, ganzzahliger Wert sein!",
						Toast.LENGTH_LONG).show();
				return false;
			}

			return true;
		}
	};

}
