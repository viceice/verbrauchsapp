package de.anipe.verbrauchsapp.tasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import de.anipe.verbrauchsapp.TabbedImportActivity;
import de.anipe.verbrauchsapp.fragments.GDriveImportFragment;
import de.anipe.verbrauchsapp.io.XMLHandler;
import de.anipe.verbrauchsapp.util.ResetableCountDownLatch;

import static com.google.android.gms.common.api.GoogleApiClient.Builder;
import static com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import static com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import static com.google.android.gms.drive.DriveApi.*;
import static com.google.android.gms.drive.DriveApi.MetadataBufferResult;

/**
 * Created by kriese on 13.01.2015.
 */
public class ImportGDriveCar extends AsyncTask<String, Void, Void> {
    private Activity mCon;
    private ProgressDialog myprogsdial;
    private int dataSets = -2;
    private GoogleApiClient mClient;

    public ImportGDriveCar(Activity con) {
        mCon = con;
        Builder builder = new Builder(con)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE);
        mClient = builder.build();
    }

    @Override
    protected void onPreExecute() {
        myprogsdial = ProgressDialog.show(mCon,
                "Datensatz-Import", "Bitte warten ...", true);
    }

    @Override
    protected Void doInBackground(String... params) {
        Log.d("ImportGDriveCar", "Importing gdrive file.");
        final ResetableCountDownLatch latch = new ResetableCountDownLatch(1);
        mClient.registerConnectionCallbacks(new ConnectionCallbacks() {
            @Override
            public void onConnectionSuspended(int cause) {
            }

            @Override
            public void onConnected(Bundle arg0) {
                latch.countDown();
            }
        });
        mClient.registerConnectionFailedListener(new OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(ConnectionResult result) {
                try {
                    result.startResolutionForResult(mCon, 1);
                } catch (IntentSender.SendIntentException e) {
                    Log.e("ImportGDriveCar", "Exception while starting resolution activity", e);
                }
                latch.countDown();
            }
        });
        mClient.connect();
        try {
            latch.await();
        } catch (InterruptedException e) {
            return null;
        }
        if (!mClient.isConnected()) {
            return null;
        }
        try {

            DriveFolder folder = Drive.DriveApi.getFolder(mClient, Drive.DriveApi.getRootFolder(mClient).getDriveId());
            Query query = new Query.Builder()
                    .addFilter(Filters.eq(SearchableField.TITLE, params[0]))
                    .build();

            MetadataBuffer buffer = folder.queryChildren(mClient, query).await().getMetadataBuffer();
            DriveContents content = null;
            try {
                if (buffer.getCount() == 1) {
                    Metadata meta = buffer.get(0);
                    if (!meta.isTrashed() && params[0].equals(meta.getTitle())) {

                        DriveFile file = Drive.DriveApi.getFile(mClient, meta.getDriveId());

                        DriveContentsResult result = file.open(mClient, DriveFile.MODE_READ_ONLY, null).await();
                        if (!result.getStatus().isSuccess())
                            return null;

                        content = result.getDriveContents();

                        InputStream stream = content.getInputStream();

                        XMLHandler xmlImporter = new XMLHandler(mCon);
                        dataSets = xmlImporter.importXMLCarDataWithConsumption(stream);
                    }
                }
            } finally {
                if (content != null)
                    content.discard(mClient);

                buffer.release();
            }
        } catch (Exception e) {
            Log.e("ImportGDriveCar", "Exception while importing from gdrive. ", e);
        } finally {
            mClient.disconnect();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void nope) {
        myprogsdial.dismiss();
        if (dataSets < 0)
            Toast.makeText(mCon, "Fehler beim Importieren.",
                    Toast.LENGTH_LONG).show();
        else
            Toast.makeText(mCon,
                    "Fahrzeug mit " + dataSets + " Datensätzen importiert.",
                    Toast.LENGTH_LONG).show();
    }
}