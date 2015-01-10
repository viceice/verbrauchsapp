package de.anipe.verbrauchsapp;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.anipe.verbrauchsapp.io.FileSystemAccessor;
import de.anipe.verbrauchsapp.io.XMLHandler;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.app.ActionBar.Tab;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class TabbedImportActivity extends FragmentActivity {
	
	ImportPagerAdapter pagerAdapter;
    ViewPager viewPager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_collection_import);
		
		// ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        pagerAdapter = new ImportPagerAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(pagerAdapter);
        
        final ActionBar actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(false);
		// Specify that tabs should be displayed in the action bar.
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		ActionBar.TabListener tabListener = new ActionBar.TabListener() {
			
			@Override
			public void onTabUnselected(Tab tab, FragmentTransaction ft) {}
			
			@Override
			public void onTabSelected(Tab tab, FragmentTransaction ft) {
				viewPager.setCurrentItem(tab.getPosition());
			}
			
			@Override
			public void onTabReselected(Tab tab, FragmentTransaction ft) {}
		};
		
		actionBar.addTab(actionBar.newTab().setText(R.string.actionbar_local)
				.setTabListener(tabListener));
		actionBar.addTab(actionBar.newTab().setText(R.string.actionbar_remote)
				.setTabListener(tabListener));
		
		/**
         * on swiping the viewpager make respective tab selected
         * */
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
 
            @Override
            public void onPageSelected(int position) {
                // on changing the page
                // make respected tab selected
                actionBar.setSelectedNavigationItem(position);
            }
 
            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }
 
            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });

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
						MyAsyncTask task = new MyAsyncTask();
						task.item = item;
						task.execute();
					}
				});
			}
			
			return rootView;
		}
		
		class MyAsyncTask extends AsyncTask<Void, Void, Void> {
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

