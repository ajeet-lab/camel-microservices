package in.goindigo.webservices;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;

import com.sun.istack.logging.Logger;

public class Convert {

	private static final transient Logger LOGGER = Logger.getLogger(Convert.class);

	public List<Map<String, Object>> epochToDate(Exchange exchange)
	{
		Object object=exchange.getIn().getBody();
		//System.out.println("type name=="+object.getClass().getName());
		List list=exchange.getIn().getBody(List.class);
		//System.out.println("list=="+list);
		List<Map<String, Object>> data=new ArrayList<Map<String,Object>>();
		Iterator it=list.iterator();
		while(it.hasNext())
		{
			HashMap<String, Object> hashMap=(HashMap<String, Object>)it.next();
			for(Map.Entry<String, Object> entry : hashMap.entrySet())
			{
				if(entry.getValue() != null)
				{
					if(entry.getValue().getClass().getName().equals("java.sql.Timestamp"))
					{
						String val=entry.getValue().toString();
						//System.out.println("class name1=:"+entry.getKey());
						hashMap.put(entry.getKey(), val);
						//System.out.println("class name=:"+hashMap.get(entry.getKey()).getClass().getName());
					}
				}	
			}
			data.add(hashMap);
		}
		//System.out.println("data=="+data);
		return data;
	}

	public void CustomException(Exchange exchange)
	{
		String fdate = exchange.getIn().getHeader("Flight_Date", String.class);
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		//format.setLenient(true);
		format.setLenient(false);
		try {
			Date date = format.parse(fdate);
		} catch (ParseException e) {
			throw new RuntimeException("Enter valid date format!");
		}
	}

	public void mergeBody(Exchange exchange){
		List<Map<String, Object>> list = exchange.getIn().getHeader("OrigBody",List.class);
		List<Map<String, Object>> listBody = exchange.getIn().getBody(List.class);
		List finalList = new ArrayList<>();

		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> mapBody = new HashMap<String, Object>();

		for(int i =0; i < list.size(); i++ ) {
			map = (Map<String, Object>) list.get(i);
			String fltNbr = null;
			if(map.containsKey("FLT_NBR"))
				fltNbr = map.get("FLT_NBR").toString();
			else
				fltNbr = map.get("flt_nbr").toString();

			for(int j =0; j < listBody.size(); j++ ) {
				mapBody = (Map<String, Object>) listBody.get(j);
				if(fltNbr.equals(mapBody.get("FLT_NBR").toString())){
					map.put("ActualDateTime",mapBody.get("ActualDateTime"));
					finalList.add(map);
					break;				
				}
			}
		}

		exchange.getIn().setBody(finalList);
	}

}

