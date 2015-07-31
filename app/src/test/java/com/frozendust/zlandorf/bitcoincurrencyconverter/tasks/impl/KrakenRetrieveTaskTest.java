package com.frozendust.zlandorf.bitcoincurrencyconverter.tasks.impl;

import com.frozendust.zlandorf.bitcoincurrencyconverter.models.entities.Rate;

import junit.framework.TestCase;

import org.junit.Test;

import java.util.List;

/**
 * Created by zlandorf on 31/07/2015.
 */
public class KrakenRetrieveTaskTest extends TestCase {

    @Test
    public void testRetrieveTasks() throws Exception {
        KrakenRetrieveTask task = new KrakenRetrieveTask();
        List<Rate> rates = task.retrieveRates();
        assertNotNull(rates);
        assertFalse(rates.isEmpty());
        assertEquals(task.pairMap.keySet().size(), rates.size());
    }
}
