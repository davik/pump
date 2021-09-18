package info.kalyan.krishi.repository;

import org.joda.time.DateTime;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

import info.kalyan.krishi.pojos.Vendor;

@Repository
public interface VendorRepository extends MongoRepository<Vendor, String> {
    @Query(value = "{ 'vouchers.transactionDate' : {$gte : ?0, $lte: ?1 } }", fields = "{'name': 1, 'mobile': 1, 'vouchers.rate' : 1 , 'vouchers.quantity' : 1 , 'vouchers.unit' : 1 , 'vouchers.voucherType' : 1 , 'vouchers.value' : 1 , 'vouchers.transactionDate' : 1}")
    List<Vendor> findByVouchersTransactionDateBetween(DateTime from, DateTime to);
}
