package de.anipe.verbrauchsapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.ContentsResult;
import com.google.android.gms.drive.DriveApi.DriveIdResult;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder.DriveFileResult;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;

import de.anipe.verbrauchsapp.db.ConsumptionDataSource;
import de.anipe.verbrauchsapp.io.ApiClientAsyncTask;
import de.anipe.verbrauchsapp.io.FileSystemAccessor;
import de.anipe.verbrauchsapp.io.XMLHandler;
import de.anipe.verbrauchsapp.objects.Car;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class GDriveStoreActivity extends Activity implements
		ConnectionCallbacks, OnConnectionFailedListener {

	private File outputFile;
	private DriveId fileId;
	private long carId;

	private static GoogleApiClient mGoogleApiClient;

	private FileSystemAccessor accessor;
	private ConsumptionDataSource dataSource;

	private static final int REQUEST_CODE_CREATOR = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		carId = bundle.getLong("carid");

		setContentView(R.layout.activity_gdrive_upload);
				
		mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Drive.API)
				.addScope(Drive.SCOPE_FILE).addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).build();

		accessor = FileSystemAccessor.getInstance();
		dataSource = ConsumptionDataSource.getInstance(this);
		try {
			dataSource.open();
		} catch (SQLException e) {
			Toast.makeText(GDriveStoreActivity.this,
					"Fehler beim Öffnen der Datenbank!", Toast.LENGTH_LONG)
					.show();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Step 1: write to local XML file
		writeXMLFileToLocalFileSystem();
						
		// Step 2: upload to the cloud
		if (outputFile != null) {
			if (mGoogleApiClient == null) {
				mGoogleApiClient = new GoogleApiClient.Builder(this)
						.addApi(Drive.API).addScope(Drive.SCOPE_FILE)
						.addScope(Drive.SCOPE_APPFOLDER)
						.addConnectionCallbacks(this)
						.addOnConnectionFailedListener(this).build();
			}
			mGoogleApiClient.connect();
		} else {
			finish();
		}
	}
	
	private void writeXMLFileToLocalFileSystem() {
		Car car = dataSource.getCarForId(carId);

		XMLHandler handler = new XMLHandler(null, car,
				dataSource.getOverallConsumptionForCar(carId),
				dataSource.getConsumptionCycles(carId));		
		try {
			outputFile = accessor.writeXMLFileToStorage(this,
					handler.createConsumptionDocument(),
					MainActivity.STORAGE_DIR,
					car.getBrand() + "_" + car.getType());
			TextView xmlTv = (TextView) findViewById(R.id.xmlExportValueLine);
			xmlTv.setTextColor(Color.GREEN);
			xmlTv.setText("OK");
		} catch (Exception e) {
			Toast.makeText(
					GDriveStoreActivity.this,
					"Fehler beim Schreiben der XML-Datei. Grund: "
							+ e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
			TextView xmlTv = (TextView) findViewById(R.id.xmlExportValueLine);
			xmlTv.setTextColor(Color.RED);
			xmlTv.setText("FEHLER");
		}
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		Log.i("GDriveStoreActivity", "GoogleApiClient connected");
		Drive.DriveApi.newContents(mGoogleApiClient).setResultCallback(contentsCallback);

		
		
		
		Drive.DriveApi.fetchDriveId(mGoogleApiClient, fileId.encodeToString()).setResultCallback(idCallback);

	}

	@Override
	public void onConnectionSuspended(int result) {
		Log.i("GDriveStoreActivity", "GoogleApiClient connection suspended");
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.e("GDriveStoreActivity", "GoogleApiClient connection failed: "
				+ result.toString());
		if (!result.hasResolution()) {
			GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this,
					0).show();
			return;
		}
		try {
			result.startResolutionForResult(this, REQUEST_CODE_CREATOR);
		} catch (SendIntentException e) {
			Log.e("GDriveStoreActivity",
					"Exception while starting resolution activity", e);
		}
		
		TextView cloudTv = (TextView) findViewById(R.id.cloudExportValueLine);
		cloudTv.setTextColor(Color.RED);
		cloudTv.setText("FEHLER");
		
		showMessage("Export zu Google Drive fehlgeschlagen!");
		finish();
	}

	private void showMessage(String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}

	private final ResultCallback<DriveIdResult> idCallback = new ResultCallback<DriveIdResult>() {
		@Override
		public void onResult(DriveIdResult result) {
			if (!result.getStatus().isSuccess()) {
				Log.e("GDriveStoreActivity", "Cannot find DriveId. Are you authorized to view this file?");
				return;
			}
			
			Log.i("GDriveStoreActivity", "Start uploading file...");
			
			fileId = result.getDriveId();
			
			DriveFile file = Drive.DriveApi.getFile(mGoogleApiClient, fileId);
			new EditContentsAsyncTask(GDriveStoreActivity.this).execute(file);
			
			TextView cloudTv = (TextView) findViewById(R.id.cloudExportValueLine);
			cloudTv.setTextColor(Color.GREEN);
			cloudTv.setText("OK");
		}
	};
	
	private final ResultCallback<ContentsResult> contentsCallback = new ResultCallback<ContentsResult>() {
		@Override
		public void onResult(ContentsResult result) {
			if (!result.getStatus().isSuccess()) {
				Log.e("GDriveStoreActivity", "Error while trying to create new file contents");
				return;
			}

			MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
					.setTitle(outputFile.getName()).setMimeType("text/xml")
					.setStarred(true).build();
			// create a file on root folder
			Drive.DriveApi
					.getRootFolder(mGoogleApiClient)
					.createFile(mGoogleApiClient, changeSet,
							result.getContents())
					.setResultCallback(fileCallback);
		}
	};

	private final ResultCallback<DriveFileResult> fileCallback = new ResultCallback<DriveFileResult>() {
		@Override
		public void onResult(DriveFileResult result) {
			if (!result.getStatus().isSuccess()) {
				Log.e("GDriveStoreActivity", "Error while trying to create the file");
				return;
			}
			fileId = result.getDriveFile().getDriveId();
		}
	};

	private byte[] read(File file) throws IOException {

		byte[] buffer = new byte[(int) file.length()];
		InputStream ios = null;
		try {
			ios = new FileInputStream(file);
			if (ios.read(buffer) == -1) {
				throw new IOException(
						"EOF reached while trying to read the whole file");
			}
		} finally {
			try {
				if (ios != null)
					ios.close();
			} catch (IOException e) {
			}
		}

		return buffer;
	}

	public class EditContentsAsyncTask extends
			ApiClientAsyncTask<DriveFile, Void, Boolean> {

		public EditContentsAsyncTask(Context context) {
			super(context);
		}

		@Override
		protected Boolean doInBackgroundConnected(DriveFile... args) {
			DriveFile file = args[0];
			try {
				ContentsResult contentsResult = file.openContents(
						mGoogleApiClient, DriveFile.MODE_WRITE_ONLY, null)
						.await();
				if (!contentsResult.getStatus().isSuccess()) {
					return false;
				}
				OutputStream outputStream = contentsResult.getContents().getOutputStream();
				
				System.out.println("A");
				
				outputStream.write(read(outputFile));
				com.google.android.gms.common.api.Status status = file
						.commitAndCloseContents(mGoogleApiClient,
								contentsResult.getContents()).await();
				
				System.out.println("A");
				
				
				return status.getStatus().isSuccess();
			} catch (IOException e) {
				Log.e("GDriveStoreActivity",
						"Error while writing file to cloud");
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (!result) {
				Log.e("GDriveStoreActivity", "Error while editing contents");
				return;
			}
			Log.i("GDriveStoreActivity", "Successfully edited contents");
		}
	}
}
