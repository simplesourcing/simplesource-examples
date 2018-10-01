# User aggregate example

The User example project demonstrates several of the ways an aggregate can be 
modelled and serialized in Simple Sourcing. 

#### Test locally
If you are using Docker for Mac >= 1.12, Docker for Linux, or Docker for Windows 10, then please add the following lines 
      to `/etc/hosts` or `C:\Windows\System32\Drivers\etc\hosts`:
      
```
127.0.0.1   zookeeper
127.0.0.1   broker
127.0.0.1   schema_registry
```

To run the user example app, you will need to start all required docker containers
Change to the `examples/user` folder:
   
   ```bash
   docker-compose up 
   ```
   
The following table summarises the included scenarios.

| Serialization format | Domain format | Runner | Test |
|----|----|----|----|
| Avro | Specific generated Avro classes | `UserAvroRunner`| `UserAvroKStreamTest` |
| Avro | Custom POJO classes | `UserMappedAvroRunner`| `UserMappedAvroKStreamTest` |
| JSON | Custom POJO classes | `UserMappedJsonRunner`| `UserMappedJsonKStreamTest` |
