package in.csvconverter;
import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;

@CsvRecord(separator = ",",generateHeaderColumns = true)
public class JsonToCsvConverter {
	
	@DataField(pos=1, columnName = "Name")
	private String name;
	
	@DataField(pos=2, columnName = "Email")
	private String email;

	public void setName(String name) {
		this.name = name;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	
	
	
}
