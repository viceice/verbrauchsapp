package de.anipe.verbrauchsapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import de.anipe.verbrauchsapp.db.ConsumptionDataSource;
import de.anipe.verbrauchsapp.objects.Consumption;

public class GraphViewPlot extends AppCompatActivity {

	private ConsumptionDataSource dataSource;
	private long carId;

	private String[] xLabels;

	private double maxY = 0;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		carId = bundle.getLong("carid");

		setContentView(R.layout.xy_chart);

		dataSource = ConsumptionDataSource.getInstance(this);
		try {
			dataSource.open();
		} catch (SQLException e) {
			Toast.makeText(GraphViewPlot.this,
					"Fehler beim Öffnen der Datenbank!", Toast.LENGTH_LONG)
					.show();
		}

		GraphViewSeries conSeries = new GraphViewSeries(createDataLists());

		GraphView gView = new LineGraphView(this, "Verbrauch");

		gView.addSeries(conSeries);

		gView.setViewPort(2, 20);
		gView.setScrollable(true);
		// optional - activate scaling / zooming
		gView.setScalable(true);

		gView.getGraphViewStyle().setHorizontalLabelsColor(Color.BLUE);
		gView.getGraphViewStyle().setVerticalLabelsColor(Color.RED);
		gView.getGraphViewStyle().setVerticalLabelsWidth(50);

		gView.setHorizontalLabels(xLabels);

		LinearLayout layout = findViewById(R.id.chart);

		layout.addView(gView);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	private GraphViewData[] createDataLists() {
		List<Consumption> consList = dataSource.getConsumptionCycles(carId);

		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

		GraphViewData[] data = new GraphViewData[consList.size()];
		xLabels = new String[consList.size()];

		for (int i = 0; i < consList.size(); i++) {
			Consumption c = consList.get(i);
			double y = (Math.round(c.getConsumption()*100))/100.0;
			data[i] = new GraphViewData(i, y);
			xLabels[i] = sdf.format(c.getDate());
			if (c.getConsumption() > maxY) {
				maxY = c.getConsumption();
			}
		}
		maxY = Math.round(maxY + 2);

		return data;
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
}
