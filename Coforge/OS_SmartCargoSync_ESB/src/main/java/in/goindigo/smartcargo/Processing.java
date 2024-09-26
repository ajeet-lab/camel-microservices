package in.goindigo.smartcargo;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Date;
import java.text.ParseException;

public class Processing {

	private static final Logger LOGGER = LoggerFactory.getLogger(Processing.class);

	public  void getDate(Exchange exchange) throws ParseException{ 

		int timeOffset = Integer.parseInt(exchange.getIn().getHeader("timeOffset",String.class));
		String currentDate = exchange.getIn().getHeader("fromdate",String.class);
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date date = df.parse(currentDate); 

		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.MINUTE, timeOffset);
		String todate = df.format(cal.getTime());
		exchange.getIn().setHeader("todate", todate);
	}

	public void getConvertedDate(Exchange exchange) throws ParseException{
		Map<String, Object> mapBody=exchange.getIn().getBody(Map.class);

		String fltDate = mapBody.get("FltDate").toString();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

		String FltNbr = mapBody.get("FltNbr").toString();

		if(FltNbr.length() == 2)
			FltNbr = "0"+FltNbr;
		else if(FltNbr.length() == 1)
			FltNbr = "00"+FltNbr;

		exchange.getIn().setHeader("FltNbr",FltNbr);

		Date convertDate = format.parse(fltDate);
		exchange.getIn().setHeader("convertFltDate",sdf.format(convertDate));
	}

}
