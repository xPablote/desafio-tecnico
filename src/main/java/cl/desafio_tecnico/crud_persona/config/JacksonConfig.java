package cl.desafio_tecnico.crud_persona.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.cloud.Timestamp;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;

@Configuration
public class JacksonConfig {
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(Timestamp.class, new com.fasterxml.jackson.databind.ser.std.StdSerializer<Timestamp>(Timestamp.class) {
            @Override
            public void serialize(Timestamp value, com.fasterxml.jackson.core.JsonGenerator gen, com.fasterxml.jackson.databind.SerializerProvider provider) throws java.io.IOException {
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                gen.writeString(sdf.format(value.toDate()));
            }
        });
        module.addDeserializer(Timestamp.class, new com.fasterxml.jackson.databind.deser.std.StdDeserializer<Timestamp>(Timestamp.class) {
            @SneakyThrows
            @Override
            public Timestamp deserialize(com.fasterxml.jackson.core.JsonParser p, com.fasterxml.jackson.databind.DeserializationContext ctxt) throws java.io.IOException {
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                return Timestamp.of(sdf.parse(p.getText()));
            }
        });
        objectMapper.registerModule(module);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }
}