package de.anipe.verbrauchsapp.fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.anipe.verbrauchsapp.tasks.ImportGDriveCar;
import de.anipe.verbrauchsapp.tasks.UpdateGDriveCarList;

public class GDriveImportFragment extends ImportFragment {

    private Map<String, String> fileMapping;

    @Override
    public void onImport(String item) {
        new ImportGDriveCar(getActivity()).execute(fileMapping.get(item));
    }

    @Override
    public void refresh() {
        new UpdateGDriveCarList(this).execute();
    }

    public void update(ArrayList<String> items, HashMap<String, String> mapping){
        fileMapping = mapping;
        update(items);
    }
}
