package de.anipe.verbrauchsapp.io;

import java.io.File;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import de.anipe.verbrauchsapp.db.ConsumptionDataSource;
import de.anipe.verbrauchsapp.objects.Consumption;

public class CSVHandler {

	private static ConsumptionDataSource dataSource;
	private FileSystemAccessor accessor;

	public CSVHandler(Context context) {
		dataSource = ConsumptionDataSource.getInstance(context);
		try {
			dataSource.open();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		accessor = FileSystemAccessor.getInstance();
	}

	@SuppressLint("SimpleDateFormat")
	public int importCSVDataForCar(long carId, File inputFile) {

		// remove old entries
		dataSource.deleteConsumptionsForCar(carId);

		List<String> inList = getCsvContent(inputFile);
		List<Consumption> cycles = new LinkedList<>();

		for (String str : inList) {
			Consumption cycle = new Consumption();
			String[] in = str.split(";");

			if (in.length < 4) {
				continue;
			}

			Date d = null;
			try {
				d = new SimpleDateFormat("dd.MM.yyyy").parse(in[0]);
			} catch (ParseException e) {
				e.printStackTrace();
			}

			cycle.setCarId(carId);
			cycle.setDate(d);
			cycle.setRefuelmileage(Integer.parseInt(in[1]));

			cycle.setDrivenmileage(Integer.parseInt(in[2]));
			cycle.setRefuelliters(Double.parseDouble(in[3].replace(",", ".")));
			double consumption = (cycle.getRefuelliters() * 100)
					/ cycle.getDrivenmileage();
			cycle.setConsumption(consumption);

			if (in.length >= 6) {
				cycle.setRefuelprice(in[5].equals("") ? 0 : Double
						.parseDouble(in[5].replace(",", ".")));
			}

			cycles.add(cycle);
		}
		dataSource.addConsumptions(cycles);
		
		return cycles.size();
	}

	private List<String> getCsvContent(File file) {
		return accessor.readCSVFileFromStorage(file);
	}
}
