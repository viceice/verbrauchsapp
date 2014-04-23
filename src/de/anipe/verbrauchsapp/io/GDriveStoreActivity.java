package de.anipe.verbrauchsapp.io;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.ContentsResult;
import com.google.android.gms.drive.DriveFolder.DriveFileResult;
import com.google.android.gms.drive.MetadataChangeSet;

import android.app.Activity;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


public class GDriveStoreActivity extends Activity implements
		ConnectionCallbacks, OnConnectionFailedListener {

	private static GoogleApiClient mGoogleApiClient;

	private static final int REQUEST_CODE_CREATOR = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Drive.API)
				.addScope(Drive.SCOPE_FILE).addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).build();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mGoogleApiClient == null) {
			mGoogleApiClient = new GoogleApiClient.Builder(this)
					.addApi(Drive.API).addScope(Drive.SCOPE_FILE)
					.addScope(Drive.SCOPE_APPFOLDER)
					// required for App Folder sample
					.addConnectionCallbacks(this)
					.addOnConnectionFailedListener(this).build();
		}
		mGoogleApiClient.connect();
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		Log.i("GDriveStoreActivity", "GoogleApiClient connected");
		Drive.DriveApi.newContents(mGoogleApiClient).setResultCallback(contentsCallback);
	}

	@Override
	public void onConnectionSuspended(int result) {
		Log.i("GDriveStoreActivity", "GoogleApiClient connection suspended");
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.i("GDriveStoreActivity", "GoogleApiClient connection failed: " + result.toString());
		if (!result.hasResolution()) {
			// show the localized error dialog.
			GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this, 0).show();
			return;
		}
		try {
			result.startResolutionForResult(this, REQUEST_CODE_CREATOR);
		} catch (SendIntentException e) {
			Log.e("GDriveStoreActivity",
					"Exception while starting resolution activity", e);
		}
	}

	private void showMessage(String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}

	final private ResultCallback<ContentsResult> contentsCallback = new ResultCallback<ContentsResult>() {
		@Override
		public void onResult(ContentsResult result) {
            if (!result.getStatus().isSuccess()) {
                showMessage("Error while trying to create new file contents");
                return;
            }

            MetadataChangeSet changeSet = new MetadataChangeSet.Builder().setTitle("New file")
                    .setMimeType("text/xml").setStarred(true).build();
            // create a file on root folder
            Drive.DriveApi.getRootFolder(mGoogleApiClient)
                    .createFile(mGoogleApiClient, changeSet, result.getContents())
                    .setResultCallback(fileCallback);
        }
	};

	private final ResultCallback<DriveFileResult> fileCallback = new ResultCallback<DriveFileResult>() {
		@Override
		public void onResult(DriveFileResult result) {
			if (!result.getStatus().isSuccess()) {
				showMessage("Error while trying to create the file");
				return;
			}
			showMessage("Created a file: " + result.getDriveFile().getDriveId());
		}
	};
}
