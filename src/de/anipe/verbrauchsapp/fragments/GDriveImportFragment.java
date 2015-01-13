package de.anipe.verbrauchsapp.fragments;

import android.widget.Toast;

import de.anipe.verbrauchsapp.tasks.UpdateGDriveCarList;

/**
 * Created by kriese on 13.01.2015.
 */
public class GDriveImportFragment extends ImportFragment {

    @Override
    public void onImport(String name) {
        // TODO: create task for importing from gdrive
        Toast.makeText(getActivity(), "TODO: Import from gDrive: " + name,
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void refresh() {
        new UpdateGDriveCarList(this).execute();
    }
}
