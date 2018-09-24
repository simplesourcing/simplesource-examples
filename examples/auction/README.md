#### Running the application

From the `examples/auction` folder:

1. Start the backend dependencies in Docker
    
    ```bash
    docker-compose up
    ```
    
2. Start the backend application:
    
    ```bash
    ./run.sh
    ```
    
3. Run the front end in Docker:
   
   Change to the `examples/auction-frontend` folder:
   
   ```bash
   docker-compose up 
   ```
   
4. Open a web browser at [http://localhost:3000](http://localhost:3000)   
   

#### Test locally
A new docker compose file is added in this module to support 3 new containers postgres, Kafka connect and Confluent control center.
To run the application 

* Stop all existing kafka containers which were running from the parent compose file 
* If you are using Docker for Mac >= 1.12, Docker for Linux, or Docker for Windows 10, then please add the following lines 
      to `/etc/hosts` or `C:\Windows\System32\Drivers\etc\hosts`:
      
```
127.0.0.1   zookeeper
127.0.0.1   broker
127.0.0.1   connect
127.0.0.1   control-center
127.0.0.1   postgres
127.0.0.1   schema_registry
127.0.0.1   mongo-express
127.0.0.1   mongo
```
    * Wait until all containers start, and run the file simplesource/examples/auction/kafka_connect.sh to create the processed topic and Kafka connector instance.

#### REST APIs for write side

The example comes with a REST API, the following are supported services 

##### Account aggregate

* To create an account, do a POST request to URL `http://localhost:8080/auction-example/accounts` the following is an example
```bash
curl -X POST \
  http://localhost:8080/auction-example/accounts \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -d '{
	"accountId": "b5023d7f-e45e-44d1-8bc2-c71c77fdc8c5",
	"accountDto": {
		"userName": "Sarah Dubois",
		"funds": 1000.576
	}
}'
```
    
* To update userName of an account, you need to do a PUT request to URL `http://localhost:8080/auction-example/accounts/{accountId}` 
    
```bash
curl -X PUT \
  http://localhost:8080/auction-example/accounts/b5023d7f-e45e-44d1-8bc2-c71c77fdc8c5 \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -d '{
	"userName": "Sarah Jones"
}'
```

* To add funds to an account, use a POST request to `http://localhost:8080/auction-example/{accountId}/funds` 
```bash
curl -X POST \
  http://localhost:8080/auction-example/accounts/b5023d7f-e45e-44d1-8bc2-c71c77fdc8c5/funds \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -d '{
	"funds": 1000
}'
```

* To reserve funds for an account, use POST request to `http://localhost:8080/auction-example/accounts/{accountId}/funds/reservations`
```bash
curl -X POST \
  http://localhost:8080/auction-example/accounts/b5023d7f-e45e-44d1-8bc2-c71c77fdc8c5/funds/reservations \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -d '{
	"reservationId": "f2e12fec-413f-48d8-a64d-2b70811261b4",
	"description": "Reserve funds",
	"amount": 300
}'
```

* To cancel a reservation, you would need to use a DELETE request to `http://localhost:8080/auction-example/accounts/{accountId}/funds/reservations/{reservationId}`

* To confirm a reservation, you could use a POST request to `http://localhost:8080/auction-example/accounts/{accountId}/funds/reservations/{reservationId}` 
```bash
curl -X POST \
  http://localhost:8080/auction-example/accounts/b5023d7f-e45e-44d1-8bc2-c71c77fdc8c5/funds/reservations/f2e12fec-413f-48d8-a64d-2b70811261b4 \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -d 250
```

##### Auction aggregate

* To create auction do a POST request to endpoint http://localhost:8080/auction 
```bash
curl -X POST \
  http://localhost:8080/auction \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -d '{
	"key": "4701ecba-aed7-4df6-bfeb-6d9e11d36f5c",
	"value": {
		"creator": "Sarah Jones",
		"title": "CBD Hub auction",
		"description": "Auction for the Southern Cross Hub",
		"reservePrice": 2000000
	}
}'
```

* To update auction do a PUT request to endpoint `http://localhost:8080/auction-example/{auctionId}`
```bash
curl -X POST \
  http://localhost:8080/auction-example/4701ecba-aed7-4df6-bfeb-6d9e11d36f5c \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -d '{
	
		"creator": "Sarah Jones",
		"title": "CBD Hub updated",
		"description": "CBD Hub",
		"reservePrice": 500
	
}'
```

#### REST APIs for read side

Once you run script `kafka_connect.sh`  Kafka connector instances will be created to populate Mongo database collections with the projection of
aggregates. The following table shows the REST endpoints could be used to view the result of projection saved in Mongodb
Please note the base URI for all those endpoints is `http://localhost:8080/auction-example/projections`


| End point | Description |
| :---: | :---: |
| /accounts | Accounts projection view |
| /auctions | Auctions projection view |
| /transactions | All account transactions (added/canceled/ and confirmed reservations for accounts) |
| /accounts/{accountId} | Account details for account with passed ID | 
| /accounts/{accountId}/transactions | All transactions for specified account | 

