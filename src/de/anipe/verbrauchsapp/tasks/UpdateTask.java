package de.anipe.verbrauchsapp.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import de.anipe.verbrauchsapp.TabbedImportActivity;

/**
 * Created by kriese on 13.01.2015.
 */
public class UpdateTask extends AsyncTask<Void, Void, Void> {

    private Context mCon;

    public UpdateTask(Context con)
    {
        mCon = con;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            // Set a time to simulate a long update process.
            Thread.sleep(4000);

            return null;

        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(Void nope) {
        // Give some feedback on the UI.
        Toast.makeText(mCon, "Finished complex background function!",
                Toast.LENGTH_LONG).show();

        // Change the menu back
        ((TabbedImportActivity) mCon).resetUpdating();
    }
}
