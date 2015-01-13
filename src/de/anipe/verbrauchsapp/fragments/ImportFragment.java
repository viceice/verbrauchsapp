package de.anipe.verbrauchsapp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import de.anipe.verbrauchsapp.R;

public abstract class ImportFragment extends Fragment implements AdapterView.OnItemClickListener {

    private ArrayAdapter<String> adapter;


    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container,
                                   Bundle savedInstanceState) {

        // The last two arguments ensure LayoutParams are inflated properly.
        ListView rootView = (ListView) inflater.inflate(R.layout.csvimport_layout, container, false);

        rootView.setAdapter(this.adapter);
        rootView.setOnItemClickListener(this);

        return rootView;
    }


    @Override
    public final void onCreate(Bundle savedInstanceState) {
        adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, new ArrayList<String>());
        super.onCreate(savedInstanceState);
    }

    @Override
    public final void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        onImport(adapter.getItem(position));
    }

    public void update(ArrayList<String> items) {
        adapter.clear();
        adapter.addAll(items);
    }

    public abstract void onImport(String name);

    public abstract void refresh();
}
