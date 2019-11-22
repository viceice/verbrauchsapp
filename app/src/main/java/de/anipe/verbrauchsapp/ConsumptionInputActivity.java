package de.anipe.verbrauchsapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

import de.anipe.verbrauchsapp.db.ConsumptionDataSource;
import de.anipe.verbrauchsapp.fragments.DatePickerFragment;
import de.anipe.verbrauchsapp.objects.Consumption;

/**
 *
 */
public class ConsumptionInputActivity extends AppCompatActivity implements DatePickerFragment.OnFragmentInteractionListener {

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

        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        dataSource = ConsumptionDataSource.getInstance(this);
        try {
            dataSource.open();
        } catch (SQLException e) {
            Toast.makeText(ConsumptionInputActivity.this,
                "Fehler beim Öffnen der Datenbank!", Toast.LENGTH_LONG)
                .show();
        }

        EditText dateText = findViewById(R.id.datumTextLine);
        final Calendar c = Calendar.getInstance();
        dateText.setText(c.get(Calendar.DAY_OF_MONTH) + "."
            + (c.get(Calendar.MONTH) + 1) + "." + c.get(Calendar.YEAR));
        dateText.setOnClickListener(v -> {
            DialogFragment f = DatePickerFragment.newInstance(getCalendarFromDateText(dateText
                .getText().toString()));
            f.show(getSupportFragmentManager(), "datePicker");
        });


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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.consumption_input, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                navigateUpTo(this.getParentActivityIntent());
                return true;
            case R.id.action_done:
                done();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Calendar date) {
        EditText dateText = findViewById(R.id.datumTextLine);
        String dateSelected = String.valueOf(date.get(Calendar.DAY_OF_MONTH)) + "."
            + String.valueOf(date.get(Calendar.MONTH) + 1) + "."
            + String.valueOf(date.get(Calendar.YEAR));
        dateText.setText(dateSelected);
    }


    private void done() {
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
    }

    private boolean verify() {
        String kmState = ((EditText) findViewById(R.id.cons_km_input))
            .getText().toString();
        if (kmState.equals("")) {
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

    private Calendar getCalendarFromDateText(String text) {
        Calendar cal = Calendar.getInstance();
        String[] date = text.split("\\.");
        cal.set(Integer.parseInt(date[2]), Integer.parseInt(date[1]) - 1,
            Integer.parseInt(date[0]));

        return cal;
    }

    private Date getDateFromDateText(String text) {
        return getCalendarFromDateText(text).getTime();
    }

    private double calculateConsumption(double liters, int drivenKm) {
        return ((liters * 100) / drivenKm);
    }
}
