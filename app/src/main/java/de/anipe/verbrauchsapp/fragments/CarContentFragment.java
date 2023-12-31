package de.anipe.verbrauchsapp.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

import de.anipe.verbrauchsapp.CarInputActivity;
import de.anipe.verbrauchsapp.ConsumptionInputActivity;
import de.anipe.verbrauchsapp.ConsumptionListActivity;
import de.anipe.verbrauchsapp.GDriveStoreActivity;
import de.anipe.verbrauchsapp.GraphViewPlot;
import de.anipe.verbrauchsapp.ImportActivity;
import de.anipe.verbrauchsapp.MainActivity;
import de.anipe.verbrauchsapp.PictureImportActivity;
import de.anipe.verbrauchsapp.R;
import de.anipe.verbrauchsapp.db.ConsumptionDataSource;
import de.anipe.verbrauchsapp.io.FileSystemAccessor;
import de.anipe.verbrauchsapp.io.XMLHandler;
import de.anipe.verbrauchsapp.objects.Car;

/**
 *
 */
public class CarContentFragment extends Fragment {

    private FileSystemAccessor accessor;
    private ConsumptionDataSource dataSource;
    private long carId;
    private View rootView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        accessor = FileSystemAccessor.getInstance();
        dataSource = ConsumptionDataSource.getInstance(getActivity());

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        carId = getArguments().getLong("carid");

        // The last two arguments ensure LayoutParams are inflated properly.
        rootView = inflater.inflate(R.layout.activity_car_content, container, false);

        FloatingActionButton btn = rootView.findViewById(R.id.float_add);
        btn.setOnClickListener(clickListener);

        return rootView;
    }

    @Override
    public void onResume() {


        updateView();

        super.onResume();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.car_menubar, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_edit_entry:
                createCarEditActivity();
                return true;
            case R.id.action_add_picture:
                createAddPictureActivity();
                return true;
            case R.id.action_view_consumptions:
                createViewConsumptionsActivity();
                return true;
            case R.id.action_view_chart:
                createConsumptionPlotActivity();
                return true;
            case R.id.action_remove_entry:
                removeEntry();
                return true;
            case R.id.action_import_data:
                createImportDataActivity();
                return true;
            case R.id.action_export_data:
                // exportData();
                storeDriveData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Create an anonymous implementation of OnClickListener
    private OnClickListener clickListener = v -> {
        switch (v.getId()) {
            case R.id.float_add:
                createAddConsumptionActivity();
                break;
        }
    };

    private void createCarEditActivity() {
        Intent intent = new Intent(getActivity(),
            CarInputActivity.class);
        intent.putExtra("carid", carId);
        intent.putExtra("update", true);
        getActivity().startActivity(intent);
    }

    private void createAddConsumptionActivity() {
        Intent intent = new Intent(getActivity(),
            ConsumptionInputActivity.class);
        intent.putExtra("carid", carId);
        intent.putExtra("kmstate", dataSource.getMileageForCar(carId));
        getActivity().startActivity(intent);
    }

    private void createAddPictureActivity() {
        Intent intent = new Intent(getActivity(),
            PictureImportActivity.class);
        intent.putExtra("carid", carId);
        getActivity().startActivity(intent);
    }

    private void createImportDataActivity() {
        Intent intent = new Intent(getActivity(),
            ImportActivity.class);
        intent.putExtra("carid", carId);
        getActivity().startActivity(intent);
    }

    private void removeEntry() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
            getActivity());

        // set title
        alertDialogBuilder.setTitle("Eintrag löschen?");
        TypedValue typedValue = new TypedValue();
        getActivity().getTheme().resolveAttribute(android.R.attr.alertDialogIcon, typedValue, true);
        alertDialogBuilder.setIcon(typedValue.resourceId);

        // set dialog message
        alertDialogBuilder
            .setMessage(
                "Mit 'Ja' wird der Fahrzeugdatensatz und alle dazugehörigen Verbrauchsdatensätze gelöscht!")
            .setCancelable(false)
            .setPositiveButton("Ja", (dialog, id) -> {
                dataSource.deleteConsumptionsForCar(carId);
                dataSource.deleteCar(dataSource.getCarForId(carId));

                getActivity().finish();
            })
            .setNegativeButton("Nein",
                (dialog, id) -> dialog.cancel());

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private void createViewConsumptionsActivity() {
        Intent intent = new Intent(getActivity(),
            ConsumptionListActivity.class);
        intent.putExtra("carid", carId);
        getActivity().startActivity(intent);
    }

    private void createConsumptionPlotActivity() {
//		Intent intent = new Intent(CarContentActivity.this, PlotActivity.class);
//		Intent intent = new Intent(CarContentActivity.this, XYPlot.class);
        Intent intent = new Intent(getActivity(), GraphViewPlot.class);
        intent.putExtra("carid", carId);
        getActivity().startActivity(intent);
    }

    private void updateView() {
        Car car = dataSource.getCarForId(carId);

        TextView header = rootView.findViewById(R.id.fullscreen_car_content);
        header.setText(car.getBrand().value() + " " + car.getType());

        TextView startkm = rootView.findViewById(R.id.startkmValueLine);
        startkm.setText(String.valueOf(car.getStartKm()));

        TextView actualkm = rootView.findViewById(R.id.actualkmValueLine);
        int mileage = dataSource.getMileageForCar(carId);
        actualkm.setText(String.valueOf(mileage));

        TextView consumption = rootView.findViewById(R.id.consumptionValueLine);
        consumption.setText(new DecimalFormat("#.00").format(dataSource
            .getOverallConsumptionForCar(carId)) + " l/100km");

        TextView overallKm = rootView.findViewById(R.id.drivenKmValueLine);
        overallKm.setText(String.valueOf(mileage - car.getStartKm()));

        TextView overallCosts = rootView.findViewById(R.id.overallCostsValueLine);
        overallCosts.setText(new DecimalFormat("#0.00").format(dataSource
            .getOverallCostsForCar(carId)) + " \u20ac");

        if (car.getImage() != null) {
            ImageView image = rootView.findViewById(R.id.pictureLine);
            image.setImageBitmap(car.getImage());
        }
    }

    @SuppressWarnings("unused")
    private void exportData() {
        Car car = dataSource.getCarForId(carId);
        XMLHandler handler = new XMLHandler(null, car,
            dataSource.getOverallConsumptionForCar(carId),
            dataSource.getConsumptionCycles(carId));
        try {
            accessor.writeXMLFileToStorage(getActivity(),
                handler.createConsumptionDocument(),
                MainActivity.STORAGE_DIR,
                car.getBrand() + "_" + car.getType());
            Toast.makeText(getActivity(),
                "Daten erfolgreich in XML-Datei exportiert!",
                Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(
                getActivity(),
                "Fehler beim Schreiben der XML-Datei. Grund: "
                    + e.getLocalizedMessage(), Toast.LENGTH_LONG)
                .show();
        }
    }

    private void storeDriveData() {
        Intent intent = new Intent(getActivity(),
            GDriveStoreActivity.class);
        intent.putExtra("carid", carId);
        getActivity().startActivity(intent);
    }
}
