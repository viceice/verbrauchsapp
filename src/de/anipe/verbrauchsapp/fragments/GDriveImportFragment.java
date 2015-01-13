package de.anipe.verbrauchsapp.fragments;

import de.anipe.verbrauchsapp.tasks.ImportGDriveCar;
import de.anipe.verbrauchsapp.tasks.UpdateGDriveCarList;

/**
 * Created by kriese on 13.01.2015.
 */
public class GDriveImportFragment extends ImportFragment {

    @Override
    public void onImport(String name) {
        new ImportGDriveCar(getActivity()).execute(name);
    }

    @Override
    public void refresh() {
        new UpdateGDriveCarList(this).execute();
    }
}
