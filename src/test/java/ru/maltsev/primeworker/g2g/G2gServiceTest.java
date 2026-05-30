package ru.maltsev.primeworker.g2g;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import ru.maltsev.primeworker.g2g.dto.G2gOfferDto;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class G2gServiceTest {

    @Test
    void getOffersAddsRequestTimeout() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        stubSend(httpClient, response(
                200,
                """
                        {
                          "payload": {
                            "results": [
                              {
                                "username": "seller",
                                "display_price": "1.23"
                              }
                            ]
                          }
                        }
                        """
        ));
        G2gService service = new G2gService(httpClient, new ObjectMapper());

        List<G2gOfferDto> offers = service.getOffers();

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient).send(
                requestCaptor.capture(),
                ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()
        );
        assertEquals(Duration.ofSeconds(10), requestCaptor.getValue().timeout().orElseThrow());
        assertEquals(
                "https://sls.g2g.com/offer/search?seo_term=path-of-exile-2-currency&filter_attr=lgc_27013_platform:lgc_27013_platform_62230%7Clgc_27013_tier:lgc_27013_tier_54399&sort=lowest_price&page_size=20&group=0&currency=USD&country=DE&v=v2",
                requestCaptor.getValue().uri().toString()
        );
        assertEquals(1, offers.size());
        assertEquals(new BigDecimal("1.23"), offers.getFirst().getPriceUsd());
    }

    @Test
    void getOffersThrowsRuntimeExceptionOnHttpTimeout() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        when(httpClient.send(
                any(HttpRequest.class),
                ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()
        ))
                .thenThrow(new HttpTimeoutException("timeout"));
        G2gService service = new G2gService(httpClient, new ObjectMapper());

        RuntimeException exception = assertThrows(RuntimeException.class, service::getOffers);

        assertTrue(exception.getMessage().contains("Failed to fetch G2G offers"));
    }

    @Test
    void getOffersThrowsRuntimeExceptionOnErrorStatus() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        stubSend(httpClient, response(503, "Service Unavailable"));
        G2gService service = new G2gService(httpClient, new ObjectMapper());

        RuntimeException exception = assertThrows(RuntimeException.class, service::getOffers);

        assertTrue(exception.getCause().getMessage().contains("G2G returned HTTP status 503"));
    }

    private void stubSend(HttpClient httpClient, HttpResponse<String> response) throws Exception {
        when(httpClient.send(
                any(HttpRequest.class),
                ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()
        ))
                .thenReturn(response);
    }

    @SuppressWarnings("unchecked")
    private HttpResponse<String> response(int statusCode, String body) {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(statusCode);
        when(response.body()).thenReturn(body);
        return response;
    }
}
