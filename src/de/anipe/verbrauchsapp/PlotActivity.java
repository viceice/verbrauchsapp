package de.anipe.verbrauchsapp;

import java.sql.SQLException;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

import de.anipe.verbrauchsapp.db.ConsumptionDataSource;
import de.anipe.verbrauchsapp.objects.Consumption;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class PlotActivity extends Activity {

	private XYPlot plot;
	private ConsumptionDataSource dataSource;
	private long carId;
	
	private double maxY = 0;

	private List<Long> dateList;
	private List<Double> valueList;

	private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy",
			Locale.getDefault());

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		carId = bundle.getLong("carid");

		setContentView(R.layout.layout_consumption_plot);

		dataSource = ConsumptionDataSource.getInstance(this);
		try {
			dataSource.open();
		} catch (SQLException e) {
			Toast.makeText(PlotActivity.this,
					"Fehler beim Öffnen der Datenbank!", Toast.LENGTH_LONG)
					.show();
		}

		createDataLists();

		// initialize our XYPlot reference:
		plot = (XYPlot) findViewById(R.id.consumption_plot);

		// Turn the above arrays into XYSeries':
		XYSeries series1 = new SimpleXYSeries(dateList, valueList,
				"Verbrauch (grafisch)");

		// Create a formatter to use for drawing a series using
		// LineAndPointRenderer
		// and configure it from xml:
		LineAndPointFormatter series1Format = new LineAndPointFormatter();
		series1Format.setPointLabelFormatter(new PointLabelFormatter());
		series1Format.configure(getApplicationContext(),
				R.xml.line_point_formatter_with_plf1);

		// add a new series' to the xyplot:
		plot.addSeries(series1, series1Format);

		plot.setDomainLowerBoundary(0, BoundaryMode.FIXED);
		plot.setDomainUpperBoundary(20, BoundaryMode.AUTO);
		
		
		// reduce the number of range labels
		plot.setTicksPerRangeLabel(3);
		plot.getGraphWidget().setDomainLabelOrientation(-45);

		plot.setDomainValueFormat(new MyDateFormat());
	}

	private void createDataLists() {
		List<Consumption> consList = dataSource.getConsumptionCycles(carId);

		dateList = new ArrayList<Long>();
		valueList = new ArrayList<Double>();

		for (Consumption c : consList) {
			dateList.add(c.getDate().getTime());
			valueList.add((double)((Math.round(c.getConsumption()*100))/100.0));
			if (c.getConsumption() > maxY) {
				maxY = c.getConsumption();
			}
		}
		maxY += 2;
	}

	private class MyDateFormat extends Format {

		private static final long serialVersionUID = 1L;

		@Override
		public StringBuffer format(Object obj, StringBuffer toAppendTo,
				FieldPosition pos) {
			long timestamp = ((Number) obj).longValue();
			Date date = new Date(timestamp);
			return dateFormat.format(date, toAppendTo, pos);
		}

		@Override
		public Object parseObject(String source, ParsePosition pos) {
			return null;
		}
	}
}
