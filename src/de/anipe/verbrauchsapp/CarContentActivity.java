package de.anipe.verbrauchsapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;
import java.text.DecimalFormat;

import de.anipe.verbrauchsapp.db.ConsumptionDataSource;
import de.anipe.verbrauchsapp.io.FileSystemAccessor;
import de.anipe.verbrauchsapp.io.XMLHandler;
import de.anipe.verbrauchsapp.objects.Car;

/**
 * 
 */
public class CarContentActivity extends ActionBarActivity {

	private FileSystemAccessor accessor;
	private ConsumptionDataSource dataSource;
	private long carId;
	int listId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		carId = bundle.getLong("carid");
		listId = bundle.getInt("listid");

		setContentView(R.layout.activity_car_content);

		accessor = FileSystemAccessor.getInstance();
		dataSource = ConsumptionDataSource.getInstance(this);
		try {
			dataSource.open();
		} catch (SQLException e) {
			Toast.makeText(CarContentActivity.this,
					"Fehler beim Öffnen der Datenbank!", Toast.LENGTH_LONG)
					.show();
		}

		updateView();

		Button b1 = (Button) findViewById(R.id.addconsumption_button);
		b1.setOnClickListener(clickListener);

		Button b2 = (Button) findViewById(R.id.viewconsumptions_button);
		b2.setOnClickListener(clickListener);

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
			Toast.makeText(CarContentActivity.this,
					"Fehler beim Öffnen der Datenbank!", Toast.LENGTH_LONG)
					.show();
		}

		updateView();

		super.onResume();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.car_menubar, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.action_edit_entry:
			createCarEditActivity();
			return true;
		case R.id.action_add_consumption:
			createAddConsumptionActivity();
			return true;
		case R.id.action_add_picture:
			createAddPictureActivity();
			return true;
		case R.id.action_view_consumptions:
			createViewConsumptionsActivity();
			return true;
		case R.id.action_view_chart:
			createConsumptionPlotActivity();
			return true;
		case R.id.action_remove_entry:
			removeEntry();
			return true;
		case R.id.action_import_data:
			createImportDataActivity();
			return true;
		case R.id.action_export_data:
			// exportData();
			storeDriveData();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// Create an anonymous implementation of OnClickListener
	private OnClickListener clickListener = new OnClickListener() {
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.addconsumption_button:
				createAddConsumptionActivity();
				break;
			case R.id.viewconsumptions_button:
				createViewConsumptionsActivity();
				break;
			}
		}
	};

	private void createCarEditActivity() {
		Intent intent = new Intent(CarContentActivity.this,
				CarInputActivity.class);
		intent.putExtra("carid", carId);
		intent.putExtra("update", true);
		CarContentActivity.this.startActivity(intent);
	}

	private void createAddConsumptionActivity() {
		Intent intent = new Intent(CarContentActivity.this,
				ConsumptionInputActivity.class);
		intent.putExtra("carid", carId);
		intent.putExtra("kmstate", dataSource.getMileageForCar(carId));
		CarContentActivity.this.startActivity(intent);
	}

	private void createAddPictureActivity() {
		Intent intent = new Intent(CarContentActivity.this,
				PictureImportActivity.class);
		intent.putExtra("carid", carId);
		CarContentActivity.this.startActivity(intent);
	}

	private void createImportDataActivity() {
		Intent intent = new Intent(CarContentActivity.this,
				ImportActivity.class);
		intent.putExtra("carid", carId);
		CarContentActivity.this.startActivity(intent);
	}

	private void removeEntry() {

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				CarContentActivity.this);

		// set title
		alertDialogBuilder.setTitle("Eintrag löschen?");
		TypedValue typedValue = new TypedValue();
		getTheme().resolveAttribute(android.R.attr.alertDialogIcon, typedValue, true);
		alertDialogBuilder.setIcon(typedValue.resourceId);

		// set dialog message
		alertDialogBuilder
				.setMessage(
						"Mit 'Ja' wird der Fahrzeugdatensatz und alle dazugehörigen Verbrauchsdatensätze gelöscht!")
				.setCancelable(false)
				.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dataSource.deleteConsumptionsForCar(carId);
						dataSource.deleteCar(dataSource.getCarForId(carId));

						finish();
					}
				})
				.setNegativeButton("Nein",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
	}

	private void createViewConsumptionsActivity() {
		Intent intent = new Intent(CarContentActivity.this,
				ConsumptionListActivity.class);
		intent.putExtra("carid", carId);
		CarContentActivity.this.startActivity(intent);
	}

	private void createConsumptionPlotActivity() {
//		Intent intent = new Intent(CarContentActivity.this, PlotActivity.class);
//		Intent intent = new Intent(CarContentActivity.this, XYPlot.class);
		Intent intent = new Intent(CarContentActivity.this, GraphViewPlot.class);
		intent.putExtra("carid", carId);
		CarContentActivity.this.startActivity(intent);
	}

	private void updateView() {
		Car car = dataSource.getCarForId(carId);

		TextView header = (TextView) findViewById(R.id.fullscreen_car_content);
		header.setText(car.getBrand().value() + " " + car.getType());

		TextView startkm = (TextView) findViewById(R.id.startkmValueLine);
		startkm.setText(String.valueOf(car.getStartKm()));

		TextView actualkm = (TextView) findViewById(R.id.actualkmValueLine);
		int mileage = dataSource.getMileageForCar(carId);
		actualkm.setText(String.valueOf(mileage));

		TextView consumption = (TextView) findViewById(R.id.consumptionValueLine);
		consumption.setText(new DecimalFormat("#.00").format(dataSource
				.getOverallConsumptionForCar(carId)) + " l/100km");

		TextView overallKm = (TextView) findViewById(R.id.drivenKmValueLine);
		overallKm.setText(String.valueOf(mileage - car.getStartKm()));

		TextView overallCosts = (TextView) findViewById(R.id.overallCostsValueLine);
		overallCosts.setText(new DecimalFormat("#0.00").format(dataSource
				.getOverallCostsForCar(carId)) + " \u20ac");

		if (car.getImage() != null) {
			ImageView image = (ImageView) findViewById(R.id.pictureLine);
			image.setImageBitmap(car.getImage());
		}
	}

	@SuppressWarnings("unused")
	private void exportData() {
		Car car = dataSource.getCarForId(carId);
		XMLHandler handler = new XMLHandler(null, car,
				dataSource.getOverallConsumptionForCar(carId),
				dataSource.getConsumptionCycles(carId));
		try {
			accessor.writeXMLFileToStorage(this,
					handler.createConsumptionDocument(),
					MainActivity.STORAGE_DIR,
					car.getBrand() + "_" + car.getType());
			Toast.makeText(CarContentActivity.this,
					"Daten erfolgreich in XML-Datei exportiert!",
					Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			Toast.makeText(
					CarContentActivity.this,
					"Fehler beim Schreiben der XML-Datei. Grund: "
							+ e.getLocalizedMessage(), Toast.LENGTH_LONG)
					.show();
		}
	}

	private void storeDriveData() {
		Intent intent = new Intent(CarContentActivity.this,
				GDriveStoreActivity.class);
		intent.putExtra("carid", carId);
		CarContentActivity.this.startActivity(intent);
	}
}
