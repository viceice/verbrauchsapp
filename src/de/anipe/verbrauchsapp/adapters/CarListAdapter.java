package de.anipe.verbrauchsapp.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import de.anipe.verbrauchsapp.IOnCarSelected;
import de.anipe.verbrauchsapp.R;
import de.anipe.verbrauchsapp.objects.Car;


public class CarListAdapter extends RecyclerView.Adapter<CarListAdapter.ViewHolder> {

    private final List<Car> data;
    private IOnCarSelected selected;

    public CarListAdapter(List<Car> data) {
        this.data = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.layout_car_view, viewGroup, false);


        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        Car car = data.get(i);
        viewHolder.type.setText(car.getType());
        viewHolder.number.setText(car.getNumberPlate());
        viewHolder.imgIcon.setImageBitmap(car.getIcon());
        viewHolder.car = car;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setOnCarSelected(IOnCarSelected selected) {
        this.selected = selected;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgIcon;
        TextView type;
        TextView number;
        Car car;

        public ViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (CarListAdapter.this.selected != null)
                        CarListAdapter.this.selected.selected(car);
                }
            });

            imgIcon = (ImageView) itemView.findViewById(R.id.brand_icon);
            type = (TextView) itemView.findViewById(R.id.typeLine);
            number = (TextView) itemView.findViewById(R.id.numberplateLine);
        }
    }

    public void update(List<Car> cars) {
        clear();
        data.addAll(cars);
    }

    public void clear() {
        data.clear();
    }

    public Car getCar(int pos) {
        return data.get(pos);
    }
}
