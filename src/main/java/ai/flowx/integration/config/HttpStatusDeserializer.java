package ai.flowx.integration.config;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.http.HttpStatus;

import java.io.IOException;

public class HttpStatusDeserializer extends JsonDeserializer<HttpStatus> {
    @Override
    public HttpStatus deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        int code = Integer.parseInt(jsonParser.getText());
        return HttpStatus.resolve(code);
    }
}
