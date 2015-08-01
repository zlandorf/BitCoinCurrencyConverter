package com.frozendust.zlandorf.bitcoincurrencyconverter.tasks.rates;

import com.frozendust.zlandorf.bitcoincurrencyconverter.models.entities.Rate;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class KrakenRetrieveTaskTest {

    @Test
    public void testRetrieveTasks() throws Exception {
        KrakenRetrieveTask task = new KrakenRetrieveTask(null);
        List<Rate> rates = task.retrieveRates();
        assertNotNull("Rates list is NULL", rates);
        assertFalse("Rates list is EMPTY", rates.isEmpty());
        assertEquals("Number of rates returned is different from number of rates requested",
                task.mPairMap.keySet().size(), rates.size());
    }
}
