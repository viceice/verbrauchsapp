package de.anipe.verbrauchsapp.fragments;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import de.anipe.verbrauchsapp.tasks.CarLocalImportTask;
import de.anipe.verbrauchsapp.tasks.UpdateLocalCarList;

public class LocalImportFragment extends ImportFragment {

    private Map<String, File> fileMapping;


    public void update(ArrayList<String> names, Map<String, File> fileMapping) {
        this.fileMapping = fileMapping;
        update(names);
    }

    @Override
    public void refresh() {
        new UpdateLocalCarList(this).execute();
    }

    @Override
    public void onImport(String item) {
        new CarLocalImportTask(getActivity()).execute(fileMapping.get(item));
    }
}
