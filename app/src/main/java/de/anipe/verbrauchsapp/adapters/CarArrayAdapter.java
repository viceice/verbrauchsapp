package de.anipe.verbrauchsapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import de.anipe.verbrauchsapp.R;
import de.anipe.verbrauchsapp.objects.Car;

public class CarArrayAdapter extends ArrayAdapter<Car> {

	private Context context;
	private int layoutResourceId;
	private List<Car> cars;

	public CarArrayAdapter(Context context, int resource, List<Car> cars) {
		super(context, resource, cars);
		this.context = context;
		this.layoutResourceId = resource;
		this.cars = cars;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View row = convertView;
		CarEntryHolder holder = null;

		if (row == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new CarEntryHolder();
			holder.imgIcon = row.findViewById(R.id.brand_icon);
			holder.type = row.findViewById(R.id.typeLine);
			holder.number = row.findViewById(R.id.numberplateLine);

			row.setTag(holder);
		} else {
			holder = (CarEntryHolder) row.getTag();
		}

		Car car = cars.get(position);
		holder.type.setText(car.getType());
		holder.number.setText(car.getNumberPlate());
		holder.imgIcon.setImageBitmap(car.getIcon());

//		if (position % 2 == 0) {
//			row.setBackgroundColor(Color.LTGRAY);
//		} else {
//			row.setBackgroundColor(Color.WHITE);
//		}

		return row;
	}

	static class CarEntryHolder {
		ImageView imgIcon;
		TextView type;
		TextView number;
	}

	public void update(List<Car> cars) {
		clear();
		this.cars.addAll(cars);
	}

	@Override
	public int getCount() {
		return cars.size();
	}

	@Override
	public void clear() {
		cars.clear();
	}
}
