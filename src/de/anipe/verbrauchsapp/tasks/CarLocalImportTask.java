package de.anipe.verbrauchsapp.tasks;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.File;

import de.anipe.verbrauchsapp.TabbedImportActivity;
import de.anipe.verbrauchsapp.io.XMLHandler;

/**
 * Created by kriese on 13.01.2015.
 */
public class CarLocalImportTask extends AsyncTask<File, Void, Void> {
    private TabbedImportActivity mCon;
    private ProgressDialog myprogsdial;
    private int dataSets = 0;

    public CarLocalImportTask(TabbedImportActivity con)
    {
        mCon = con;
    }

    @Override
    protected void onPreExecute() {
        myprogsdial = ProgressDialog.show(mCon,
                "Datensatz-Import", "Bitte warten ...", true);
    }

    @Override
    protected Void doInBackground(File... params) {
        XMLHandler xmlImporter = new XMLHandler(mCon);
        File file = params[0];
        long carId = xmlImporter.importXMLCarData(file);
        dataSets = xmlImporter.importXMLConsumptionDataForCar(carId, file);

        return null;
    }

    @Override
    protected void onPostExecute(Void nope) {
        myprogsdial.dismiss();
        // Give some feedback on the UI.
        Toast.makeText(mCon,
                "Fahrzeug mit " + dataSets + " Datensätzen importiert.",
                Toast.LENGTH_LONG).show();

        // Change the menu back
        //mCon.endRefreshFragment();
    }
}
