package info.kalyan.krishi.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.opencsv.CSVWriter;
import info.kalyan.krishi.pojos.Counter;
import info.kalyan.krishi.pojos.Product;
import info.kalyan.krishi.pojos.ProductDTO;
import info.kalyan.krishi.pojos.StockDTO;
import info.kalyan.krishi.pojos.Voucher;
import info.kalyan.krishi.pojos.VoucherDTO;
import info.kalyan.krishi.pojos.Warehouse;
import info.kalyan.krishi.pojos.WarehouseDTO;
import info.kalyan.krishi.pojos.Voucher.VoucherType;
import info.kalyan.krishi.pojos.Vendor;
import info.kalyan.krishi.pojos.VendorDTO;
import info.kalyan.krishi.repository.CounterRepository;
import info.kalyan.krishi.repository.ProductRepository;
import info.kalyan.krishi.repository.VendorRepository;
import info.kalyan.krishi.repository.VoucherRepository;
import info.kalyan.krishi.repository.WarehouseRepository;

@Controller
public class WelcomeController {

	public WelcomeController(VendorRepository vendorRepo, CounterRepository counterRepo, WarehouseRepository whRepo,
			ProductRepository productRepo, VoucherRepository voucherRepo) {
		super();
		this.vendorRepo = vendorRepo;
		this.counterRepo = counterRepo;
		this.whRepo = whRepo;
		this.productRepo = productRepo;
		this.voucherRepo = voucherRepo;
		populateProductCache();
		populateVendorCache();
		populateWarehouseCache();
	}

	// inject via application.properties
	@Value("${app.welcome.message}")
	private String message = "";

	@Value("${app.welcome.title}")
	private String title = "";

	private final String alert = "alert alert-danger";

	@Autowired
	public final VendorRepository vendorRepo;
	@Autowired
	public final CounterRepository counterRepo;
	@Autowired
	public final WarehouseRepository whRepo;
	@Autowired
	public final ProductRepository productRepo;
	@Autowired
	public final VoucherRepository voucherRepo;

	public HashMap<String, Product> productCache;
	public HashMap<String, Vendor> vendorCache;
	public HashMap<String, Warehouse> warehouseCache;

	public void populateProductCache() {
		List<Product> products = productRepo.findAll();
		if (productCache == null) {
			productCache = new HashMap<>();
		}
		for (Product product : products) {
			productCache.put(product.id, product);
		}
	}

	public void populateVendorCache() {
		List<Vendor> vendors = vendorRepo.findAll();
		if (vendorCache == null) {
			vendorCache = new HashMap<>();
		}
		for (Vendor vd : vendors) {
			vendorCache.put(vd.id, vd);
		}
	}

	public void populateWarehouseCache() {
		List<Warehouse> warehouses = whRepo.findAll();
		if (warehouseCache == null) {
			warehouseCache = new HashMap<>();
		}
		for (Warehouse wh : warehouses) {
			warehouseCache.put(wh.id, wh);
		}
	}

	@GetMapping(path = "/")
	public String welcome(Map<String, Object> model, HttpServletRequest request) {
		populateCommonPageFields(model, request);

		DateTime from = DateTime.now().withTimeAtStartOfDay();
		DateTime to = DateTime.now().withTimeAtStartOfDay().plusHours(24);
		List<Voucher> vouchers = voucherRepo.findByTransactionDateBetween(from, to);

		double totalPurchase = 0;
		double totalPayment = 0;
		double totalSales = 0;
		double totalReceipt = 0;
		for (Voucher voucher : vouchers) {
			if (voucher.transactionDate.getDayOfMonth() == from.getDayOfMonth()) {
				if (voucher.voucherType == VoucherType.PURCHASE) {
					totalPurchase += voucher.value;
				} else if (voucher.voucherType == VoucherType.SALE) {
					totalSales += voucher.value;
				}

			}
		}

		Product msProduct = productRepo.findByName("MS");
		Product hsdProduct = productRepo.findByName("HSD");

		model.put("totalPurchase", totalPurchase);
		model.put("totalPayment", totalPayment);
		model.put("totalSales", totalSales);
		model.put("totalReceipt", totalReceipt);
		model.put("msCurrentPrice", msProduct.price);
		model.put("hsdCurrentPrice", hsdProduct.price);

		return "welcome";
	}

	// test 5xx errors
	@GetMapping("/5xx")
	public String serviceUnavailable() {
		throw new RuntimeException("ABC");
	}

	@PostMapping(path = "/create")
	public String create(Map<String, Object> model, @RequestBody VendorDTO vendor, HttpServletRequest request) {
		populateCommonPageFields(model, request);

		if (vendor.name.isEmpty() || vendor.mobile.isEmpty()) {
			model.put("alert", alert);
			model.put("result", "Please fill the mandatory fields!");
			return "create";
		}
		String idPrefix = "vendor";
		// Counter is used to get the next id to be assigned for a new vendor based on
		// session and course
		Optional<Counter> oct = counterRepo.findById(idPrefix);
		Counter ct = null;
		if (!oct.isPresent()) {
			ct = new Counter();
			ct.id = idPrefix;
			ct.nextId++;
		} else {
			ct = oct.get();
		}
		Vendor vd = new Vendor();
		// Increment and save the counter config
		vd.id = String.format("%03d", ct.nextId);
		ct.nextId++;
		counterRepo.save(ct);

		vd.name = vendor.name;
		vd.aadhaar = vendor.aadhaar;
		vd.mobile = vendor.mobile;
		vd.email = vendor.email;
		vd.address1 = vendor.address1;
		vd.creditBalance = vendor.openingBalance;

		// Add Opening Balance Voucher
		Voucher voucher = new Voucher();
		voucher.voucherId = GetNextVoucherId();
		voucher.transactionDate = (DateTime) DateTime.now().withZone(DateTimeZone.forID("Asia/Kolkata"));
		// voucher.voucherType = VoucherType.OPENING_BALANCE;
		voucher.value = vendor.openingBalance;
		// voucher.vendorId = vd.id;
		// voucher.vendorName = vendor.name;

		voucherRepo.save(voucher);
		vendorRepo.save(vd);

		populateVendorCache();

		model.put("alert", "alert alert-success");
		model.put("result", "Vendor Registered Successfully!");
		return "create";
	}

	@PostMapping(path = "/createWarehouse")
	public String warehouseCreate(Map<String, Object> model, @RequestBody WarehouseDTO wareHouseDTO,
			HttpServletRequest request) {
		populateCommonPageFields(model, request);

		if (wareHouseDTO.name.isEmpty() || wareHouseDTO.location.isEmpty()) {
			model.put("alert", alert);
			model.put("result", "Please fill the mandatory fields!");
			return "create";
		}
		Warehouse existingWarehouse = whRepo.findByName(wareHouseDTO.name);
		if (existingWarehouse != null) {
			model.put("alert", alert);
			model.put("result", "Warehouse " + wareHouseDTO.name + " already exists!");
			return "create";
		}
		String idPrefix = "warehouse";
		// Counter is used to get the next id to be assigned for a new warehouse
		Optional<Counter> oct = counterRepo.findById(idPrefix);
		Counter ct = null;
		if (!oct.isPresent()) {
			ct = new Counter();
			ct.id = idPrefix;
			ct.nextId++;
		} else {
			ct = oct.get();
		}
		Warehouse wHouse = new Warehouse();
		wHouse.name = wareHouseDTO.name;
		wHouse.location = wareHouseDTO.location;
		// Increment and save the counter config
		wHouse.id = String.format("%03d", ct.nextId);
		ct.nextId++;
		counterRepo.save(ct);

		whRepo.save(wHouse);
		populateWarehouseCache();

		model.put("alert", "alert alert-success");
		model.put("result", "Warehouse Registered Successfully!");
		return "create";
	}

	@PostMapping(path = "/createProduct")
	public String productCreate(Map<String, Object> model, @RequestBody ProductDTO productDTO,
			HttpServletRequest request) {
		populateCommonPageFields(model, request);

		if (productDTO.name.isEmpty()) {
			model.put("alert", alert);
			model.put("result", "Please fill the mandatory fields!");
			return "create";
		}
		Product existingProduct = productRepo.findByName(productDTO.name);
		if (existingProduct != null) {
			model.put("alert", alert);
			model.put("result", "Product " + productDTO.name + " already exists!");
			return "create";
		}
		String idPrefix = "product";
		// Counter is used to get the next id to be assigned for a new warehouse
		Optional<Counter> oct = counterRepo.findById(idPrefix);
		Counter ct = null;
		if (!oct.isPresent()) {
			ct = new Counter();
			ct.id = idPrefix;
			ct.nextId++;
		} else {
			ct = oct.get();
		}
		Product product = new Product();
		product.name = productDTO.name;
		product.unit = productDTO.unit;
		// Increment and save the counter config
		product.id = String.format("%03d", ct.nextId);
		ct.nextId++;
		counterRepo.save(ct);

		productRepo.save(product);

		populateProductCache();

		model.put("alert", "alert alert-success");
		model.put("result", "Product Registered Successfully!");
		return "create";
	}

	@PostMapping(path = "/updateProductPrice")
	public String productUpdate(Map<String, Object> model, @RequestBody ProductDTO productDTO,
			HttpServletRequest request) {
		populateCommonPageFields(model, request);

		if (productDTO.name.isEmpty()) {
			model.put("alert", alert);
			model.put("result", "Please fill the mandatory fields!");
			return "create";
		}
		Product existingProduct = productRepo.findByName(productDTO.name);
		if (existingProduct == null) {
			model.put("alert", alert);
			model.put("result", "Product " + productDTO.name + " not found!");
			return "create";
		}

		existingProduct.price = productDTO.price;
		productRepo.save(existingProduct);

		populateProductCache();

		model.put("alert", "alert alert-success");
		model.put("result", "Product Price updated Successfully!");

		return "create";
	}

	@GetMapping(path = "/registration")
	public String getRegistrationPage(Map<String, Object> model, HttpServletRequest request) {
		populateCommonPageFields(model, request);
		model.put("warehouses", warehouseCache.values());
		return "registration";
	}

	@GetMapping(path = "/contact")
	public String getContactPage(Map<String, Object> model, HttpServletRequest request) {
		populateCommonPageFields(model, request);
		return "contact";
	}

	@GetMapping(path = "/vendors")
	public String getVendorsPage(Map<String, Object> model, HttpServletRequest request) {
		populateCommonPageFields(model, request);

		List<Vendor> vendors = vendorRepo.findAll();
		model.put("vendors", vendors);
		return "vendors";
	}

	@GetMapping(path = "/purchase")
	public String getPurchasePage(Map<String, Object> model, HttpServletRequest request) {
		populateCommonPageFields(model, request);
		model.put("products", productCache.values());
		Object[] products = productCache.values().toArray();
		Product pd = (Product) products[0];
		model.put("unit", pd.unit.toString());

		// DateTime from = new DateTime();
		// from.minusDays(30);
		// DateTime to = new DateTime();
		List<Voucher> purchaseVouchers = voucherRepo.findByVoucherType(VoucherType.PURCHASE);
		model.put("purchase", purchaseVouchers);
		return "purchase";
	}

	@GetMapping(value = "/getUnit")
	@ResponseBody
	public String getUnit(Map<String, Object> model, @RequestParam(name = "product") String productId,
			HttpServletRequest request) {
		String unit = "";

		for (Product pd : productCache.values()) {
			if (pd.id.equals(productId)) {
				unit = pd.unit.toString();
			}
		}

		return unit;
	}

	@GetMapping(path = "/sale")
	public String getSalePage(Map<String, Object> model, HttpServletRequest request) {
		populateCommonPageFields(model, request);

		return "sale";
	}

	@GetMapping(path = "/stock")
	public String getStockPage(Map<String, Object> model, HttpServletRequest request) {
		populateCommonPageFields(model, request);

		List<StockDTO> stocks = new ArrayList<>();
		for (Entry<String, Product> entry : productCache.entrySet()) {
			StockDTO stock = new StockDTO();
			stock.productId = entry.getKey();
			stock.productName = entry.getValue().name;
			stock.unit = entry.getValue().unit;
			stock.price = entry.getValue().price;

			HashMap<String, Integer> whSpecificStock = new HashMap<>();

			stock.currentStocks.addAll(whSpecificStock.values());
			stocks.add(stock);
		}
		model.put("stocks", stocks);
		return "stock";
	}

	@GetMapping(path = "/payment")
	public String getPaymentPage(Map<String, Object> model, HttpServletRequest request) {
		populateCommonPageFields(model, request);
		return "payment";
	}

	@GetMapping(path = "/report")
	public String getReportPage(Map<String, Object> model, HttpServletRequest request) {
		populateCommonPageFields(model, request);
		return "report";
	}

	@GetMapping(value = "/accountDetails")
	public String getVoucherDetails(Map<String, Object> model, @RequestParam(name = "id") String vendorId,
			@RequestParam(name = "from") @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd") DateTime from,
			@RequestParam(name = "to") @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd") DateTime to,
			HttpServletRequest request) {
		populateCommonPageFields(model, request);

		Optional<Vendor> ovd = vendorRepo.findById(vendorId);
		if (!ovd.isPresent()) {
			model.put("alert", alert);
			model.put("result", "Vendor not found!");
		} else {
			Vendor vendor = ovd.get();
			// For Account Details page
			to = to.plusDays(1);
			List<Voucher> vouchers = voucherRepo.findByTransactionDateBetween(from, to);
			model.put("vendor", new VendorDTO(vendor, vouchers));

			// For Voucher Form
			model.put("products", productCache.values());
			Warehouse wh = new Warehouse("-1", "Direct", "For Transfer Only");
			List<Warehouse> whList = new ArrayList<>();
			whList.addAll(warehouseCache.values());
			whList.add(wh);
			model.put("warehouses", whList);
			model.put("voucherType", getNames(VoucherType.class));
		}
		return "accountDetails";
	}

	@GetMapping(value = "/stockDetails")
	public String getStockDetails(Map<String, Object> model, @RequestParam(name = "id") String productId,
			HttpServletRequest request) {
		populateCommonPageFields(model, request);

		Product product = productCache.get(productId);
		if (null == product) {
			model.put("alert", alert);
			model.put("result", "Product not found!");
		} else {
			// For Product Details page
			List<Voucher> vouchers = voucherRepo.findByProductId(productId);
			List<VoucherDTO> voucherDTOs = new ArrayList<>();
			for (Voucher vc : vouchers) {
				// Stock will not be shown for direct sales

				VoucherDTO vdto = new VoucherDTO(vc);
				voucherDTOs.add(vdto);
			}

			ProductDTO pdto = new ProductDTO();
			pdto.name = product.name;

			model.put("product", pdto);
			model.put("vouchers", voucherDTOs);
		}
		return "productDetails";
	}

	@PostMapping(path = "/createVoucher")
	public String setVoucherDetails(Map<String, Object> model, @RequestBody Voucher voucher,
			HttpServletRequest request) {
		populateCommonPageFields(model, request);

		if (voucher.value == 0) {
			model.put("alert", alert);
			model.put("result", "Please fill the mandatory fields!");
			return "create";
		}

		voucher.voucherId = GetNextVoucherId();
		if (voucher.transactionDate == null) {
			voucher.transactionDate = (DateTime) DateTime.now().withZone(DateTimeZone.forID("Asia/Kolkata"));
		}

		if (!voucher.productId.isEmpty()) {
			Product pd = productCache.get(voucher.productId);
			voucher.productName = pd.name;
		}

		voucherRepo.save(voucher);

		model.put("alert", "alert alert-success");
		model.put("result", "Information Recorded Successfully!");
		return "accountDetails";
	}

	public int getFiscalYear(Calendar calendar) {
		int month = calendar.get(Calendar.MONTH);
		int year = calendar.get(Calendar.YEAR);
		return (month > Calendar.MARCH) ? year : year - 1;
	}

	public void populateCommonPageFields(Map<String, Object> model, HttpServletRequest request) {
		model.put("title", title);
		model.put("message", message);
		model.put("user", request.getRemoteUser());
	}

	@GetMapping(value = "/stockReport")
	public void generateStockReport(Map<String, Object> model,
			@RequestParam(name = "from") @DateTimeFormat(pattern = "yyyy-MM-dd") DateTime from,
			@RequestParam(name = "to") @DateTimeFormat(pattern = "yyyy-MM-dd") DateTime to,
			HttpServletResponse response, HttpServletRequest request) throws IOException {
		populateCommonPageFields(model, request);
		if (from == null || to == null) {
			return;
		}

		List<Voucher> vouchers = voucherRepo.findByTransactionDateBetween(from, to.plusDays(1));

		String outputFileName = "C:\\Users\\polaris2\\" + "paydue.csv";
		File reportFile = new File(outputFileName);

		try {
			// create FileWriter object with file as parameter
			FileWriter outputfile = new FileWriter(reportFile);

			// create CSVWriter object filewriter object as parameter
			CSVWriter writer = new CSVWriter(outputfile);

			// create a List which contains String array
			List<String[]> data = new ArrayList<>();
			data.add(new String[] { "Date", "Rate", "Quantity", "Unit", "Value" });
			double totalStock = 0;
			double totalValue = 0;

			if (null != vouchers) {
				for (Voucher voucher : vouchers) {
					if (voucher.voucherType == VoucherType.PURCHASE && voucher.transactionDate.isAfter(from)
							&& voucher.transactionDate.isBefore(to.plusDays(1))) {
						data.add(new String[] { voucher.transactionDate.toDate().toString(),
								Double.toString(voucher.rate), Double.toString(voucher.quantity), voucher.unit.name(),
								Double.toString(voucher.value) });
						totalStock += voucher.quantity;
						totalValue += voucher.value;
					}
				}
			}

			data.add(new String[] { "", "", "", "", "", "", "" });
			data.add(new String[] { "Total", "", "", "", Double.toString(totalStock), "Kg",
					Double.toString(totalValue) });
			writer.writeAll(data);

			// closing writer connection
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Download section
		String mimeType = "text/csv";
		response.setContentType(mimeType);
		String reportFileName = "StockReport" + "_" + from.getDayOfMonth() + "_" + from.getMonthOfYear() + "_"
				+ to.getDayOfMonth() + "_" + to.getMonthOfYear() + ".csv";
		response.setHeader("Content-Disposition", String.format("attachment; filename=\"" + reportFileName + "\""));
		response.setContentLength((int) reportFile.length());
		try (InputStream inputStream = new BufferedInputStream(new FileInputStream(reportFile))) {
			FileCopyUtils.copy(inputStream, response.getOutputStream());
			response.flushBuffer();
		}
		model.put("alert", "alert alert-success");
		model.put("result", "Report Generated Successfully!");
	}

	public static String[] getNames(Class<? extends Enum<?>> e) {
		return Arrays.stream(e.getEnumConstants()).map(Enum::name).toArray(String[]::new);
	}

	public String GetNextVoucherId() {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("IST"));
		// Fetch the Payment or Money Receipt ID counter
		int year = getFiscalYear(calendar);
		Optional<Counter> oct = counterRepo.findById(year + "-" + String.valueOf(year + 1).substring(2));
		Counter ct = null;
		if (!oct.isPresent()) {
			ct = new Counter();
			ct.id = year + "-" + String.valueOf(year + 1).substring(2);
			ct.nextId++;

		} else {
			ct = oct.get();
		}

		String nextID = ct.id + "/" + String.format("%05d", ct.nextId);

		// Save in DB
		ct.nextId++;
		counterRepo.save(ct);

		return nextID;
	}

}