import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URISyntaxException;

public class ApacheHttpClient {

    public static CloseableHttpClient httpClient = HttpClients.createDefault();
    static String url = "http://172.16.12.43:1943/";

    public static String postWithUrl(String path, String body){
        String responseJsonString = "";
        try {
            HttpPost httpPost = new HttpPost(url + path);
            httpPost.addHeader("Content-Type", "application/json");

            StringEntity entity = new StringEntity(body);

            httpPost.setEntity(entity);

            HttpResponse httpResponse = httpClient.execute(httpPost);

            responseJsonString = EntityUtils.toString(httpResponse.getEntity());

        }
        catch (IOException e){
            e.printStackTrace();
        }
        return responseJsonString;
    }

    public static String postWithUrl(String path){
        String response = "";
        try {
            HttpPost post = new HttpPost(url + path);
            CloseableHttpResponse httpResponse = httpClient.execute(post);
            response = EntityUtils.toString(httpResponse.getEntity());
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return response;
    }

    public static String getWithUrl(String path){
        String response = "";
        try {
            System.out.println(url+path);
            HttpGet get = new HttpGet(url + path);
            CloseableHttpResponse httpResponse = httpClient.execute(get);
            HttpEntity entity = httpResponse.getEntity();
            response = EntityUtils.toString(entity, "utf-8");
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return response;
    }

    public static String getWithUrl(String path, String params){
        System.out.println("*******************");
        System.out.println("path:" + path + "params:" + params);
        String response = "";
        try {
            URIBuilder uriBuilder = new URIBuilder(url + path);
            uriBuilder.addParameter("query", params);
            HttpGet httpGet = new HttpGet(uriBuilder.build());
            httpGet.addHeader("Content-Type", "application/json");
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);

            HttpEntity httpEntity = httpResponse.getEntity();
            response = EntityUtils.toString(httpEntity, "utf-8");
        }
        catch (URISyntaxException e){
            e.printStackTrace();
        }

        catch (IOException e){
            e.printStackTrace();
        }
        return response;
    }
}
