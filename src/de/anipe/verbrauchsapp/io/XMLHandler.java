package de.anipe.verbrauchsapp.io;

import java.io.File;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import android.content.Context;
import android.util.Log;

import de.anipe.verbrauchsapp.db.ConsumptionDataSource;
import de.anipe.verbrauchsapp.objects.Brand;
import de.anipe.verbrauchsapp.objects.Car;
import de.anipe.verbrauchsapp.objects.Consumption;
import de.anipe.verbrauchsapp.objects.Fueltype;

public class XMLHandler {

	private Car car;
	private double cons;
	private List<Consumption> consumptions;

	private static ConsumptionDataSource dataSource;

	private SimpleDateFormat dateFormat = new SimpleDateFormat(
			"dd.MM.yyyy-HH.mm.ss", Locale.getDefault());
	private SimpleDateFormat shortDateFormat = new SimpleDateFormat(
			"dd.MM.yyyy", Locale.getDefault());

	private static final String ROOT_ELEMENT_NAME = "ConsumptionData";
	private static final String CAR_ELEMENT_NAME = "Car";
	private static final String CAR_ID_NAME = "InAppCarID";
	private static final String EXPORT_DATE = "ExportDatum";
	private static final String MANUFACTURER = "Hersteller";
	private static final String TYPE = "Typ";
	private static final String NUMBERPLATE = "Kennzeichen";
	private static final String START_KILOMETER = "Startkilometer";
	private static final String FUELTYPE = "Kraftstoff";
	private static final String OVERALL_CONSUMPTION = "Durchschnittsverbrauch";

	private static final String CONSUMPTIONS_ROOT_NAME = "Consumptions";
	private static final String CONSUMPTION_ELEMENT_NAME = "Consumption";
	private static final String DATE_ELEMENT_NAME = "Datum";
	private static final String KILOMETER_STATE_NAME = "Kilometerstand";
	private static final String DRIVEN_KILOMETER_NAME = "GefahreneKilometer";
	private static final String REFUEL_LITER_NAME = "LiterGetankt";
	private static final String LITER_PRICE_NAME = "PreisJeLiter";
	private static final String CONSUMPTION_NAME = "Verbrauch";

	public XMLHandler(Context context) {
		this(context, null, 0, null);
	}

	public XMLHandler(Context context, Car car, double cons,
			List<Consumption> consumptions) {
		this.car = car;
		this.cons = cons;
		this.consumptions = consumptions;

		if (context != null) {
			dataSource = ConsumptionDataSource.getInstance(context);
			try {
				dataSource.open();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public Document importXMLCarData(File inputFile) {
		try {
			SAXBuilder builder = new SAXBuilder();
			Document doc = (Document) builder.build(inputFile);
			dataSource.addCar(parseCarFromDocument(doc));

			return doc;
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("XMLHandler", "Exception while parsing XML document");
		}
		return null;
	}

	public Document importXMLConsumptionDataForCar(long carId, File inputFile) {
		// remove old entries
		dataSource.deleteConsumptionsForCar(carId);

		try {
			SAXBuilder builder = new SAXBuilder();
			Document doc = (Document) builder.build(inputFile);
			dataSource
					.addConsumptions(parseConsumptionFromDocument(doc, carId));

			return doc;
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("XMLHandler", "Exception while parsing XML document");
		}
		return null;
	}

	public Car parseCarFromDocument(Document doc) {

		Element rootElem = doc.getRootElement();
		Element carElement = rootElem.getChild(CAR_ELEMENT_NAME);

		Car car = new Car();
		car.setCarId(Long.parseLong(carElement.getAttributeValue(CAR_ID_NAME)));
		car.setBrand(Brand.fromValue(carElement.getChildText(MANUFACTURER)));
		car.setType(carElement.getChildText(TYPE));
		car.setNumberPlate(carElement.getChildText(NUMBERPLATE));
		car.setStartKm(Integer.parseInt(carElement
				.getChildText(START_KILOMETER)));
		car.setFuelType(Fueltype.fromValue(carElement.getChildText(FUELTYPE)));

		return car;
	}

	public List<Consumption> parseConsumptionFromDocument(Document doc,
			long carId) {
		List<Consumption> cons = new LinkedList<Consumption>();
		List<Element> conList = doc.getRootElement()
				.getChild(CONSUMPTIONS_ROOT_NAME)
				.getChildren(CONSUMPTION_ELEMENT_NAME);
		for (int i = 0; i < conList.size(); i++) {
			Element elem = conList.get(i);

			Consumption conElem = new Consumption();
			conElem.setCarId(carId);
			conElem.setDate(getDateFromString(elem
					.getChildText(DATE_ELEMENT_NAME)));
			conElem.setRefuelmileage(Integer.parseInt(elem
					.getChildText(KILOMETER_STATE_NAME)));
			conElem.setDrivenmileage(Integer.parseInt(elem
					.getChildText(DRIVEN_KILOMETER_NAME)));
			conElem.setRefuelliters(Double.parseDouble(elem
					.getChildText(REFUEL_LITER_NAME)));
			conElem.setRefuelprice(Double.parseDouble(elem
					.getChildText(LITER_PRICE_NAME)));
			conElem.setConsumption(Double.parseDouble(elem
					.getChildText(CONSUMPTION_NAME)));

			cons.add(conElem);
		}

		return cons;
	}

	public Document createConsumptionDocument() {
		Element root = new Element(ROOT_ELEMENT_NAME);
		Document doc = new Document(root);
		doc.getRootElement().addContent(getCarData(doc, car));
		doc.getRootElement().addContent(getConsumptionData(doc, consumptions));

		return doc;
	}

	private Element getCarData(Document doc, Car car) {
		Element carData = new Element(CAR_ELEMENT_NAME);
		carData.setAttribute(new Attribute(CAR_ID_NAME, String.valueOf(car
				.getCarId())));

		carData.addContent(new Element(EXPORT_DATE).setText(getDateTime(
				new Date(), false)));
		carData.addContent(new Element(MANUFACTURER).setText(car.getBrand()
				.value()));
		carData.addContent(new Element(TYPE).setText(car.getType()));
		carData.addContent(new Element(NUMBERPLATE).setText(car
				.getNumberPlate()));
		carData.addContent(new Element(START_KILOMETER).setText(String
				.valueOf(car.getStartKm())));
		carData.addContent(new Element(FUELTYPE).setText(String.valueOf(car
				.getFuelType().value())));
		carData.addContent(new Element(OVERALL_CONSUMPTION).setText(String
				.valueOf(cons)));

		return carData;
	}

	private Element getConsumptionData(Document doc,
			List<Consumption> consumptions) {
		Element consumptionData = new Element(CONSUMPTIONS_ROOT_NAME);
		for (Consumption con : consumptions) {
			Element consumptionNode = new Element(CONSUMPTION_ELEMENT_NAME);

			consumptionNode.addContent(new Element(DATE_ELEMENT_NAME)
					.setText(getDateTime(con.getDate(), true)));
			consumptionNode.addContent(new Element(KILOMETER_STATE_NAME)
					.setText(String.valueOf(con.getRefuelmileage())));
			consumptionNode.addContent(new Element(DRIVEN_KILOMETER_NAME)
					.setText(String.valueOf(con.getDrivenmileage())));
			consumptionNode.addContent(new Element(REFUEL_LITER_NAME)
					.setText(String.valueOf(con.getRefuelliters())));
			consumptionNode.addContent(new Element(LITER_PRICE_NAME)
					.setText(String.valueOf(con.getRefuelprice())));
			consumptionNode.addContent(new Element(CONSUMPTION_NAME)
					.setText(String.valueOf(con.getConsumption())));

			consumptionData.addContent(consumptionNode);
		}
		return consumptionData;
	}

	private String getDateTime(Date date, boolean shortFormat) {
		if (!shortFormat) {
			return dateFormat.format(date);
		}
		return shortDateFormat.format(date);
	}

	private Date getDateFromString(String dateString) {
		try {
			return shortDateFormat.parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
}
