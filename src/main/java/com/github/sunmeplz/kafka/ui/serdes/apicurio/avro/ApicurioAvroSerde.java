package com.github.sunmeplz.kafka.ui.serdes.apicurio.avro;

import com.provectus.kafka.ui.serde.api.DeserializeResult;
import com.provectus.kafka.ui.serde.api.PropertyResolver;
import com.provectus.kafka.ui.serde.api.RecordHeaders;
import com.provectus.kafka.ui.serde.api.SchemaDescription;
import com.provectus.kafka.ui.serde.api.Serde;
import io.apicurio.registry.serde.avro.AvroSerde;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.io.JsonEncoder;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class ApicurioAvroSerde implements Serde {


    public static final String APICURIO_REGISTRY_URL = "apicurio.registry.url";
    public static final String APICURIO_REGISTRY_ARTIFACT_RESOLVER_STRATEGY = "apicurio.registry.artifact-resolver-strategy";
    private AvroSerde<GenericRecord> serde;

    @Override
    public void configure(
        PropertyResolver serdeProperties,
        PropertyResolver clusterProperties,
        PropertyResolver appProperties
    ) {
        serde = new AvroSerde<>();
        Map<String,String> serdeConfig = extractConfiguration(serdeProperties);
        serde.configure(serdeConfig,false);
    }

    private Map<String, String> extractConfiguration(PropertyResolver serdeProperties) {
        Optional<String> registryUrl = serdeProperties.getProperty(APICURIO_REGISTRY_URL, String.class);
        Optional<String> resolverStrategy = serdeProperties.getProperty(APICURIO_REGISTRY_ARTIFACT_RESOLVER_STRATEGY, String.class);
        if (registryUrl.isEmpty() || resolverStrategy.isEmpty()){
            throw new IllegalArgumentException("Misconfigured");
        }
        return Map.of(
            APICURIO_REGISTRY_URL,registryUrl.get(),
            APICURIO_REGISTRY_ARTIFACT_RESOLVER_STRATEGY,resolverStrategy.get()
        );
        //this is not works with k8s, doublequotes in properties is now allowed in config map
//        serdeProperties.getMapProperty("apicurio", String.class, String.class)
//            .ifPresent(config -> {
//                serde.configure(config, false);
//            });
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.of("Apicurio avro serde");
    }

    @Override
    public Optional<SchemaDescription> getSchema(String s, Target target) {
        return Optional.empty();
    }

    @Override
    public boolean canDeserialize(String s, Target target) {
        return true;
    }

    @Override
    public boolean canSerialize(String s, Target target) {
        return false;
    }

    @Override
    public Serializer serializer(String topic, Target target) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public Deserializer deserializer(String topic, Target target) {
        return new Deserializer() {
            @Override
            public DeserializeResult deserialize(RecordHeaders headers, byte[] data) {

                try {
                    GenericRecord genericRecord = serde.deserializer().deserialize(topic, data);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();

                    JsonEncoder jsonEncoder = EncoderFactory.get().jsonEncoder(genericRecord.getSchema(), baos,true);
                    final DatumWriter<GenericRecord> writer = new GenericDatumWriter<>(genericRecord.getSchema());
                    writer.write(genericRecord, jsonEncoder);

                    jsonEncoder.flush();
                    String result = genericRecord.getSchema().getName() + "\n" + baos.toString();
                    return new DeserializeResult(
                        result,
                        DeserializeResult.Type.JSON,
                        Collections.emptyMap()
                    );
                } catch (Throwable e) {
                    System.out.println("Unable to deserialize payload:" + e.getMessage());
                    throw new RuntimeException("Unable to deserialize payload:" + e.getMessage());
                }

            }
        };

    }
}
