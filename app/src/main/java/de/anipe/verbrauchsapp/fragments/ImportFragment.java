package de.anipe.verbrauchsapp.fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
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

        View view = inflater.inflate(R.layout.csvimport_layout, container, false);
        // The last two arguments ensure LayoutParams are inflated properly.
        ListView rootView = view.findViewById(android.R.id.list);

        rootView.setAdapter(this.adapter);
        rootView.setOnItemClickListener(this);

        return rootView;
    }


    @Override
    public final void onCreate(Bundle savedInstanceState) {
        adapter = new ArrayAdapter<>(getActivity(),
            android.R.layout.simple_list_item_1, new ArrayList<>());
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
