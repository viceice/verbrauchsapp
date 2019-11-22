package de.anipe.verbrauchsapp.fragments;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import de.anipe.verbrauchsapp.CarInputActivity;
import de.anipe.verbrauchsapp.MainActivity;
import de.anipe.verbrauchsapp.R;
import de.anipe.verbrauchsapp.adapters.CarListAdapter;
import de.anipe.verbrauchsapp.adapters.IOnCarSelected;
import de.anipe.verbrauchsapp.db.ConsumptionDataSource;
import de.anipe.verbrauchsapp.objects.Car;
import de.anipe.verbrauchsapp.support.DividerItemDecoration;

public class CarListFragment extends Fragment implements IOnCarSelected {

    private ConsumptionDataSource dataSource;
    private CarListAdapter adapter;
    private View rootView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // The last two arguments ensure LayoutParams are inflated properly.
        rootView = inflater.inflate(R.layout.car_listview, container, false);

        RecyclerView rv = rootView.findViewById(R.id.my_recycler_view);
        Activity parent = getActivity();

        rv.addItemDecoration(new DividerItemDecoration(parent, DividerItemDecoration.VERTICAL_LIST));
        rv.setLayoutManager(new LinearLayoutManager(parent));
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.setAdapter(this.adapter);
        rv.setHasFixedSize(true);

        FloatingActionButton btn = rootView.findViewById(R.id.float_add);
        btn.setOnClickListener(v -> {
            Intent intent = new Intent(parent, CarInputActivity.class);
            Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(parent).toBundle();
            startActivity(intent, bundle);
        });

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
