package info.kalyan.krishi.pojos;

import java.util.List;
import java.util.ArrayList;

public class VendorDTO {
	public String name = "";
	public String mobile = "";
	public String email = "";
	public String aadhaar = "";
	public String address1 = "";
	public ArrayList<VoucherDTO> vouchers = new ArrayList<VoucherDTO>();
	public double creditBalance = 0;
	public double openingBalance = 0;

	public VendorDTO(Vendor vendor, List<Voucher> vouchers) {
		this.name = vendor.name;
		this.mobile = vendor.mobile;
		this.address1 = vendor.address1;
		for (Voucher voucher : vouchers) {
			VoucherDTO vDto = new VoucherDTO(voucher);
			this.vouchers.add(vDto);
		}
		this.creditBalance = vendor.creditBalance;
	}

	public VendorDTO() {
	}
}