package de.anipe.verbrauchsapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveFolder.DriveFileResult;
import com.google.android.gms.drive.DriveResource.MetadataResult;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;

import de.anipe.verbrauchsapp.db.ConsumptionDataSource;
import de.anipe.verbrauchsapp.io.GDriveAsyncTask;
import de.anipe.verbrauchsapp.io.FileSystemAccessor;
import de.anipe.verbrauchsapp.io.XMLHandler;
import de.anipe.verbrauchsapp.objects.Car;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class GDriveStoreActivity extends GDriveBaseActivity {

	private File outputFile;
	private long carId;

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

		accessor = FileSystemAccessor.getInstance();
		dataSource = ConsumptionDataSource.getInstance(this);
		try {
			dataSource.open();
		} catch (SQLException e) {
			Toast.makeText(GDriveStoreActivity.this,
					"Fehler beim Öffnen der Datenbank!", Toast.LENGTH_LONG)
					.show();
		}
		
		// Step 1: write to local XML file
		writeXMLFileToLocalFileSystem();
		
		if (outputFile == null) {
			Log.e("GDriveStoreActivity", "Output file is null. Nothing to write to Google Drive. Aborting Activity.");
			finish();
		}
	}

//	@Override
//	protected void onResume() {
//		super.onResume();
//
//		// Step 1: write to local XML file
//		writeXMLFileToLocalFileSystem();
//
//		// Step 2: upload to the cloud
//		if (outputFile != null) {
//			if (getGoogleApiClient() == null) {
//				mGoogleApiClient = new GoogleApiClient.Builder(this)
//						.addApi(Drive.API).addScope(Drive.SCOPE_FILE)
//						.addScope(Drive.SCOPE_APPFOLDER)
//						.addConnectionCallbacks(this)
//						.addOnConnectionFailedListener(this).build();
//			}
//			mGoogleApiClient.connect();
//		} else {
//			finish();
//		}
//	}

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
			xmlTv.setBackgroundColor(Color.GREEN);
			xmlTv.setTextColor(Color.BLACK);
			xmlTv.setText("OK");
		} catch (Exception e) {
			Toast.makeText(
					GDriveStoreActivity.this,
					"Fehler beim Schreiben der XML-Datei. Grund: "
							+ e.getLocalizedMessage(), Toast.LENGTH_LONG)
					.show();
			TextView xmlTv = (TextView) findViewById(R.id.xmlExportValueLine);
			xmlTv.setBackgroundColor(Color.RED);
			xmlTv.setTextColor(Color.BLACK);
			xmlTv.setText("FEHLER");
		}
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		Log.i("GDriveStoreActivity", "GoogleApiClient connected");

		try {
			new CreateFileAsyncTask(this).execute();
		} catch (Exception e) {
			Toast.makeText(
					GDriveStoreActivity.this,
					"Fehler beim Upload der XML-Datei. Grund: "
							+ e.getLocalizedMessage(), Toast.LENGTH_LONG)
					.show();
			TextView xmlTv = (TextView) findViewById(R.id.cloudExportValueLine);
			xmlTv.setBackgroundColor(Color.RED);
			xmlTv.setTextColor(Color.BLACK);
			xmlTv.setText("FEHLER");
		}
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
		cloudTv.setBackgroundColor(Color.RED);
		cloudTv.setTextColor(Color.BLACK);
		cloudTv.setText("FEHLER");

		showMessage("Verbindungsaufbau zu Google Drive fehlgeschlagen!");
		finish();
	}

	private void showMessage(String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}

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

	public class CreateFileAsyncTask extends
			GDriveAsyncTask<Void, Void, Metadata> {

		public CreateFileAsyncTask(Context context) {
			super(context);
		}

		@Override
		protected Metadata doInBackgroundConnected(Void... arg0) {

			// First we start by creating a new contents, and blocking on the
			// result by calling await().
			DriveContentsResult contentsResult = Drive.DriveApi.newDriveContents(
					getGoogleApiClient()).await();
			if (!contentsResult.getStatus().isSuccess()) {
				Log.e("GDriveStoreActivity", "Creating Drive content: failed!");
				// We failed, stop the task and return.
				return null;
			}

			// Read the contents and open its output stream for writing, then
			// write a short message.
			DriveContents originalContents = contentsResult.getDriveContents();
			OutputStream os = originalContents.getOutputStream();
			try {
				os.write(read(outputFile));
			} catch (IOException e) {
				Log.e("GDriveStoreActivity", "Exception while writing content to output stream: " + e.getLocalizedMessage());
				e.printStackTrace();
				return null;
			}

			// Create the metadata for the new file including title and MIME
			// type.
			MetadataChangeSet originalMetadata = new MetadataChangeSet.Builder()
					.setTitle(outputFile.getName()).setMimeType("text/xml")
					.build();

			// Create the file in the root folder, again calling await() to
			// block until the request finishes.
			DriveFolder rootFolder = Drive.DriveApi
					.getRootFolder(getGoogleApiClient());
			DriveFileResult fileResult = rootFolder.createFile(
					getGoogleApiClient(), originalMetadata, originalContents)
					.await();
			if (!fileResult.getStatus().isSuccess()) {
				// We failed, stop the task and return.
				Log.e("GDriveStoreActivity", "Creating Drive content failed!");
				return null;
			}

			// Finally, fetch the metadata for the newly created file, again
			// calling await to block until the request finishes.
			MetadataResult metadataResult = fileResult.getDriveFile()
					.getMetadata(getGoogleApiClient()).await();
			if (!metadataResult.getStatus().isSuccess()) {
				// We failed, stop the task and return.
				return null;
			}
			// We succeeded, return the newly created metadata.
			return metadataResult.getMetadata();
		}

		@Override
		protected void onPostExecute(Metadata result) {
			super.onPostExecute(result);
			if (result == null) {
				// The creation failed somehow, so show a message.
				// showMessage("Error while creating the file.");

				TextView xmlTv = (TextView) findViewById(R.id.cloudExportValueLine);
				xmlTv.setTextColor(Color.RED);
				xmlTv.setText("FEHLER");

				return;
			}
			// The creation succeeded, show a message.
			// showMessage("File created: " + result.getDriveId());

			Log.i("GDriveStoreActivity", "Writing to Drive ok. Created content size: " + result.getFileSize());
			
			TextView xmlTv = (TextView) findViewById(R.id.cloudExportValueLine);
			xmlTv.setBackgroundColor(Color.GREEN);
			xmlTv.setTextColor(Color.BLACK);
			xmlTv.setText("OK");
		}
	}
}
