package in.goindigo.smartcargo;

import java.io.File;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.processor.idempotent.FileIdempotentRepository;
import org.springframework.stereotype.Component;

@Component
public class CargoTopicSubscriberRoute extends RouteBuilder{

	@Override
	public void configure() throws Exception {
	
		from("{{amqp.queue.name}}").id("CargoTopicSubscriberRoute").streamCaching()
			.setHeader("primaryKeySearch").simple("${header.FltNo}${header.FltDate}${header.Dep}${header.Arr}")
			.log("${header.primaryKeySearch} SmartCargoSubscriber001 started ** FlightNumber ${header.FltNo},FlightDate ${header.FltDate},Dep ${header.Dep},Arr ${header.Arr}  ")
			.setHeader("messageId").simple("${header.FltNo}${header.FltDate}${header.Dep}${header.Arr}${date:now:yyyy-MM-dd HH:mm}")
			.log("${header.primaryKeySearch}  messageID :: ${header.messageId}")
			.idempotentConsumer(header("messageId"), FileIdempotentRepository.fileIdempotentRepository(new File("ideptetnt.txt")))
			.filter().method(Processing.class, "isCamelDuplicateMessage")
			.log(LoggingLevel.ERROR, "${header.primaryKeySearch} Dubplicate found ** FlightNumber ${header.FltNo},FlightDate ${header.FltDate},Dep ${header.Dep},Arr ${header.Arr} ")
			.stop()
			.end()
			.to("direct:proceedToCargo");
	}

}
