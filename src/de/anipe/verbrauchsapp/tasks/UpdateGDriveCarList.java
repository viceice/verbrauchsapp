package de.anipe.verbrauchsapp.tasks;

import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;

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

public class UpdateGDriveCarList extends AsyncTask<Void, Void, Void> {
    private GDriveImportFragment mCon;
    private int dataSets = 0;
    public HashMap<String, String> fileMapping;
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
            fileMapping = new HashMap<String, String>();

            MetadataBufferResult result = Drive.DriveApi
                    .getFolder(mClient, Drive.DriveApi.getRootFolder(mClient).getDriveId())
                    .listChildren(mClient).await();

            if (!result.getStatus().isSuccess())
            {
                Log.w("UpdateGDriveCarList", result.getStatus().getStatusMessage());
                return null;
            }

            MetadataBuffer buffer = result.getMetadataBuffer();
            try {
                for (int i = 0; i < buffer.getCount(); i++) {
                    Metadata meta = buffer.get(i);
                    String title = meta.getTitle();
                    if (!meta.isTrashed() && !(title == null)) {
                        filesList.add(title);
                        fileMapping.put(title, meta.getDriveId().encodeToString());
                        dataSets++;
                    }
                }

            } catch (Exception e) {
                Log.e("UpdateGDriveCarList", "Error listing files", e);
            } finally {
                buffer.release();
            }

            Collections.sort(filesList);

            return null;
        } finally {
            mClient.disconnect();
        }
    }

    @Override
    protected void onPostExecute(Void nope) {

        ((TabbedImportActivity) mCon.getActivity()).endRefreshFragment();

        if (dataSets == 0) {
            Toast.makeText(mCon.getActivity(),
                    "Keine gDrive Exports gefunden.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(mCon.getActivity(), dataSets + " gDrive Exports gefunden.",
                Toast.LENGTH_LONG).show();

        mCon.update(filesList, fileMapping);
    }
}