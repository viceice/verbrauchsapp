package de.anipe.verbrauchsapp.tasks;

import android.os.AsyncTask;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import de.anipe.verbrauchsapp.MainActivity;
import de.anipe.verbrauchsapp.TabbedImportActivity;
import de.anipe.verbrauchsapp.fragments.LocalImportFragment;
import de.anipe.verbrauchsapp.io.FileSystemAccessor;

/**
 * Created by kriese on 13.01.2015.
 */
public class UpdateLocalCarList extends AsyncTask<Void, Void, Void> {
    private LocalImportFragment mCon;
    //private ProgressDialog myprogsdial;
    private int dataSets = 0;
    public HashMap<String, File> fileMapping;
    private ArrayList<String> filesList;

    public UpdateLocalCarList(LocalImportFragment con) {
        mCon = con;
    }

    @Override
    protected void onPreExecute() {
//        myprogsdial = ProgressDialog.show(mCon.getActivity(),
//                "Export-Suche", "Bitte warten ...", true);
    }

    @Override
    protected Void doInBackground(Void... params) {
        filesList = new ArrayList<String>();
        fileMapping = new HashMap<String, File>();

        FileSystemAccessor accessor = FileSystemAccessor.getInstance();
        File[] files = accessor.readFilesFromStorageDir(accessor
                .createOrGetStorageDir(MainActivity.STORAGE_DIR));

        if (files != null && files.length > 0) {
            for (File f : files) {
                String name = f.getName();
                if (name.toLowerCase().endsWith(".xml")) {
                    filesList.add(f.getName());
                    fileMapping.put(f.getName(), f);
                }
            }
            dataSets++;

            Collections.sort(filesList);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void nope) {
//        myprogsdial.dismiss();

        TabbedImportActivity activity = (TabbedImportActivity) mCon.getActivity();

        if (activity != null) {
            activity.endRefreshFragment();

            if (dataSets == 0) {
                // Give some feedback on the UI.
                Toast.makeText(mCon.getActivity(),
                        "Keine lokalen Exports gefunden.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            Toast.makeText(mCon.getActivity(), dataSets + " Exports gefunden.",
                    Toast.LENGTH_LONG).show();
        }

        mCon.update(filesList, fileMapping);
    }
}