package de.anipe.verbrauchsapp;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

import de.anipe.verbrauchsapp.db.ConsumptionDataSource;
import de.anipe.verbrauchsapp.objects.Consumption;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

/**
 * 
 */
public class ConsumptionInputActivity extends ActionBarActivity {

	private ConsumptionDataSource dataSource;
	private long carId;
	private int kmState;
	final int Date_Dialog_ID = 19999;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		carId = bundle.getLong("carid");
		kmState = bundle.getInt("kmstate");

		setContentView(R.layout.inputform_consumption);

		dataSource = ConsumptionDataSource.getInstance(this);
		try {
			dataSource.open();
		} catch (SQLException e) {
			Toast.makeText(ConsumptionInputActivity.this,
					"Fehler beim Öffnen der Datenbank!", Toast.LENGTH_LONG)
					.show();
		}

		EditText dateText = (EditText) findViewById(R.id.datumTextLine);
		final Calendar c = Calendar.getInstance();
		dateText.setText(c.get(Calendar.DAY_OF_MONTH) + "."
				+ (c.get(Calendar.MONTH) + 1) + "." + c.get(Calendar.YEAR));
		dateText.setOnClickListener(clickListener);

		Button addButton = (Button) findViewById(R.id.button_add_consumption);
		addButton.setOnClickListener(clickListener);

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
			Toast.makeText(ConsumptionInputActivity.this,
					"Fehler beim Öffnen der Datenbank!", Toast.LENGTH_LONG)
					.show();
		}
		super.onResume();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		final Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DAY_OF_MONTH);
		switch (id) {
		case Date_Dialog_ID:
			return new DatePickerDialog(this, mDateSetListener, year, month,
					day);
		}
		return null;
	}

	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
		// onDateSet method
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			EditText dateText = (EditText) findViewById(R.id.datumTextLine);
			String dateSelected = String.valueOf(dayOfMonth) + "."
					+ String.valueOf(monthOfYear + 1) + "."
					+ String.valueOf(year);
			dateText.setText(dateSelected);
		}
	};

	// Create an anonymous implementation of OnClickListener
	private OnClickListener clickListener = new OnClickListener() {
		@SuppressWarnings("deprecation")
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.datumTextLine:
				showDialog(Date_Dialog_ID);
				break;
			case R.id.button_add_consumption:
				if (verify()) {
					Consumption cons = new Consumption();
					cons.setCarId(carId);
					cons.setDate(getDateFromDateText(((EditText) findViewById(R.id.datumTextLine))
							.getText().toString()));
					cons.setRefuelmileage(Integer
							.parseInt(((EditText) findViewById(R.id.cons_km_input))
									.getText().toString()));
					cons.setDrivenmileage(cons.getRefuelmileage() - kmState);
					cons.setRefuelliters(Double
							.parseDouble(((EditText) findViewById(R.id.cons_liter_input))
									.getText().toString().replace(',', '.')));
					String priceString = ((EditText) findViewById(R.id.cons_price_input))
							.getText().toString().replace(',', '.');
					cons.setRefuelprice(priceString.equals("") ? 0 : Double
							.parseDouble(priceString));
					cons.setConsumption(calculateConsumption(
							cons.getRefuelliters(), cons.getDrivenmileage()));

					if (dataSource.addConsumption(cons) > -1) {
						Toast.makeText(ConsumptionInputActivity.this,
								"Verbrauchsdatensatz hinzugefügt!",
								Toast.LENGTH_LONG).show();
						finish();
					} else {
						Toast.makeText(
								ConsumptionInputActivity.this,
								"Fehler beim Hinzufügen des Verbrauchsdatensatzes!",
								Toast.LENGTH_LONG).show();
					}
					finish();
				}
				break;
			}

		}

		private boolean verify() {
			String kmState = ((EditText) findViewById(R.id.cons_km_input))
					.getText().toString();
			if (kmState == null || kmState.equals("")) {
				Toast.makeText(ConsumptionInputActivity.this,
						"Kilometerstand darf nicht leer sein!",
						Toast.LENGTH_LONG).show();
				return false;
			}
			try {
				int val = Integer.parseInt(kmState);
				if (val - ConsumptionInputActivity.this.kmState <= 0) {
					Toast.makeText(
							ConsumptionInputActivity.this,
							"Kilometerstand muss größer als der letzte Kilometerstand sein!",
							Toast.LENGTH_LONG).show();
					return false;
				}
			} catch (NumberFormatException nfe) {
				Toast.makeText(ConsumptionInputActivity.this,
						"Kilometerstand ganzzahlig eingeben!",
						Toast.LENGTH_LONG).show();
				return false;
			}
			String liter = ((EditText) findViewById(R.id.cons_liter_input))
					.getText().toString().replace(',', '.');
			if (liter.equals("")) {
				Toast.makeText(ConsumptionInputActivity.this,
						"Literzahl darf nicht leer sein!", Toast.LENGTH_LONG)
						.show();
				return false;
			}
			try {
				Double.parseDouble(liter);
			} catch (NumberFormatException nfe) {
				Toast.makeText(
						ConsumptionInputActivity.this,
						"Literzahl ggf. mit Komma oder Punkt separiert eingeben!",
						Toast.LENGTH_LONG).show();
				return false;
			}
			String price = ((EditText) findViewById(R.id.cons_price_input))
					.getText().toString().replace(',', '.');
			if (!price.equals("")) {
				try {
					Double.parseDouble(price);
				} catch (NumberFormatException nfe) {
					Toast.makeText(
							ConsumptionInputActivity.this,
							"Preis ggf. mit Komma oder Punkt separiert eingeben!",
							Toast.LENGTH_LONG).show();
					return false;
				}
			}

			return true;
		}

		private Date getDateFromDateText(String text) {
			Calendar cal = Calendar.getInstance();
			String[] date = text.split("\\.");
			cal.set(Integer.parseInt(date[2]), Integer.parseInt(date[1]) - 1,
					Integer.parseInt(date[0]));

			return cal.getTime();
		}

		private double calculateConsumption(double liters, int drivenKm) {
			return ((liters * 100) / drivenKm);
		}
	};

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
