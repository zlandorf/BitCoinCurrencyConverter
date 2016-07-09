package fr.zlandorf.currencyconverter.tasks.rates;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import fr.zlandorf.currencyconverter.models.entities.Currency;
import fr.zlandorf.currencyconverter.models.entities.Pair;
import fr.zlandorf.currencyconverter.models.entities.Rate;
import fr.zlandorf.currencyconverter.services.HttpService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.matches;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BitfinexRetrieveTaskTest {

    @Mock
    private HttpService httpService;

    @Test
    public void testRetrieveTaskValidUrl() throws Exception {
        new BitfinexRetrieveTask(null, httpService).retrieveRates();

        verify(httpService, times(3))
            .request(
                matches(String.format(
                    "https://api\\.bitfinex\\.com/v1/pubticker/(%s|%s|%s)",
                    BitfinexPair.BTC_USD.getId(),
                    BitfinexPair.LTC_USD.getId(),
                    BitfinexPair.LTC_BTC.getId()
                )));
    }

    @Test
    public void testRetrieveTasksEmpty() throws Exception {
        when(httpService.request(anyString()))
            .thenReturn(null);

        List<Rate> rates = new BitfinexRetrieveTask(null, httpService).retrieveRates();

        assertThat(rates).isEmpty();
    }

    @Test
    public void testRetrieveTasks() throws Exception {
        when(httpService.request(contains(BitfinexPair.BTC_USD.getId()))).thenReturn("{\"last_price\" : \"459.47\"}");
        when(httpService.request(contains(BitfinexPair.LTC_USD.getId()))).thenReturn("{\"last_price\" : \"3.7199\"}");
        when(httpService.request(contains(BitfinexPair.LTC_BTC.getId()))).thenReturn("{\"last_price\" : \"0.0081\"}");

        List<Rate> rates = new BitfinexRetrieveTask(null, httpService).retrieveRates();

        assertThat(rates)
            .isNotNull()
            .isNotEmpty();
        assertThat(rates)
            .extracting("value")
            .containsExactly(
                459.47,
                3.7199,
                0.0081
            );
        assertThat(rates)
            .extracting("pair")
            .containsExactly(
                new Pair(Currency.BTC, Currency.USD),
                new Pair(Currency.LTC, Currency.USD),
                new Pair(Currency.LTC, Currency.BTC)
            );
    }

}