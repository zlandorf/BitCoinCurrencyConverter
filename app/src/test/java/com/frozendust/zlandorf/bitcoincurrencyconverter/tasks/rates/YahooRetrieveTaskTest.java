package com.frozendust.zlandorf.bitcoincurrencyconverter.tasks.rates;

import com.frozendust.zlandorf.bitcoincurrencyconverter.models.entities.Rate;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class YahooRetrieveTaskTest {

    @Test
    public void testRetrieveRates() throws Exception {
        YahooRetrieveTask task = new YahooRetrieveTask(null);
        List<Rate> rates = task.retrieveRates();
        assertNotNull("Rates list is NULL", rates);
        assertFalse("Rates list is EMPTY", rates.isEmpty());
        assertEquals("Number of rates returned is different from number of rates requested",
                task.pairMap.keySet().size(), rates.size());
    }
}