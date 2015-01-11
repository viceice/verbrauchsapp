package de.anipe.verbrauchsapp.io;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.DriveApi.DriveIdResult;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.MetadataBufferResult;
import com.google.android.gms.drive.DriveFolder;

import de.anipe.verbrauchsapp.GDriveBaseActivity;

public class GDriveListFilesExecutor extends GDriveBaseActivity {
	
	@Override
	public void onConnected(Bundle connectionHint) {
		super.onCreate(connectionHint);
		
		Drive.DriveApi.fetchDriveId(getGoogleApiClient(), Drive.DriveApi.getRootFolder(getGoogleApiClient()).getDriveId().encodeToString())
				.setResultCallback(idCallback);
	}

	final private ResultCallback<DriveIdResult> idCallback = new ResultCallback<DriveIdResult>() {
		@Override
		public void onResult(DriveIdResult result) {
			if (!result.getStatus().isSuccess()) {
				Log.e("GDriveListFilesExecutor", "Cannot find DriveId. Are you authorized to view this file?");
				return;
			}
			DriveFolder folder = Drive.DriveApi.getFolder(getGoogleApiClient(),	result.getDriveId());
			folder.listChildren(getGoogleApiClient()).setResultCallback(metadataResult);
		}
	};
	
	final private ResultCallback<MetadataBufferResult> metadataResult = new ResultCallback<MetadataBufferResult>() {
		@Override
		public void onResult(MetadataBufferResult result) {
			if (!result.getStatus().isSuccess()) {
				Log.e("GDriveListFilesExecutor", "Problem while retrieving files");
				return;
			}
			Log.i("GDriveListFilesExecutor", "Successfully listed files.");
		}
	};
}
