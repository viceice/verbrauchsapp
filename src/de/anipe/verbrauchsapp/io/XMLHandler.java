package de.anipe.verbrauchsapp.io;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.anipe.verbrauchsapp.objects.Car;
import de.anipe.verbrauchsapp.objects.Consumption;

public class XMLHandler {

	private Car car;
	private double cons;
	private List<Consumption> consumptions;
	private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy-HH.mm.ss", Locale.getDefault());
	private SimpleDateFormat shortDateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
	
	public XMLHandler(Car car, double cons, List<Consumption> consumptions) {
		this.car = car;
		this.cons = cons;
		this.consumptions = consumptions;
	}

	public List<Consumption> parseConsumptionDocument(Document doc) {
		// TODO create functionality
		
		// XXX Do we need this?
		Node header = doc.getElementsByTagName("Car").item(0);
		long carId = Long.parseLong(((Element) header).getElementsByTagName("InAppCarID").item(0).getTextContent());  
		
		List<Consumption> cons = new LinkedList<Consumption>();
		NodeList nList = doc.getElementsByTagName("Consumption");
		
		
		
		
		
		
		return cons;
	}
	
	public Document createConsumptionDocument() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder parser = null;
		try {
			parser = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
        Document doc=parser.newDocument();
        Element root = doc.createElement("ConsumptionData");
        
        root.appendChild(getCarData(doc, car));
        root.appendChild(getConsumptionData(doc, consumptions));
        
        doc.appendChild(root);

        return doc;
	}
	
	private Element getCarData(Document doc, Car car) {
		Element carData = doc.createElement("Car");
		carData.setAttribute("InAppCarID", String.valueOf(car.getCarId()));
		
		Element expDateNode = doc.createElement("ExportDatum");
		expDateNode.appendChild(doc.createTextNode(getDateTime(new Date(), false)));
		carData.appendChild(expDateNode);
		
		Element brandNode = doc.createElement("Hersteller");
		brandNode.appendChild(doc.createTextNode(car.getBrand().value()));
		carData.appendChild(brandNode);
		
		Element typeNode = doc.createElement("Typ");
		typeNode.appendChild(doc.createTextNode(car.getType()));
		carData.appendChild(typeNode);
		
		Element numberPlateNode = doc.createElement("Kennzeichen");
		numberPlateNode.appendChild(doc.createTextNode(car.getNumberPlate()));
		carData.appendChild(numberPlateNode);
		
		Element startKm = doc.createElement("Startkilometer");
		startKm.appendChild(doc.createTextNode(String.valueOf(car.getStartKm())));
		carData.appendChild(startKm);
		
		Element fuelType = doc.createElement("Kraftstoff");
		fuelType.appendChild(doc.createTextNode(car.getFuelType().value()));
		carData.appendChild(fuelType);
		
		Element consNode = doc.createElement("Durchschnittsverbrauch");
		consNode.appendChild(doc.createTextNode(String.valueOf(cons)));
		carData.appendChild(consNode);
		
		return carData;
	}
	
	private Element getConsumptionData(Document doc, List<Consumption> consumptions) {
		Element consumptionData = doc.createElement("Consumptions");
		for (Consumption con : consumptions) {
			Element consumptionNode = doc.createElement("Consumption");
			
			Element dateNode = doc.createElement("Datum");
			dateNode.appendChild(doc.createTextNode(getDateTime(con.getDate(), true)));
			consumptionNode.appendChild(dateNode);
			
			Element refuelKm = doc.createElement("Kilometerstand");
			refuelKm.appendChild(doc.createTextNode(String.valueOf(con.getRefuelmileage())));
			consumptionNode.appendChild(refuelKm);
			
			Element drivenKm = doc.createElement("GefahreneKilometer");
			drivenKm.appendChild(doc.createTextNode(String.valueOf(con.getDrivenmileage())));
			consumptionNode.appendChild(drivenKm);
			
			Element refuelLiter = doc.createElement("LiterGetankt");
			refuelLiter.appendChild(doc.createTextNode(String.valueOf(con.getRefuelliters())));
			consumptionNode.appendChild(refuelLiter);
			
			Element refuelPrice = doc.createElement("PreisJeLiter");
			refuelPrice.appendChild(doc.createTextNode(String.valueOf(con.getRefuelprice())));
			consumptionNode.appendChild(refuelPrice);
			
			Element consValue = doc.createElement("Verbrauch");
			consValue.appendChild(doc.createTextNode(String.valueOf(con.getConsumption())));
			consumptionNode.appendChild(consValue);
			
			consumptionData.appendChild(consumptionNode);
		}
		return consumptionData;
	}
	
	private String getDateTime(Date date, boolean shortFormat) {
		if (!shortFormat) {
			return dateFormat.format(date);
		}
		return shortDateFormat.format(date);
	}
}
