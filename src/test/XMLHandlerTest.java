package test;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

public class XMLHandlerTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	
		Document doc = null;
		try {
			doc = readXMLDocumentFromFile("", "SKODA_Octavia III 1.4 TSI DSG_30.04.2014-08.21.50.xml");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(doc != null);
		
		
	}
	
	public static Document readXMLDocumentFromFile(String folder, String name) throws Exception {
		File xmlFile = new File(folder, name);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(xmlFile);
		doc.getDocumentElement().normalize();
		return doc;
	}

}
