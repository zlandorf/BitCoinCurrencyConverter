package fr.zlandorf.currencyconverter.tasks.rates;

import fr.zlandorf.currencyconverter.models.entities.Currency;
import fr.zlandorf.currencyconverter.models.entities.Pair;
import fr.zlandorf.currencyconverter.models.entities.Rate;
import fr.zlandorf.currencyconverter.models.exchanges.Exchange;
import fr.zlandorf.currencyconverter.models.exchanges.Yahoo;
import fr.zlandorf.currencyconverter.services.HttpService;

import org.junit.Before;
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
public class YahooRetrieveTaskTest {

    @Mock private HttpService httpService;

    private Exchange exchange;

    @Before
    public void setup() {
        exchange = new Yahoo();
    }

    @Test
    public void testRetrieveRatesValidUrl() throws Exception {
        new YahooRetrieveTask(exchange, null, httpService).retrieveRates();

        verify(httpService).request(matches("http://download\\.finance\\.yahoo\\.com/d/quotes\\.csv\\?s=[A-Z]{6}=X(,[A-Z]{6}=X)*+&f=sl1&e=\\.csv"));
    }

    @Test
    public void testRetrieveRatesEmpty() throws Exception {
        when(httpService.request(anyString()))
            .thenReturn(null);

        List<Rate> rates = new YahooRetrieveTask(exchange, null, httpService).retrieveRates();

        assertThat(rates).isEmpty();
    }

    @Test
    public void testRetrieveRates() throws Exception {
        when(httpService.request(anyString()))
            .thenReturn("\"EURUSD=X\",1.0884\n\"EURCAD=X\",1.4541\n\"EURGBP=X\",0.7202\n\"EURCNY=X\",6.9682\n\"USDCAD=X\",1.3361\n\"USDGBP=X\",0.6617\n\"USDCNY=X\",6.4025\n\"CADGBP=X\",0.4953\n\"CADCNY=X\",4.7919\n\"GBPCNY=X\",9.6751\n");

        List<Rate> rates = new YahooRetrieveTask(exchange, null, httpService).retrieveRates();

        assertThat(rates).isNotNull().isNotEmpty();
        // number of pairs returned by the mock
        assertThat(rates).hasSize(10);
    }

    @Test
    public void testRetrieveRatesSpecificPair() throws Exception {
        when(httpService.request(anyString()))
            .thenReturn("\"EURUSD=X\",1.0884");

        List<Rate> rates = new YahooRetrieveTask(exchange, null, httpService).retrieveRates();

        assertThat(rates)
            .isNotNull()
            .isNotEmpty();
        assertThat(rates)
            .extracting("value").containsExactly(1.0884);
        assertThat(rates)
            .extracting("pair").containsExactly(new Pair(Currency.EUR, Currency.USD));
    }
}