package com.ragin.bdd.cucumbertests.hooks;

import com.ragin.bdd.Application;
import com.ragin.bdd.cucumber.core.DatabaseExecutorService;
import com.ragin.bdd.cucumber.utils.JsonUtils;
import io.cucumber.java.Before;
import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

@ActiveProfiles("cucumberTest")
@CucumberContextConfiguration
@ContextConfiguration(
        classes = {
                Application.class,
                DatabaseExecutorService.class,
                JsonUtils.class,
                SimpleCustomUUIDMatcher.class,
                ParameterizedCustomScenarioContextMatcher.class,
                ParameterizedCustomMultiParameterMatcher.class,
                CustomDateTimeFormatter.class
        },
        loader = SpringBootContextLoader.class
)
@ConfigurationPropertiesScan("com.ragin.bdd.cucumber.config")
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.main.allow-bean-definition-overriding=true"
)
@SuppressWarnings("squid:S3577")
public class CreateContextHooks {
    private final String POSTGRES_IMAGE = "postgres:latest";
    private final String DOCKER_NETWORK_NAME = "bdd_cucumber_pg";
    private final Network DOCKER_NETWORK = Network.newNetwork();

    @Container
    private final GenericContainer<?> dbContainer = new PostgreSQLContainer<>(POSTGRES_IMAGE)
        .withNetwork(DOCKER_NETWORK)
        .withNetworkAliases(DOCKER_NETWORK_NAME);

    @Test
    @Ignore("Empty test to avoid java.lang.Exception: No runnable methods.")
    public void emptyTest(){
        // See "http://sqa.fyicenter.com/FAQ/JUnit/Can_You_Explain_the_Exception_No_runnable_meth.html" for more info.
    }

    @Before
    public void springDummyForConfiguration(){
        // Dummy method so cucumber will recognize this class and use its context configuration.
    }
}
