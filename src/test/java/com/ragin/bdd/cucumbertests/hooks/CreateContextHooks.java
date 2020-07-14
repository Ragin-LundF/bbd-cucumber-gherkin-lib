package com.ragin.bdd.cucumbertests.hooks;

import com.ragin.bdd.Application;
import com.ragin.bdd.cucumber.core.DatabaseExecutorService;
import io.cucumber.java.Before;
import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ActiveProfiles("cucumberTest")
@CucumberContextConfiguration
@ContextConfiguration(
        classes = {
                Application.class,
                DatabaseExecutorService.class
        },
        loader = SpringBootContextLoader.class
)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.main.allow-bean-definition-overriding=true"
)
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
