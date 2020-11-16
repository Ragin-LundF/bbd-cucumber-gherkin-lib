package com.ragin.bdd.cucumber.utils;

import static java.util.Arrays.asList;

import java.util.stream.Stream;

import org.junit.Test;

import com.ragin.bdd.cucumbertests.hooks.CustomDateTimeFormatter;

import junit.framework.TestCase;


public class DateUtilsTest {
    
    @Test
    public void testIsValidMandatoryDate() {
        Stream.of(
                "2020.11.11",
                "2020-11-11",
                "2020-11-11 17:53",
                "2020-11-11 17:53:37",
                "2020-11-11 17:53:37.",
                "2020-11-11 17:53:37.1",
                "2020-11-11 17:53:37.12",
                "2020-11-11 17:53:37.123",
                "2020-11-11 17:53:37.1234",
                "2020-11-11 17:53:37.12345",
                "2020-11-11 17:53:37.12346",
                "2020-11-11 17:53:37.123467",
                "2020-11-11 17:53:37.1234678",
                "2020-11-11 17:53:37.12346789",
                "2020-11-11T17:53",
                "2020-11-11T17:53:37",
                "2020-11-11T17:53:37.",
                "2020-11-11T17:53:37.1",
                "2020-11-11T17:53:37.12",
                "2020-11-11T17:53:37.123",
                "2020-11-11T17:53:37.1234",
                "2020-11-11T17:53:37.12345",
                "2020-11-11T17:53:37.12346",
                "2020-11-11T17:53:37.123467",
                "2020-11-11T17:53:37.1234678",
                "2020-11-11T17:53:37.12346789")
            .forEach(sample -> TestCase.assertTrue(
                    DateUtils.isValidMandatoryDate(sample, 
                            asList(new CustomDateTimeFormatter()))));
    }
}
