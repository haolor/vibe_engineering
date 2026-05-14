package com.vibe.finance.config;

import com.vibe.finance.model.CounterDocument;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class SequenceService {
    private final MongoTemplate mongoTemplate;

    public SequenceService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public long next(String key) {
        Query query = new Query(Criteria.where("_id").is(key));
        Update update = new Update().inc("value", 1L);
        FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true).upsert(true);
        CounterDocument counter = mongoTemplate.findAndModify(query, update, options, CounterDocument.class);
        return counter == null ? 1L : counter.getValue();
    }

    /**
     * Ensures the counter for `key` is at least `minimum`.
     * Call this after seeding data to prevent new IDs colliding with seeded IDs.
     */
    public void ensureMinimum(String key, long minimum) {
        // Upsert: if doc doesn't exist, set value = minimum
        // If it exists but value < minimum, set value = minimum
        Query query = new Query(Criteria.where("_id").is(key));
        CounterDocument existing = mongoTemplate.findOne(query, CounterDocument.class);
        if (existing == null || existing.getValue() == null || existing.getValue() < minimum) {
            Update update = new Update().set("value", minimum);
            FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true).upsert(true);
            mongoTemplate.findAndModify(query, update, options, CounterDocument.class);
        }
    }
}
