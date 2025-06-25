package org.fortishop.notificationservice.global;

import lombok.RequiredArgsConstructor;
import org.fortishop.notificationservice.domain.DatabaseSequence;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SequenceGenerator {

    private final MongoOperations mongoOperations;

    public long generateSequence(String sequenceName) {
        Query query = new Query(Criteria.where("_id").is(sequenceName));
        Update update = new Update().inc("seq", 1);
        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true).upsert(true);

        DatabaseSequence sequence = mongoOperations.findAndModify(query, update, options, DatabaseSequence.class);
        return sequence != null ? sequence.getSeq() : 1L;
    }
}

