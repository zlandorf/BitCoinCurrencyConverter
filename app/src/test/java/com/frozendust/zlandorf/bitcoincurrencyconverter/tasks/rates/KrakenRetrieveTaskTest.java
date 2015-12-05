package com.frozendust.zlandorf.bitcoincurrencyconverter.tasks.rates;

import com.frozendust.zlandorf.bitcoincurrencyconverter.models.entities.Currency;
import com.frozendust.zlandorf.bitcoincurrencyconverter.models.entities.Pair;
import com.frozendust.zlandorf.bitcoincurrencyconverter.models.entities.Rate;
import com.frozendust.zlandorf.bitcoincurrencyconverter.services.HttpService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.matches;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class KrakenRetrieveTaskTest {

    @Mock
    private HttpService httpService;

    @Test
    public void testRetrieveTaskValidUrl() throws Exception {
        new KrakenRetrieveTask(null, httpService).retrieveRates();

        verify(httpService).request(matches("https://api\\.kraken\\.com/0/public/Ticker\\?pair=[A-Z]{8}(,[A-Z]{8})*"));
    }

    @Test
    public void testRetrieveTasksEmpty() throws Exception {
        when(httpService.request(anyString()))
            .thenReturn(null);

        List<Rate> rates = new KrakenRetrieveTask(null, httpService).retrieveRates();

        assertThat(rates).isEmpty();
    }

    @Test
    public void testRetrieveTasksSpecificPair() throws Exception {
        when(httpService.request(anyString()))
            .thenReturn("{\"error\":[],\"result\":{\"XLTCZEUR\":{\"a\":[\"3.13299\",\"28\",\"28.000\"],\"b\":[\"3.10000\",\"9\",\"9.000\"],\"c\":[\"3.13765\",\"174.04780991\"],\"v\":[\"1310.04373147\",\"1340.04373147\"],\"p\":[\"3.09882\",\"3.09146\"],\"t\":[132,133],\"l\":[\"3.05002\",\"3.05002\"],\"h\":[\"3.14648\",\"3.14648\"],\"o\":\"3.10000\"}}}");

        List<Rate> rates = new KrakenRetrieveTask(null, httpService).retrieveRates();

        assertThat(rates)
            .isNotNull()
            .isNotEmpty();
        assertThat(rates)
            .extracting("value")
            .containsExactly(3.13765);
        assertThat(rates)
            .extracting("pair")
            .containsExactly(new Pair(Currency.LTC, Currency.EUR));
    }

    @Test
    public void testRetrieveTasks() throws Exception {
        when(httpService.request(anyString()))
            .thenReturn("{\"error\":[],\"result\":{\"XLTCZEUR\":{\"a\":[\"3.13299\",\"28\",\"28.000\"],\"b\":[\"3.10000\",\"9\",\"9.000\"],\"c\":[\"3.13765\",\"174.04780991\"],\"v\":[\"1310.04373147\",\"1340.04373147\"],\"p\":[\"3.09882\",\"3.09146\"],\"t\":[132,133],\"l\":[\"3.05002\",\"3.05002\"],\"h\":[\"3.14648\",\"3.14648\"],\"o\":\"3.10000\"},\"XLTCZUSD\":{\"a\":[\"3.42245\",\"57\",\"57.000\"],\"b\":[\"3.37101\",\"10\",\"10.000\"],\"c\":[\"3.50000\",\"11.03204157\"],\"v\":[\"361.54912641\",\"361.54912641\"],\"p\":[\"3.39311\",\"3.39310\"],\"t\":[24,24],\"l\":[\"3.35001\",\"3.35001\"],\"h\":[\"3.50000\",\"3.50000\"],\"o\":\"3.36999\"},\"XXBTXLTC\":{\"a\":[\"108.38557\",\"1\",\"1.000\"],\"b\":[\"107.51314\",\"1\",\"1.000\"],\"c\":[\"108.87789\",\"1.04436373\"],\"v\":[\"17.24263580\",\"17.24263580\"],\"p\":[\"106.71346\",\"106.71092\"],\"t\":[188,188],\"l\":[\"105.51637\",\"105.51637\"],\"h\":[\"108.87789\",\"108.87789\"],\"o\":\"106.29827\"},\"XXBTZCAD\":{\"a\":[\"489.63440\",\"20\",\"20.000\"],\"b\":[\"484.73800\",\"9\",\"9.000\"],\"c\":[\"482.13200\",\"0.50000000\"],\"v\":[\"3.50000000\",\"3.50000000\"],\"p\":[\"482.50692\",\"482.50690\"],\"t\":[5,5],\"l\":[\"481.50500\",\"481.50500\"],\"h\":[\"486.70260\",\"482.96350\"],\"o\":\"486.70260\"},\"XXBTZEUR\":{\"a\":[\"335.20000\",\"10\",\"10.000\"],\"b\":[\"334.50000\",\"2\",\"2.000\"],\"c\":[\"335.14990\",\"0.14400182\"],\"v\":[\"4328.49875367\",\"4351.00464387\"],\"p\":[\"332.13358\",\"332.01492\"],\"t\":[4295,4329],\"l\":[\"326.50000\",\"326.50000\"],\"h\":[\"335.99999\",\"335.99999\"],\"o\":\"332.63790\"},\"XXBTZUSD\":{\"a\":[\"366.03406\",\"26\",\"26.000\"],\"b\":[\"363.34001\",\"8\",\"8.000\"],\"c\":[\"364.93447\",\"0.01937334\"],\"v\":[\"50.81390144\",\"50.81390144\"],\"p\":[\"362.06831\",\"362.05755\"],\"t\":[88,88],\"l\":[\"357.48405\",\"357.48405\"],\"h\":[\"365.08260\",\"365.08260\"],\"o\":\"361.29800\"}}}");

        List<Rate> rates = new KrakenRetrieveTask(null, httpService).retrieveRates();

        assertThat(rates).isNotNull().isNotEmpty();
        // number of pairs returned by the mock
        assertThat(rates).hasSize(6);
    }
}
