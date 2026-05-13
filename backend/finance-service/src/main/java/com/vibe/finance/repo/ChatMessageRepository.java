package com.vibe.finance.repo;

import com.vibe.finance.model.ChatMessageDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessageDocument, String> {
    List<ChatMessageDocument> findByUserIdOrderByTimestampAsc(String userId);
}
