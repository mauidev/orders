package com.overstock.orders;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

public class OrdersApplication extends ResourceConfig {

    public OrdersApplication() {

        this.packages("com.overstock.orders");

        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(MatchEngine.class).to(MatchEngine.class);

            }
        });

    }
}
