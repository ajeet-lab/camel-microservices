package in.goindigo.smartcargo;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import com.microsoft.sqlserver.jdbc.SQLServerException;

@Component
public class ProceedToCargoRoute extends RouteBuilder{

	@Override
	public void configure() throws Exception {
	
		
		  onException(SQLServerException.class)
		  .log("${header.primaryKeySearch} SmartCargoSubscriber012 exception ${exception} at and FltNo :: ${header.FltNo} ${date:now:yyyy-MM-dd'T'HH:mm:ss:SSS}") 
		  .removeHeader("*");
		  
		  onException(RuntimeException.class)
		  .log("${header.primaryKeySearch} SmartCargoSubscriber013 exception ${exception} at and FltNo :: ${header.FltNo} ${date:now:yyyy-MM-dd'T'HH:mm:ss:SSS}") 
		  .removeHeader("*");
		 
		
		onException(Exception.class)
			.log("${header.primaryKeySearch} SmartCargoSubscriber014 exception ${exception} and FltNo :: ${header.FltNo} at ${date:now:yyyy-MM-dd'T'HH:mm:ss:SSS}")
			.removeHeader("*");
		
		from("direct:proceedToCargo").id("ProceedToCargoRoute").streamCaching()
			.log("${header.primaryKeySearch} SmartCargoSubscriber_proceedToCargo001 started at ${date:now:yyyy-MM-dd HH:mm:ss} ** FlightNumber ${header.FltNo},FlightDate ${header.FltDate},Dep ${header.Dep},Arr ${header.Arr}")
			.setBody(simple("${in.headers}"))
			.to("mybatis:selectCargoDetails?statementType=SelectList")
			.log("${header.primaryKeySearch} SmartCargoSubscriber002 Database response body ** ${body} for FlightNumber ${header.FltNo},FlightDate ${header.FltDate},Dep ${header.Dep},Arr ${header.Arr}")
			.choice()
				.when(simple("${body} == '[]'"))
					.setBody(simple("${in.headers}"))
					.to("mybatis:insertflightDetails?statementType=Insert")
					.log("${header.primaryKeySearch} SmartCargoSubscriber003 Flight details inserted in DB for FlightNumber ${header.FltNo},FlightDate ${header.FltDate},Dep ${header.Dep},Arr ${header.Arr}")
				.otherwise()
					.log("${header.primaryKeySearch} SmartCargoSubscriber004 Flight details already inserted in DB for FlightNumber ${header.FltNo},FlightDate ${header.FltDate},Dep ${header.Dep},Arr ${header.Arr}")
			.end()
			.bean(Processing.class, "getTimeDiff")
			.choice()
				.when(simple("${header.greater90min}"))
					.log("${header.primaryKeySearch} SmartCargoSubscriber005 Flight Takeoff is greater than 90 min for FlightNumber ${header.FltNo},FlightDate ${header.FltDate},Dep ${header.Dep},Arr ${header.Arr}")
				.otherwise()
					.choice()
						.when(simple("${header.90mincheck}"))
							.log("${header.primaryKeySearch} SmartCargoSubscriber006 Flight Takeoff is between 90 min and 45 min for FlightNumber ${header.FltNo},FlightDate ${header.FltDate},Dep ${header.Dep},Arr ${header.Arr}")
							.setBody(simple("${in.headers}"))
							.to("mybatis:selectCargoDetails?statementType=SelectOne")
							.choice()
								.when(simple("${body[cargo90min]}"))
									.log("${header.primaryKeySearch} SmartCargoSubscriber007 Flight Cargo details already updated in CLC at 90 min for FlightNumber ${header.FltNo},FlightDate ${header.FltDate},Dep ${header.Dep},Arr ${header.Arr}")
								.otherwise()
									.log("${header.primaryKeySearch} SmartCargoSubscriber008 Fetching Cargo details for 90 min for FlightNumber ${header.FltNo},FlightDate ${header.FltDate},Dep ${header.Dep},Arr ${header.Arr} from SmartCargo")
									.setHeader("AckEventESB").simple("D-90")
									.to("direct:updateCargoDetails")
						.when(simple("${header.45mincheck}"))
							.log("${header.primaryKeySearch} SmartCargoSubscriber009 Flight Takeoff is less than 45 min for FlightNumber ${header.FltNo},FlightDate ${header.FltDate},Dep ${header.Dep},Arr ${header.Arr}")
							.setBody(simple("${in.headers}"))
							.to("mybatis:selectCargoDetails?statementType=SelectOne")
							.choice()
								.when(simple("${body[cargo45min]}"))
									.log("${header.primaryKeySearch} SmartCargoSubscriber010 Flight Cargo details already updated in CLC at 45 min for FlightNumber ${header.FltNo},FlightDate ${header.FltDate},Dep ${header.Dep},Arr ${header.Arr}")
								.otherwise()
								.log("${header.primaryKeySearch} SmartCargoSubscriber011 Fetching Cargo details for 45 min for FlightNumber ${header.FltNo},FlightDate ${header.FltDate},Dep ${header.Dep},Arr ${header.Arr} from SmartCargo")
								.setHeader("AckEventESB").simple("D-45")
								.to("direct:updateCargoDetails");
								
	}

}
