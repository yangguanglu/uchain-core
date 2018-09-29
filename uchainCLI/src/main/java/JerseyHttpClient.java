import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.filter.LoggingFilter;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class JerseyHttpClient {

    static Client client = ClientBuilder.newClient(new ClientConfig().register(LoggingFilter.class));

    static String url = "http://localhost:1943/";

    public static String getWithUrl(String path){
        WebTarget webTarget = client.target(url + path);
        Invocation.Builder builder = webTarget.request(MediaType.TEXT_PLAIN);
        Response response = builder.get();
        return response.readEntity(String.class);
    }
}
