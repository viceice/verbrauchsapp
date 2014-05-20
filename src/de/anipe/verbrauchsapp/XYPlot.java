/**
 * Copyright (C) 2009 - 2013 SC 4ViewSoft SRL
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.anipe.verbrauchsapp;

import java.sql.SQLException;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import de.anipe.verbrauchsapp.db.ConsumptionDataSource;
import de.anipe.verbrauchsapp.objects.Consumption;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.Toast;

public class XYPlot extends Activity {
	/** The main dataset that includes all the series that go into a chart. */
	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	/** The main renderer that includes all the renderers customizing a chart. */
	private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
	/** The most recently added series. */
	private XYSeries mCurrentSeries;
	/** The most recently created renderer, customizing the current series. */
	private XYSeriesRenderer mCurrentRenderer;
	/** The chart view that displays the data. */
	private GraphicalView mChartView;
	
	private ConsumptionDataSource dataSource;
	private long carId;
	
	private double maxY = 0;

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// save the current data, for instance when changing screen orientation
		outState.putSerializable("dataset", mDataset);
		outState.putSerializable("renderer", mRenderer);
		outState.putSerializable("current_series", mCurrentSeries);
		outState.putSerializable("current_renderer", mCurrentRenderer);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedState) {
		super.onRestoreInstanceState(savedState);
		// restore the current data, for instance when changing the screen
		// orientation
		mDataset = (XYMultipleSeriesDataset) savedState
				.getSerializable("dataset");
		mRenderer = (XYMultipleSeriesRenderer) savedState
				.getSerializable("renderer");
		mCurrentSeries = (XYSeries) savedState
				.getSerializable("current_series");
		mCurrentRenderer = (XYSeriesRenderer) savedState
				.getSerializable("current_renderer");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		carId = bundle.getLong("carid");

		setContentView(R.layout.layout_consumption_plot);

		dataSource = ConsumptionDataSource.getInstance(this);
		try {
			dataSource.open();
		} catch (SQLException e) {
			Toast.makeText(XYPlot.this,
					"Fehler beim Öffnen der Datenbank!", Toast.LENGTH_LONG)
					.show();
		}
		
		setContentView(R.layout.xy_chart);

		// set some properties on the main renderer
		mRenderer.setApplyBackgroundColor(true);
		mRenderer.setBackgroundColor(Color.argb(100, 50, 50, 50));
		mRenderer.setAxisTitleTextSize(46);
		mRenderer.setChartTitleTextSize(40);
		mRenderer.setLabelsTextSize(35);
		mRenderer.setLegendTextSize(35);
		mRenderer.setMargins(new int[] { 10, 30, 15, 0 });
		mRenderer.setZoomButtonsVisible(true);
		mRenderer.setPointSize(10);
		mRenderer.setYAxisMin(0);
		
		
		
		if (mChartView == null) {
			LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
			mChartView = ChartFactory.getLineChartView(this, mDataset,
					mRenderer);
			// enable the chart click events
//			mRenderer.setClickEnabled(true);
//			mRenderer.setSelectableBuffer(10);
//			mChartView.setOnClickListener(new View.OnClickListener() {
//				public void onClick(View v) {
//					// handle the click event on the chart
//					SeriesSelection seriesSelection = mChartView
//							.getCurrentSeriesAndPoint();
//					if (seriesSelection == null) {
//						Toast.makeText(XYPlot.this, "No chart element",
//								Toast.LENGTH_SHORT).show();
//					} else {
//						// display information of the clicked point
//						Toast.makeText(
//								XYPlot.this,
//								"Chart element in series index "
//										+ seriesSelection.getSeriesIndex()
//										+ " data point index "
//										+ seriesSelection.getPointIndex()
//										+ " was clicked"
//										+ " closest point value X="
//										+ seriesSelection.getXValue() + ", Y="
//										+ seriesSelection.getValue(),
//								Toast.LENGTH_SHORT).show();
//					}
//				}
//			});
			layout.addView(mChartView, new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		} else {
			mChartView.repaint();
		}
		
		
		String seriesTitle = "Series " + (mDataset.getSeriesCount() + 1);
        // create a new series of data
        XYSeries series = new XYSeries(seriesTitle);
        mDataset.addSeries(series);
        mCurrentSeries = series;
        // create a new renderer for the new series
        XYSeriesRenderer renderer = new XYSeriesRenderer();
        mRenderer.addSeriesRenderer(renderer);
        // set some renderer properties
        renderer.setPointStyle(PointStyle.CIRCLE);
        renderer.setFillPoints(true);
        renderer.setDisplayChartValues(true);
        renderer.setDisplayChartValuesDistance(10);
        
        createDataLists();
        
        mRenderer.setYAxisMax(maxY);
        mCurrentRenderer = renderer;
        mChartView.repaint();

	}
	
	private void createDataLists() {
		List<Consumption> consList = dataSource.getConsumptionCycles(carId);
		
		System.out.println(consList.size());
		
		for (Consumption c : consList) {
		
			System.out.println("PING");
			
			double x = c.getDate().getTime()/100000000.0;
			
			System.out.println(x);
			
			double y = (double)((Math.round(c.getConsumption()*100))/100.0);
			
			System.out.println(y);
			
			mCurrentSeries.add(x, y);
			
			System.out.println("PONG");
			
			
			if (c.getConsumption() > maxY) {
				maxY = c.getConsumption();
			}
		}
		maxY = Math.round(maxY + 2);
	}
}