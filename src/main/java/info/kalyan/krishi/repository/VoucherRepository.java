package info.kalyan.krishi.repository;

import org.joda.time.DateTime;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

import info.kalyan.krishi.pojos.Voucher;
import info.kalyan.krishi.pojos.Voucher.VoucherType;

@Repository
public interface VoucherRepository extends MongoRepository<Voucher, String> {
    @Query(value = "{ 'transactionDate' : {$gte : ?0, $lte: ?1, $eq : ?2 } }", fields = "{'rate' : 1 , 'quantity' : 1 , 'unit' : 1 , 'voucherType' : 1 , 'value' : 1 , 'transactionDate' : 1}")
    List<Voucher> findByTransactionDateBetweenAndVoucherType(DateTime from, DateTime to, VoucherType voucherType);

    @Query(value = "{ 'transactionDate' : {$gte : ?0, $lte: ?1} }", fields = "{'rate' : 1 , 'quantity' : 1 , 'unit' : 1 , 'voucherType' : 1 , 'value' : 1 , 'transactionDate' : 1}")
    List<Voucher> findByTransactionDateBetween(DateTime from, DateTime to);

    List<Voucher> findByVoucherType(VoucherType voucherType);

    List<Voucher> findByProductId(String productId);
}
