package de.anipe.verbrauchsapp;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.IntentSender.SendIntentException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveIdResult;
import com.google.android.gms.drive.DriveApi.MetadataBufferResult;
import com.google.android.gms.drive.DriveFolder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.anipe.verbrauchsapp.io.FileSystemAccessor;
import de.anipe.verbrauchsapp.io.XMLHandler;
import de.anipe.verbrauchsapp.tasks.UpdateTask;

public class TabbedImportActivity extends FragmentActivity implements ConnectionCallbacks, OnConnectionFailedListener {
	
	private GoogleApiClient mGoogleApiClient;
	private ImportPagerAdapter pagerAdapter;
    private ViewPager viewPager;
    
    private static String[] gDriveFiles;
    private Menu menu;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_collection_import);
		
		// ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        pagerAdapter = new ImportPagerAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(pagerAdapter);

        getActionBar().setDisplayHomeAsUpEnabled(true);
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.car_import, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_refresh:
                ImageView iv = (ImageView)getLayoutInflater().inflate(R.layout.iv_refresh, null);
                Animation rotation = AnimationUtils.loadAnimation(this, R.anim.rotate_refresh);
                rotation.setRepeatCount(Animation.INFINITE);
                iv.startAnimation(rotation);
                item.setActionView(iv);
                new UpdateTask(this).execute();
                return true;
        }
        return super.onOptionsItemSelected(item);
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
		Log.i("TabbedImportActivity", "GoogleApiClient connected");

		DriveFolder folder = Drive.DriveApi.getFolder(mGoogleApiClient, Drive.DriveApi.getRootFolder(mGoogleApiClient).getDriveId());		
		folder.listChildren(mGoogleApiClient).setResultCallback(childrenRetrievedCallback);
	}
	
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.e("TabbedImportActivity", "GoogleApiClient connection failed: " + result.toString());
		if (!result.hasResolution()) {
			// show the localized error dialog.
			GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this,
					0).show();
			return;
		}
		try {
			result.startResolutionForResult(this, 1);
		} catch (SendIntentException e) {
			Log.e("TabbedImportActivity", "Exception while starting resolution activity", e);
		}
	}
	
	@Override
	public void onConnectionSuspended(int cause) {
		Log.i("TabbedImportActivity", "GoogleApiClient connection suspended");
	}
	
	final private ResultCallback<MetadataBufferResult> childrenRetrievedCallback = new ResultCallback<MetadataBufferResult>() {
        @Override
        public void onResult(MetadataBufferResult result) {
            if (!result.getStatus().isSuccess()) {
            	Log.e("TabbedImportActivity", "Problem while retrieving files");
                return;
            }
            
            Log.i("TabbedImportActivity", "Listing files...");
            
            List<String> tempList = new LinkedList<String>();
			for(int i = 0; i < result.getMetadataBuffer().getCount(); i++) {
				if (!result.getMetadataBuffer().get(i).isTrashed() && !(result.getMetadataBuffer().get(i).getTitle() == null)) {
					tempList.add(result.getMetadataBuffer().get(i).getTitle());
				}
			}
			gDriveFiles = new String[tempList.size()];
			for (int j = 0; j < tempList.size(); j++) {
				gDriveFiles[j] = tempList.get(j);
			}
			Arrays.sort(gDriveFiles);
			
			result.getMetadataBuffer().release();
        }
    };
	
	@SuppressWarnings("unused")
	final private ResultCallback<DriveIdResult> idCallback = new ResultCallback<DriveIdResult>() {
		@Override
		public void onResult(DriveIdResult result) {
			if (!result.getStatus().isSuccess()) {
				Log.e("TabbedImportActivity", "Cannot find DriveId. Are you authorized to view this file?");
				return;
			}
			DriveFolder folder = Drive.DriveApi.getFolder(mGoogleApiClient,	result.getDriveId());
			folder.listChildren(mGoogleApiClient).setResultCallback(metadataResult);
		}
	};
	
	final private ResultCallback<MetadataBufferResult> metadataResult = new ResultCallback<MetadataBufferResult>() {
		@Override
		public void onResult(MetadataBufferResult result) {
			if (!result.getStatus().isSuccess()) {
				Log.e("TabbedImportActivity", "Problem while retrieving files");
				return;
			}
			Log.i("TabbedImportActivity", "Successfully listed files.");
			
			gDriveFiles = new String[result.getMetadataBuffer().getCount()];
			for(int i = 0; i < result.getMetadataBuffer().getCount(); i++) {
				gDriveFiles[i] = result.getMetadataBuffer().get(i).getTitle();
			}
			
		}
	};

    public void resetUpdating() {
        // Get our refresh item from the menu
        MenuItem m = menu.findItem(R.id.action_refresh);
        if(m.getActionView()!=null)
        {
            // Remove the animation.
            m.getActionView().clearAnimation();
            m.setActionView(null);
        }
    }

    // Since this is an object collection, use a FragmentStatePagerAdapter,
	// and NOT a FragmentPagerAdapter.
	public class ImportPagerAdapter extends FragmentStatePagerAdapter {
		
	    public ImportPagerAdapter(FragmentManager fm) {
	        super(fm);
	    }

	    @Override
	    public Fragment getItem(int i) {
	    	Fragment fragment = new ImportObjectFragment();
	    	Bundle args = new Bundle();
            args.putInt("tab", i);
            fragment.setArguments(args);
            return fragment;
	    }

	    @Override
	    public int getCount() {
	        return 2;
	    }

	    @Override
	    public CharSequence getPageTitle(int position) {
	        if (position == 0) {
	        	return getString(R.string.actionbar_local);
	        } else {
	        	return getString(R.string.actionbar_remote);
	        }
	    }
	}

	
	// Instances of this class are fragments representing a single
	// object in our collection.
	public static class ImportObjectFragment extends Fragment {
		
		private FileSystemAccessor accessor;
		private XMLHandler xmlImporter;
		private Map<String, File> fileMapping;
		private int local = 0;
		
		@SuppressLint("DefaultLocale")
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			// The last two arguments ensure LayoutParams are inflated properly.
			ListView rootView = (ListView)inflater.inflate(R.layout.csvimport_layout, container, false);
			
			Bundle args = getArguments();
			local = args.getInt("tab");
			
			if (local == 0) {
				ArrayList<String> filesList = new ArrayList<String>();
				fileMapping = new HashMap<String, File>();

				accessor = FileSystemAccessor.getInstance();
				File[] files = accessor.readFilesFromStorageDir(accessor
						.createOrGetStorageDir(MainActivity.STORAGE_DIR));
			
				if (files != null && files.length > 0) {
					for (File f : files) {
						String name = f.getName();
						if (name.toLowerCase().endsWith(".xml")) {
							filesList.add(f.getName());
							fileMapping.put(f.getName(), f);
						}
					}
				} else {
					Toast.makeText(getActivity(), "Zielordner existiert nicht oder ist leer!",
							Toast.LENGTH_LONG).show();
				}

				final ListAdapter adapter = new ArrayAdapter<String>(getActivity(),
						android.R.layout.simple_list_item_1, filesList);
				
				rootView.setAdapter(adapter);
				
				rootView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						
						String item = (String) adapter.getItem(position);
						ExportLocalAsyncTask task = new ExportLocalAsyncTask();
						task.item = item;
						task.execute();
					}
				});
			} else if (local == 1) {

				if (gDriveFiles == null) {
					gDriveFiles = new String[] {"aaa", "bbb", "ccc"};
				}
				
				Log.i("TabbedImportActivity", "Array size is " + gDriveFiles.length);
				
				for (int i = 0; i < gDriveFiles.length; i++) {
					Log.i("TabbedImportActivity", "Element at " + i + " is " + gDriveFiles[i]);
				}
				
				final ListAdapter adapter = new ArrayAdapter<String>(getActivity(),
						android.R.layout.simple_list_item_1, gDriveFiles);			// gDriveList
				
				rootView.setAdapter(adapter);
				
				rootView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						
						// TODO: Read from Google Drive file
					}
				});
				
			} 
			
			return rootView;
		}
		
		class ExportLocalAsyncTask extends AsyncTask<Void, Void, Void> {
			ProgressDialog myprogsdial;
			int dataSets = 0;
			String item;

			@Override
			protected void onPreExecute() {
				myprogsdial = ProgressDialog.show(getActivity(),
						"Datensatz-Import", "Bitte warten ...", true);
			}

			@Override
			protected Void doInBackground(Void... params) {
				xmlImporter = new XMLHandler(getActivity());
				long carId = xmlImporter.importXMLCarData(fileMapping.get(item));
				dataSets = xmlImporter.importXMLConsumptionDataForCar(carId,
						fileMapping.get(item));

				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				myprogsdial.dismiss();
				Toast.makeText(
							getActivity(),
							"Fahrzeug mit " + dataSets + " Datensätzen importiert.",
							Toast.LENGTH_LONG).show();
			}
		}
	}
}

