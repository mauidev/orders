package com.overstock.test;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import javax.ws.rs.core.Application;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.overstock.orders.OrdersApplication;
import com.overstock.orders.Transaction;

public class BasicTest extends JerseyTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    protected Application configure() {
        return new OrdersApplication();
    }

    @Test
    public void script() throws Exception {

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {

            postSellOrder(httpclient, 10, new BigDecimal(15));
            postSellOrder(httpclient, 10, new BigDecimal(13));

            postBuyOrder(httpclient, 10, new BigDecimal(7));
            postBuyOrder(httpclient, 10, new BigDecimal(9.5));

            String result = book(httpclient);
            String expected = "{\"buys\":[{\"qty\":10,\"prc\":9.5},{\"qty\":10,\"prc\":7}],\"sells\":[{\"qty\":10,\"prc\":13},{\"qty\":10,\"prc\":15}]}";
            assertEquals(expected, result);

            // sell 5
            postSellOrder(httpclient, 5, new BigDecimal(9.5));
            result = book(httpclient);
            expected = "{\"buys\":[{\"qty\":5,\"prc\":9.5},{\"qty\":10,\"prc\":7}],\"sells\":[{\"qty\":10,\"prc\":13},{\"qty\":10,\"prc\":15}]}";
            assertEquals(expected, result);

            // buy 6
            postBuyOrder(httpclient, 6, new BigDecimal(13));
            result = book(httpclient);
            expected = "{\"buys\":[{\"qty\":5,\"prc\":9.5},{\"qty\":10,\"prc\":7}],\"sells\":[{\"qty\":4,\"prc\":13},{\"qty\":10,\"prc\":15}]}";

            // sell 7
            postSellOrder(httpclient, 7, new BigDecimal(7));
            result = book(httpclient);
            expected = "{\"buys\":[{\"qty\":8,\"prc\":7}],\"sells\":[{\"qty\":4,\"prc\":13},{\"qty\":10,\"prc\":15}]}";

            // sell 12
            postSellOrder(httpclient, 12, new BigDecimal(6));
            result = book(httpclient);
            expected = "{\"buys\":[],\"sells\":[{\"qty\":4,\"prc\":6},{\"qty\":4,\"prc\":13},{\"qty\":10,\"prc\":15}]}";

        }

    }

    @Test
    public void matchTest_sell() throws Exception {

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {

            postSellOrder(httpclient, 10, new BigDecimal(15));
            postSellOrder(httpclient, 10, new BigDecimal(13));

            postBuyOrder(httpclient, 10, new BigDecimal(7));
            postBuyOrder(httpclient, 10, new BigDecimal(9.5));

            // book
            book(httpclient);

            postSellOrder(httpclient, 5, new BigDecimal(9.5));
            String result = book(httpclient);

            String expected = "{\"buys\":[{\"qty\":5,\"prc\":9.5},{\"qty\":10,\"prc\":7}],\"sells\":[{\"qty\":10,\"prc\":13},{\"qty\":10,\"prc\":15}]}";
            assertEquals(expected, result);

        }

    }

    @Test
    public void matchTest_delete() throws Exception {

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {

            postBuyOrder(httpclient, 10, new BigDecimal(7));
            postBuyOrder(httpclient, 10, new BigDecimal(9.5));

            // book
            book(httpclient);

            postSellOrder(httpclient, 10, new BigDecimal(9.5));
            String result = book(httpclient);

            String expected = "{\"buys\":[{\"qty\":10,\"prc\":7}],\"sells\":[]}";
            assertEquals(expected, result);

        }

    }

    @Test
    public void matchTest_deleteAll() throws Exception {

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {

            postBuyOrder(httpclient, 10, new BigDecimal(7));
            postBuyOrder(httpclient, 10, new BigDecimal(9.5));

            // book
            book(httpclient);

            postSellOrder(httpclient, 20, new BigDecimal(9.5));
            String result = book(httpclient);

            String expected = "{\"buys\":[],\"sells\":[]}";
            assertEquals(expected, result);

        }

    }

    @Test
    public void matchTest_deletePartial() throws Exception {

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {

            postBuyOrder(httpclient, 10, new BigDecimal(7));
            postBuyOrder(httpclient, 10, new BigDecimal(9.5));
            postBuyOrder(httpclient, 10, new BigDecimal(13));

            // book
            book(httpclient);

            postSellOrder(httpclient, 26, new BigDecimal(9.5));
            String result = book(httpclient);

            String expected = "{\"buys\":[{\"qty\":4,\"prc\":7}],\"sells\":[]}";
            assertEquals(expected, result);

        }

    }

    @Test
    public void matchTest_deleteWithRemainder() throws Exception {

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {

            postBuyOrder(httpclient, 10, new BigDecimal(7));
            postBuyOrder(httpclient, 10, new BigDecimal(9.5));

            // book
            book(httpclient);

            postSellOrder(httpclient, 25, new BigDecimal(15));
            String result = book(httpclient);

            String expected = "{\"buys\":[],\"sells\":[{\"qty\":5,\"prc\":15}]}";
            assertEquals(expected, result);
        }

    }

    private String book(CloseableHttpClient httpClient) throws Exception {

        HttpGet httpGet = new HttpGet("http://localhost:9998/book");
        httpGet.setHeader("Accept", "application/json");

        CloseableHttpResponse response = httpClient.execute(httpGet);
        String json = EntityUtils.toString(response.getEntity());
        System.out.println("result from book: " + json);

        return json;

    }

    private void postBuyOrder(CloseableHttpClient httpClient, int qty, BigDecimal price) throws Exception {

        HttpPost httpPost = new HttpPost("http://localhost:9998/buy");
        httpPost.setHeader("Content-type", "application/json");

        Transaction order = new Transaction();
        order.setQty(qty);
        order.setPrice(price);

        String json = mapper.writeValueAsString(order);

        httpPost.setEntity(new StringEntity(json));
        CloseableHttpResponse response = httpClient.execute(httpPost);

        int status = response.getStatusLine().getStatusCode();
        assertEquals(200, status);

    }

    private void postSellOrder(CloseableHttpClient httpClient, int qty, BigDecimal price) throws Exception {

        HttpPost httpPost = new HttpPost("http://localhost:9998/sell");
        httpPost.setHeader("Content-type", "application/json");

        Transaction order = new Transaction();
        order.setQty(qty);
        order.setPrice(price);

        String json = mapper.writeValueAsString(order);

        httpPost.setEntity(new StringEntity(json));
        CloseableHttpResponse response = httpClient.execute(httpPost);

        int status = response.getStatusLine().getStatusCode();
        assertEquals(200, status);

    }
}
