package info.kalyan.krishi.pojos;

import org.joda.time.DateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "vouchers")
public class Voucher {
	public enum VoucherType {
		PURCHASE, SALE
	}

	@Id
	public String voucherId;
	@Indexed
	@org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd")
	public DateTime transactionDate;
	public VoucherType voucherType;
	public String transactionInfo = "";
	public String productId = "";
	public String productName = "";
	public double quantity = 0;
	public ProductDTO.Unit unit = ProductDTO.Unit.Litre;
	public double rate = 0;
	public double value = 0;
	public double balance = 0;
	public String mode = "";
}