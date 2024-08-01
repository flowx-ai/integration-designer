package ai.flowx.integration.web.rest;

import ai.flowx.integration.FlowXIntegrationDesignerApp;
import ai.flowx.it.BaseRestIT;
import ai.flowx.it.containers.ContainerType;
import ai.flowx.it.containers.RequiredTestContainers;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junitpioneer.jupiter.DisableIfTestFails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest(classes = {FlowXIntegrationDesignerApp.class}, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@RequiredTestContainers(value = {ContainerType.MONGO})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisableIfTestFails
public class WorkflowNodeResourceIT extends BaseRestIT {

    private final String MOCK_FOLDER_PATH = "/web/rest/workflownode";

    @Override
    protected String getResourcePath() {
        return "/api/workflows/nodes";
    }

    @Autowired
    private MongoTemplate mongoTemplate;

}
