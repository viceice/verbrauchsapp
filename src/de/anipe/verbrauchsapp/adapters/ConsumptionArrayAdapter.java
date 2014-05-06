package de.anipe.verbrauchsapp.adapters;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import de.anipe.verbrauchsapp.R;
import de.anipe.verbrauchsapp.objects.Consumption;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ConsumptionArrayAdapter extends ArrayAdapter<Consumption> {

	private Context context;
	private int layoutResourceId;
	private List<Consumption> consumptions;
	private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy",
			Locale.getDefault());

	public ConsumptionArrayAdapter(Context context, int resource,
			List<Consumption> consumptions) {
		super(context, resource, consumptions);
		this.context = context;
		this.layoutResourceId = resource;
		this.consumptions = consumptions;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View row = convertView;
		ConsumptionEntryHolder holder = null;

		if (row == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new ConsumptionEntryHolder();
			holder.date = (TextView) row.findViewById(R.id.date_text);
			holder.refuelKm = (TextView) row.findViewById(R.id.refuelkm_text);
			holder.drivenKm = (TextView) row.findViewById(R.id.drivenkm_text);
			holder.liter = (TextView) row.findViewById(R.id.liter_text);
			holder.price = (TextView) row.findViewById(R.id.price_text);
			holder.cons = (TextView) row.findViewById(R.id.consumption_text);

			row.setTag(holder);
		} else {
			holder = (ConsumptionEntryHolder) row.getTag();
		}

		Consumption consumption = consumptions.get(position);
		holder.date.setText(dateFormat.format(consumption.getDate()));
		holder.refuelKm.setText(String.valueOf(consumption.getRefuelmileage()));
		holder.drivenKm.setText(String.valueOf(consumption.getDrivenmileage()));
		holder.liter.setText(new DecimalFormat("#.00").format(consumption
				.getRefuelliters()));
		holder.price.setText(new DecimalFormat("#0.000").format(consumption
				.getRefuelprice()));
		holder.cons.setText(new DecimalFormat("#0.00").format(consumption
				.getConsumption()));

		if (position % 2 == 0) {
			row.setBackgroundColor(Color.LTGRAY);
		} else {
			row.setBackgroundColor(Color.WHITE);
		}

		return row;
	}

	static class ConsumptionEntryHolder {
		TextView date;
		TextView refuelKm;
		TextView drivenKm;
		TextView liter;
		TextView price;
		TextView cons;
	}

	public void update(List<Consumption> consumptions) {
		clear();
		this.consumptions.addAll(consumptions);
	}

	@Override
	public int getCount() {
		return consumptions.size();
	}

	@Override
	public void clear() {
		consumptions.clear();
	}
}
