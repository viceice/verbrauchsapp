package de.anipe.verbrauchsapp;

import java.sql.SQLException;

import de.anipe.verbrauchsapp.adapters.CarArrayAdapter;
import de.anipe.verbrauchsapp.db.ConsumptionDataSource;
import de.anipe.verbrauchsapp.objects.Car;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class CarListActivity extends ListActivity {

	private ConsumptionDataSource dataSource;
	private CarArrayAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    
	    dataSource = ConsumptionDataSource.getInstance(this);
	    try {
			dataSource.open();
		} catch (SQLException e) {
			Toast.makeText(CarListActivity.this, "Fehler beim Öffnen der Datenbank!", Toast.LENGTH_LONG).show();
		}
	    
		adapter = new CarArrayAdapter(this, R.layout.layout_car_view, dataSource.getCarList());
		getListView().setAdapter(adapter);
		
		registerForContextMenu(getListView());
	}
	
	@Override
	protected void onResume() {
		try {
			dataSource.open();
		} catch (SQLException e) {
			Toast.makeText(CarListActivity.this, "Fehler beim Öffnen der Datenbank!", Toast.LENGTH_LONG).show();
		}
		adapter.notifyDataSetChanged();
		super.onResume();
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.context_menu, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.delete_item: 
			Car car = (Car) getListAdapter().getItem(info.position);
			dataSource.deleteCar(car);
			adapter.update(dataSource.getCarList());
			adapter.notifyDataSetChanged();
			return true;
		}
		return super.onContextItemSelected(item);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Car item = (Car) getListAdapter().getItem(position);		
		Intent intent = new Intent(CarListActivity.this, CarContentActivity.class);
		intent.putExtra("carid", item.getCarId());
		CarListActivity.this.startActivity(intent);
	}
}
