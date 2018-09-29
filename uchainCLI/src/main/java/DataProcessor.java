import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;

public class DataProcessor {
    static ObjectMapper mapper;

    static{
        mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    public static String  JsonMapperTo(Object object) throws IOException {
        String json = mapper.writeValueAsString(object);
        return json;
    }

    public static <T> T JsonMapperFrom(String content, Class<T> valueType) throws IOException{
        T object = mapper.readValue(content, valueType);
        return object;
    }
}
