package de.anipe.verbrauchsapp.tasks;

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
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import de.anipe.verbrauchsapp.TabbedImportActivity;
import de.anipe.verbrauchsapp.fragments.GDriveImportFragment;
import de.anipe.verbrauchsapp.util.ResetableCountDownLatch;

import static com.google.android.gms.common.api.GoogleApiClient.Builder;
import static com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import static com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import static com.google.android.gms.drive.DriveApi.MetadataBufferResult;

/**
 * Created by kriese on 13.01.2015.
 */
public class UpdateGDriveCarList extends AsyncTask<Void, Void, Void> {
    private GDriveImportFragment mCon;
    //private ProgressDialog myprogsdial;
    private int dataSets = 0;
    public HashMap<String, File> fileMapping;
    private ArrayList<String> filesList;
    private GoogleApiClient mClient;

    public UpdateGDriveCarList(GDriveImportFragment con) {
        mCon = con;
        Builder builder = new Builder(con.getActivity())
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE);
        mClient = builder.build();
    }

    @Override
    protected void onPreExecute() {
//        myprogsdial = ProgressDialog.show(mCon.getActivity(),
//                "Export-Suche", "Bitte warten ...", true);
    }

    @Override
    protected Void doInBackground(Void... params) {
        Log.d("UpdateGDriveCarList", "Listing gdrive files.");
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
                    result.startResolutionForResult(mCon.getActivity(), 1);
                } catch (IntentSender.SendIntentException e) {
                    Log.e("UpdateGDriveCarList", "Exception while starting resolution activity", e);
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
            filesList = new ArrayList<String>();
            //fileMapping = new HashMap<String, File>();
            latch.reset();

            DriveFolder folder = Drive.DriveApi.getFolder(mClient, Drive.DriveApi.getRootFolder(mClient).getDriveId());
            folder.listChildren(mClient).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {

                @Override
                public void onResult(MetadataBufferResult result) {
                    try {
                        MetadataBuffer buffer = result.getMetadataBuffer();

                        for (int i = 0; i < buffer.getCount(); i++) {
                            Metadata meta = buffer.get(i);
                            if (!meta.isTrashed() && !(meta.getTitle() == null)) {
                                filesList.add(meta.getTitle());
                                dataSets++;
                            }
                        }

                        buffer.release();
                    } catch (Exception e){
                        Log.e("UpdateGDriveCarList", "Error listing files", e);
                    } finally {
                        latch.countDown();
                    }
                }
            });

            try {
                latch.await();
            } catch (InterruptedException e) {
                return null;
            }

            Collections.sort(filesList);

            return null;
        } finally {
            mClient.disconnect();
        }
    }

    @Override
    protected void onPostExecute(Void nope) {
//        myprogsdial.dismiss();

        ((TabbedImportActivity) mCon.getActivity()).endRefreshFragment();

        if (dataSets == 0) {
            // Give some feedback on the UI.
            Toast.makeText(mCon.getActivity(),
                    "Keine gDrive Exports gefunden.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(mCon.getActivity(), dataSets + " gDrive Exports gefunden.",
                Toast.LENGTH_LONG).show();

        mCon.update(filesList);
    }
}