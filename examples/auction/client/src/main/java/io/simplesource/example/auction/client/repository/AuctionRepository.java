package io.simplesource.example.auction.client.repository;

import io.simplesource.example.auction.client.views.AuctionView;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "auctions", path = "auctions")
public interface AuctionRepository extends MongoRepository<AuctionView, String> {
}
