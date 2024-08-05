package ai.flowx.integration.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.IndexResolver;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MongoIndexesListener {
    private final MongoTemplate mongoTemplate;

    @EventListener(ContextRefreshedEvent.class)
    public void initIndicesAfterStartup() {

        MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty> mappingContext = mongoTemplate
                .getConverter().getMappingContext();

        IndexResolver resolver = new MongoPersistentEntityIndexResolver(mappingContext);

        // consider only entities that are annotated with @Document
        mappingContext.getPersistentEntities()
                .stream()
                .filter(it -> it.isAnnotationPresent(Document.class))
                .forEach(it -> {

                    IndexOperations indexOps = mongoTemplate.indexOps(it.getType());
                    resolver.resolveIndexFor(it.getType()).forEach(indexOps::ensureIndex);
                });
    }
}
