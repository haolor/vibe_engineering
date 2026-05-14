package com.vibe.finance.repo;

import com.vibe.finance.model.CategoryDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CategoryRepository extends MongoRepository<CategoryDocument, String> {
}

