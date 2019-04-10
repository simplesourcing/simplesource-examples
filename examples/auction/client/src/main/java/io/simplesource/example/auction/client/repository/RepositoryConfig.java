package io.simplesource.example.auction.client.repository;

import io.simplesource.example.auction.client.views.AccountView;
import io.simplesource.example.auction.client.views.AuctionView;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;

@Configuration
public class RepositoryConfig extends RepositoryRestConfigurerAdapter {
    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
        config.exposeIdsFor(AccountView.class);
        config.exposeIdsFor(AuctionView.class);
    }
}
