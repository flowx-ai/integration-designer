package ai.flowx.integration.service;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import javax.annotation.PostConstruct;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Slf4j
@Component
public class EventService {

    private final MongoTemplate mongoTemplate;


    @PostConstruct
    public void watchEvents() {
        log.debug("Watching events from MongoDB");
        MongoCollection<Document> collection = mongoTemplate.getCollection("integration_systems");


        CompletableFuture.runAsync(() -> {
            log.debug("Thread started");
            collection.watch().forEach((ChangeStreamDocument<Document> change) -> {
                Document fullDocument = change.getFullDocument();

                log.debug("!!!!!!!!!!!!!!!!!!!");
                log.debug("Received event: {}", fullDocument);

            });
        });

    }

}
