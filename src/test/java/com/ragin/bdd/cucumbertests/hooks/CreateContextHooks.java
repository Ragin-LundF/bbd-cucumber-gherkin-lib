package com.ragin.bdd.cucumbertests.hooks;

import com.ragin.bdd.application.CucumberTestApplication;
import io.cucumber.java.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ActiveProfiles("cucumberTest")
@SpringBootTest(
        classes = CucumberTestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class CreateContextHooks {

    @Test
    @Ignore
    public void emptyTest(){
        // Empty test to avoid "java.lang.Exception: No runnable methods".
        // See "http://sqa.fyicenter.com/FAQ/JUnit/Can_You_Explain_the_Exception_No_runnable_meth.html" for more info.
    }

    @Before
    public void springDummyForConfiguration(){
        // Dummy method so cucumber will recognize this class and use its context configuration.
    }

}
