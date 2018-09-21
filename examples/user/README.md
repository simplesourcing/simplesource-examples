# User aggregate example

The User example project demonstrates several of the ways an aggregate can be 
modelled and serialized in Simple Sourcing.

The following table summarises the included scenarios.

| Serialization format | Domain format | Runner | Test |
|----|----|----|----|
| Avro | Specific generated Avro classes | `UserAvroRunner`| `UserAvroKStreamTest` |
| Avro | Custom POJO classes | `UserMappedAvroRunner`| `UserMappedAvroKStreamTest` |
| JSON | Custom POJO classes | `UserMappedJsonRunner`| `UserMappedJsonKStreamTest` |
