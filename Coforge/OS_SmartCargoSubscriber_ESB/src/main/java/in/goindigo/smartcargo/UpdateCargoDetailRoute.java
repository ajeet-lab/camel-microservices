package in.goindigo.smartcargo;

import java.net.UnknownHostException;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

@Component
public class UpdateCargoDetailRoute extends RouteBuilder{

	@Override
	public void configure() throws Exception {
		
		onException(UnknownHostException.class)
			.maximumRedeliveries("{{maximumRedeliveries}}")
			.redeliveryDelay("{{redeliveryDelay}}")
			.retryAttemptedLogLevel(LoggingLevel.WARN)
			.handled(true)
			.log(LoggingLevel.ERROR, "${exchangeId} SPSTA6EAdieuHrHUB  In Retry Exception block and exception is ${exception.message}");
		
		onException(Exception.class)
		.log(LoggingLevel.ERROR, "${header.primaryKeySearch} SmartCargoSubscriber028 exception ${exception} and FltNo :: ${header.FltNo} at ${date:now:yyyy-MM-dd'T'HH:mm:ss:SSS}")
		.removeHeader("*");
			
		from("direct:updateCargoDetails").streamCaching()
			.log("${header.primaryKeySearch} SmartCargoSubscriber015 In updateCargoDetails at ${date:now:yyyy-MM-dd HH:mm:ss} ** FlightNumber ${header.FltNo},FlightDate ${header.FltDate},Dep ${header.Dep},Arr ${header.Arr}")
			.setBody(simple("{\"ClientUID\":\"{{ClientUID}}\",\"ClientKey\":\"{{ClientKey}}\"}"))
			.setHeader("Content-Type").constant("application/json")
			.to("{{SMGenerateTokenAPI}}")
			.convertBodyTo(String.class)
			.setBody(simple("${body.substring(19,${body.lastIndexOf(':')})}"))
			.setBody(simple("${body.replaceAll('\\\\','')}"))
			.unmarshal().json(JsonLibrary.Jackson)
			.log("${header.primaryKeySearch} SmartCargoSubscriber016 SmartCargo Token ** ${body[Table][0][TokenNumber]} for FlightNumber ${header.FltNo},FlightDate ${header.FltDate},Dep ${header.Dep},Arr ${header.Arr}")
			.setHeader("TokenNumber").simple("${body[Table][0][TokenNumber]}")
			.setBody(simple("{\"FltNo\":\"${header.FltNo}\",\"Dep\":\"${header.Dep}\",\"Arr\":\"${header.Arr}\",\"DepartDate\":\"${header.FltDate}\",\"TokenNumber\":\"${header.TokenNumber}\"}"))
			.log("${header.primaryKeySearch} SmartCargoSubscriber017 FlightNumber ${header.FltNo} Fetching cargo details for ** ${body} for FlightNumber ${header.FltNo},FlightDate ${header.FltDate},Dep ${header.Dep},Arr ${header.Arr}")
			.to("{{SMGetCargoDetailAPI}}")		
			//.setBody().constant("{\"d\":\"RESPONSE_START:SUCCESS:RESPONSE_ENDRESULT_START:{\\\"Table\\\":[{\\\"FltDate\\\":\\\"2022-07-13T00:00:00\\\",\\\"FltOrigin\\\":\\\"IXA\\\",\\\"FltDestination\\\":\\\"CCU\\\",\\\"FltNumber\\\":\\\"6E604\\\",\\\"ActualCarGoFigure\\\":6.20,\\\"ActualMail\\\":0.00,\\\"OffloadCargo\\\":null,\\\"OffloadMail\\\":null,\\\"StandbyCargo\\\":0.00,\\\"StandbyMail\\\":0.00,\\\"Acregid\\\":\\\"VTIZO\\\",\\\"CreatedBy\\\":\\\"anirban.debnath@goindigo.in\\\",\\\"CreatedOn\\\":\\\"2022-07-13T09:28:01.537\\\",\\\"UpdatedBy\\\":\\\"anirban.debnath@goindigo.in\\\",\\\"UpdatedOn\\\":\\\"2022-07-13T09:28:14\\\"}]}:RESULT_END\"}")
			.log("${header.primaryKeySearch} SmartCargoSubscriber018 FlightNumber ${header.FltNo} CargoDetails received response ** ${body} for FlightNumber ${header.FltNo},FlightDate ${header.FltDate},Dep ${header.Dep},Arr ${header.Arr}")
			.convertBodyTo(String.class)
			.setBody(simple("${body.substring(54,${body.lastIndexOf(':')})}"))
			.setBody(simple("${body.replaceAll('\\\\','')}"))
			.unmarshal().json(JsonLibrary.Jackson)
			.log("${header.primaryKeySearch} SmartCargoSubscriber019 CargoDetails Final Body ** ${body} for FlightNumber ${header.FltNo},FlightDate ${header.FltDate},Dep ${header.Dep},Arr ${header.Arr}")
			.setBody(simple("${body[Table]}"))
			.choice()
				.when(simple("${body[0][Column1]} == 'AWb not present or No data found' "))
					.log(LoggingLevel.WARN, "${header.primaryKeySearch} SmartCargoSubscriber019_1 ${body[0][Column1]} , No Data Found")
				.otherwise()
					.bean(Processing.class, "getCLCBody")
					.log("${header.primaryKeySearch} SmartCargoSubscriber020 Update cargo details in CLC ** ${body} for FlightNumber ${header.FltNo},FlightDate ${header.FltDate},Dep ${header.Dep},Arr ${header.Arr}")
					.marshal().json(JsonLibrary.Jackson)
					.log("${header.primaryKeySearch} SmartCargoSubscriber020_1 Update cargo details in CLC ** FlightNumber ${header.FltNo} After marshalled Final Body = ${body} ")
					.removeHeaders("*", "Dep|Arr|FltDate|FltNo|TokenNumber|90mincheck|45mincheck|primaryKeySearch")
					.setHeader("Content-Type").constant("application/json")
					.setHeader("Authorization").simple("{{CLCBasicAuth}}")
					.to("{{CLCUpdateCargoAPI}}")
					.log("${header.primaryKeySearch} SmartCargoSubscriber021 CLC Update API response ** ${body} for FlightNumber ${header.FltNo},FlightDate ${header.FltDate},Dep ${header.Dep},Arr ${header.Arr}")
					.unmarshal().json(JsonLibrary.Jackson)
					.setHeader("AckEvent").simple("${body[AckEvent]}")
					.setHeader("TotalWeightReceived").simple("${body[TotalWeightReceived]}")
					.choice()
						.when(simple("${header.90mincheck}"))
							.setHeader("Cargo90min").simple("1")
							.setBody(simple("${in.headers}"))
							.to("mybatis:updateCargoStatus?statementType=Update")
							.log("${header.primaryKeySearch} SmartCargoSubscriber022 updating 90min status in DB for FlightNumber ${header.FltNo},FlightDate ${header.FltDate},Dep ${header.Dep},Arr ${header.Arr}")
							.setBody(simple("{\"FltNo\":\"${header.FltNo}\",\"Dep\":\"${header.Dep}\",\"Arr\":\"${header.Arr}\",\"DepartDate\":\"${header.FltDate}\",\"status\":\"D-90 min cargo for Total weight ${header.TotalWeightReceived} updated for Flight ${header.FltNo}\",\"TokenNumber\":\"${header.TokenNumber}\"}"))
						.when(simple("${header.45mincheck}"))
							.setHeader("Cargo45min")
							.simple("1")
							.setBody(simple("${in.headers}"))
							.to("mybatis:updateCargoStatus?statementType=Update")
							.setBody(simple("{\"FltNo\":\"${header.FltNo}\",\"Dep\":\"${header.Dep}\",\"Arr\":\"${header.Arr}\",\"DepartDate\":\"${header.FltDate}\",\"status\":\"D-45 min cargo for Total weight ${header.TotalWeightReceived} updated for Flight ${header.FltNo}\",\"TokenNumber\":\"${header.TokenNumber}\"}"))
							.log("${header.primaryKeySearch} SmartCargoSubscriber023 updating 45min status in DB for FlightNumber ${header.FltNo},FlightDate ${header.FltDate},Dep ${header.Dep},Arr ${header.Arr}")
						.end()
					.log("${header.primaryKeySearch} SmartCargoSubscriber024 sending update acknowledge to SmartCargo ** ${body} for FlightNumber ${header.FltNo},FlightDate ${header.FltDate},Dep ${header.Dep},Arr ${header.Arr}")
					.to("{{SMAcknowledgeAPI}}")
					.log("${header.primaryKeySearch} SmartCargoSubscriber025 SmartCargo acknowledge response ** ${body} for FlightNumber ${header.FltNo},FlightDate ${header.FltDate},Dep ${header.Dep},Arr ${header.Arr}");
		
	}

}
