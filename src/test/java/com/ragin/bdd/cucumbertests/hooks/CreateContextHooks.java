package com.ragin.bdd.cucumbertests.hooks;

import com.ragin.bdd.Application;
import com.ragin.bdd.cucumber.core.DatabaseExecutorService;
import com.ragin.bdd.cucumber.utils.JsonUtils;
import io.cucumber.java.Before;
import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

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
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.main.allow-bean-definition-overriding=true"
)
@SuppressWarnings("squid:S3577")
public class CreateContextHooks {
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
