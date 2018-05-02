package com.overstock.orders;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
@Singleton
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class OrdersResource {

    private MatchEngine matchEngine;

    @Inject
    public OrdersResource(MatchEngine matchEngine) {
        this.matchEngine = matchEngine;
    }

    /**
     * Returns outstanding buy and sell orders.
     * 
     * @return book in json format
     * @throws Exception
     */
    @GET
    @Path("/book")
    public String book() throws Exception {
        return matchEngine.book();
    }

    /**
     * Process a buy order.
     * 
     * @param buyOrder
     * @return 200 status
     */
    @POST
    @Path("/buy")
    public Response buy(Transaction buyOrder) {
        matchEngine.buy(buyOrder);
        return Response.ok().build();
    }

    /**
     * Process a sell order.
     * 
     * @param sellOrder
     * @return 200 status
     */
    @POST
    @Path("/sell")
    public Response sell(Transaction sellOrder) {
        matchEngine.sell(sellOrder);
        return Response.ok().build();
    }

}
