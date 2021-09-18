package info.kalyan.krishi.pojos;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "vendors")
public class Vendor {
	@Id
	public String id;
	public String name = "";
	public String mobile = "";
	public String email = "";
	public String aadhaar = "";
	public String address1 = "";
	public double creditBalance = 0;
}