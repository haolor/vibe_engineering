package com.vibe.finance.repo;

import com.vibe.finance.model.TransactionDocument;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TransactionRepository extends MongoRepository<TransactionDocument, String> {
    List<TransactionDocument> findByUserIdOrderByTransactionDateDescIdDesc(String userId);
    Optional<TransactionDocument> findByUserIdAndId(String userId, Long id);
}
