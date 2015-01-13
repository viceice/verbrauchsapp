package de.anipe.verbrauchsapp.tasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.File;

import de.anipe.verbrauchsapp.io.XMLHandler;

public class CarLocalImportTask extends AsyncTask<File, Void, Void> {
    private Activity mCon;
    private ProgressDialog myprogsdial;
    private int dataSets = 0;

    public CarLocalImportTask(Activity con)
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
        Toast.makeText(mCon,
                "Fahrzeug mit " + dataSets + " Datensätzen importiert.",
                Toast.LENGTH_LONG).show();
    }
}
