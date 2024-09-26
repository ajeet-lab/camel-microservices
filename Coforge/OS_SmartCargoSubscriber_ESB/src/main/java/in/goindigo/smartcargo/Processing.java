package in.goindigo.smartcargo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.camel.Exchange;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import java.text.ParseException;
import java.util.Date;

@Component
public class Processing {

	private static final Logger LOGGER = LoggerFactory.getLogger(Processing.class);
	
	public void getTimeDiff(Exchange exchange) throws ParseException{ 
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, 0);

		Date fltTime = null,currentTime = null;
		long diff = 0;

		fltTime = format.parse(exchange.getIn().getHeader("DepartureTime",String.class));
		currentTime = format.parse(format.format(cal.getTime()));

		diff = fltTime.getTime() - currentTime.getTime();
		if(TimeUnit.MILLISECONDS.toMinutes(diff) <= 90 && TimeUnit.MILLISECONDS.toMinutes(diff) > 45){
			exchange.getIn().setHeader("90mincheck",true);	
		} else if(TimeUnit.MILLISECONDS.toMinutes(diff) <= 45 && TimeUnit.MILLISECONDS.toMinutes(diff) > 0){
			exchange.getIn().setHeader("45mincheck",true);	
		} else
			exchange.getIn().setHeader("greater90min",true);

		//exchange.getIn().setHeader("90mincheck",true);	// Need to Delete
	}

	public void getCLCBodyold(Exchange exchange){
		Map<String, Object> mapBody = (Map<String, Object>) exchange.getIn().getBody(Map.class);	

		Map<String, Object> finalBody = new HashMap<String,Object>();
		finalBody.put("Date",mapBody.get("FltDate").toString().substring(0,10));
		finalBody.put("DEP",mapBody.get("FltOrigin").toString());
		finalBody.put("ARR",mapBody.get("FltDestination").toString());
		finalBody.put("FLTNO",mapBody.get("FltNumber").toString().substring(2));
		finalBody.put("ActualCarGoFigure",mapBody.get("ActualCarGoFigure") == null?"":Math.round(Float.parseFloat(mapBody.get("ActualCarGoFigure").toString())));
		finalBody.put("ActualMail",mapBody.get("ActualMail") == null?"":Math.round(Float.parseFloat(mapBody.get("ActualMail").toString())));
		finalBody.put("OffloadCargo",mapBody.get("OffloadCargo") == null?"":Math.round(Float.parseFloat(mapBody.get("OffloadCargo").toString())));
		finalBody.put("OffloadMail",mapBody.get("OffloadMail") == null?"":Math.round(Float.parseFloat(mapBody.get("OffloadMail").toString())));
		finalBody.put("StandbyCargo",mapBody.get("StandbyCargo") == null?"":Math.round(Float.parseFloat(mapBody.get("StandbyCargo").toString())));
		finalBody.put("StandbyMail",mapBody.get("StandbyMail") == null?"":Math.round(Float.parseFloat(mapBody.get("StandbyMail").toString())));
		finalBody.put("UpdatedBy",mapBody.get("UpdatedBy") == null?"":mapBody.get("UpdatedBy").toString());
		finalBody.put("Acregid",mapBody.get("Acregid") == null?"":mapBody.get("Acregid").toString());

		exchange.getIn().setBody(finalBody);
	}



	public LinkedHashMap<String, Object> getCLCBody(Exchange exchange) {

		List<Map<String, Object>> map = (List<Map<String, Object>>) exchange.getIn().getBody();
		List<String> ls1 = new ArrayList<>();
		LinkedHashMap<String, Object> map2 = new LinkedHashMap<>();
		List<Map<String, Object>> ls4 = new ArrayList<>();
		String fltdate = dateformater(map);
		
		//map2.put("FltNo", map.get(0).get("FltNumber").toString().toLowerCase().replace("6e",""));
		map2.put("FltNo", Integer.parseInt(map.get(0).get("FltNumber").toString().toLowerCase().replace("6e","")));
		//map2.put("FltDate", fltdate);
		
		map2.put("Date", fltdate);
		map2.put("Dep", map.get(0).get("FltOrigin"));
		map2.put("ARR", map.get(map.size()-1).get("FltDestination"));
		map2.put("AckEvent", exchange.getIn().getHeader("AckEventESB"));


		for (Map<String, Object> ls : map) {
			Map<String, Object> map3 = new HashMap<>();
			Map<String, Object> map5 = new HashMap<>();
			String str = ls.get("FltOrigin") + "-" + ls.get("FltDestination");

			ls1.add(str);
			map3.put("ActualCarGoFigure", ls.get("ActualCarGoFigure") == null ? ""
					: Math.round(Float.parseFloat(ls.get("ActualCarGoFigure").toString())));
			map3.put("ActualMail",
					ls.get("ActualMail") == null ? "" : Math.round(Float.parseFloat(ls.get("ActualMail").toString())));
			map3.put("OffloadMail", ls.get("OffloadMail") == null ? ""
					: Math.round(Float.parseFloat(ls.get("OffloadMail").toString())));
			map3.put("StandbyCargo", ls.get("StandbyCargo") == null ? ""
					: Math.round(Float.parseFloat(ls.get("StandbyCargo").toString())));
			map3.put("OffloadCargo", ls.get("OffloadCargo") == null ? ""
					: Math.round(Float.parseFloat(ls.get("OffloadCargo").toString())));
			map3.put("StandbyMail", ls.get("StandbyMail") == null ? ""
					: Math.round(Float.parseFloat(ls.get("StandbyMail").toString())));
			map3.put("SectorName", str == null ? "": str);

			//map5.put(str, map3);

			ls4.add(map3);
		}
		map2.put("sectors", ls1);
		map2.put("SectorData", ls4);

		return map2;
	}

	private String dateformater(List<Map<String, Object>> map) {

		String input = map.get(0).get("FltDate").toString();
		SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd");
		Date date;
		String fltdate = null;
		try {
			date = format1.parse(input);
			fltdate = format2.format(date);
			LOGGER.info(" Flight date " + fltdate);
		} catch (ParseException e) {
			throw new RuntimeException("SmartCargoSubscriber_ESB Exception in converting date to ddMMMyy format and input date " + input);
		}
		return fltdate;
	}
	
	public boolean isCamelDuplicateMessage(Exchange exchange) {
		Boolean duplicateMessage = exchange.getProperty("CamelDuplicateMessage",Boolean.class);
		String primaryKeySearch = exchange.getIn().getHeader("primaryKeySearch",String.class);
		LOGGER.info(primaryKeySearch+" CamelDuplicateMessage ::"+duplicateMessage);
		if(duplicateMessage==null)
		   return false;
		else
			return duplicateMessage;
	}
	
}
