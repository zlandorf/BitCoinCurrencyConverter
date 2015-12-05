package com.frozendust.zlandorf.bitcoincurrencyconverter.tasks;

import com.frozendust.zlandorf.bitcoincurrencyconverter.services.HttpService;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class HttpTaskTest {

    @Test
    public void testRequest() throws Exception {
        HttpService task = new HttpService();
        String response = task.request("http://www.google.com");
        assertNotNull("Response from google is NULL", response);
        assertFalse("Response from google is EMPTY", response.isEmpty());
    }
}