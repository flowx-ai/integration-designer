package ai.flowx.integration.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.http.HttpStatus;

import java.io.IOException;

public class HttpStatusSerializer extends JsonSerializer<HttpStatus> {
    @Override
    public void serialize(HttpStatus httpStatus, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(String.valueOf(httpStatus.value()));
    }
}
