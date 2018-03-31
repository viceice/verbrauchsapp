package de.anipe.verbrauchsapp.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import de.anipe.verbrauchsapp.adapters.IOnCarSelected;
import de.anipe.verbrauchsapp.MainActivity;
import de.anipe.verbrauchsapp.R;
import de.anipe.verbrauchsapp.adapters.CarListAdapter;
import de.anipe.verbrauchsapp.db.ConsumptionDataSource;
import de.anipe.verbrauchsapp.objects.Car;
import de.anipe.verbrauchsapp.support.DividerItemDecoration;

public class CarListFragment extends Fragment implements IOnCarSelected {

    private ConsumptionDataSource dataSource;
    private CarListAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // The last two arguments ensure LayoutParams are inflated properly.
        RecyclerView rootView = (RecyclerView) inflater.inflate(R.layout.car_listview, container, false);

        rootView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        rootView.setLayoutManager(new LinearLayoutManager(getActivity()));
        rootView.setItemAnimator(new DefaultItemAnimator());
        rootView.setAdapter(this.adapter);
        rootView.setHasFixedSize(true);

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dataSource = ConsumptionDataSource.getInstance(getActivity());
        this.adapter = new CarListAdapter(dataSource.getCarList());
        adapter.setOnCarSelected(this);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menubar, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onResume() {
        super.onResume();

        adapter.update(dataSource.getCarList());
        adapter.notifyDataSetChanged();
    }

    @Override
    public void selected(Car car) {
        MainActivity a = (MainActivity) getActivity();
        a.selectCar(car);
    }
}
