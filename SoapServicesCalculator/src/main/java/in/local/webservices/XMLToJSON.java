package in.local.webservices;
import org.apache.camel.Exchange;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;


public class XMLToJSON {
	private static final Logger LOGGER = Logger.getLogger(XMLToJSON.class);
	public static void jsonConverter(Exchange ex) {
		System.out.println();
		String xml = (String) ex.getIn().getBody(String.class);
		try {  
			JSONObject json = XML.toJSONObject(xml);
		    ex.getIn().setBody(json.toString());
			}catch (JSONException e) {  
			System.out.println(e.toString());  
			} 
	}
	
}
