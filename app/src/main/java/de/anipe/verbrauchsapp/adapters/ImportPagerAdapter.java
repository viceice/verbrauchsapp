package de.anipe.verbrauchsapp.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;

import de.anipe.verbrauchsapp.R;
import de.anipe.verbrauchsapp.fragments.GDriveImportFragment;
import de.anipe.verbrauchsapp.fragments.LocalImportFragment;

public class ImportPagerAdapter extends FragmentStatePagerAdapter {

    private final LocalImportFragment localImportFragment;
    private final GDriveImportFragment gDriveImportFragment;
    private final Context context;

    public ImportPagerAdapter(FragmentActivity ctx) {
        super(ctx.getSupportFragmentManager());
        context = ctx;
        localImportFragment = new LocalImportFragment();
        gDriveImportFragment = new GDriveImportFragment();
    }

    @Override
    public Fragment getItem(int i) {

        if (i == 0)
            return localImportFragment;
        else
            return gDriveImportFragment;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return context.getString(R.string.actionbar_local);
        } else {
            return context.getString(R.string.actionbar_remote);
        }
    }
}
