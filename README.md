## Apicurio avro serde for kafka-ui 
simple serde implementation with apicurio avro serde for kafka ui
in case you have multiple message type per topic(kafka ui supports only single type for topic)

# Kafka ui properties example example
```
kafka.clusters.0.defaultValueSerde: Apicurio-avro-serde
kafka.clusters.0.serde.0.name: Apicurio-avro-serde
kafka.clusters.0.serde.0.filePath: /apicurio-avro-serde/kafka-ui-apicurio-avro-serde-jar-with-dependencies.jar
kafka.clusters.0.serde.0.className: com.github.sunmeplz.kafka.ui.serdes.apicurio.avro.ApicurioAvroSerde
kafka.clusters.0.serde.0.properties.apicurio.registry.url: "http://schema-registry:8080/apis/registry/v2"
kafka.clusters.0.serde.0.properties.apicurio.registry.artifact-resolver-strategy: "io.apicurio.registry.serde.avro.strategy.QualifiedRecordIdStrategy"

```