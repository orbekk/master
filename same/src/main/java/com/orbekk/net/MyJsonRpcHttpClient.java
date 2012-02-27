package com.orbekk.net;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.jsonrpc4j.JsonRpcClient;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;

/**
 * This class is horrible. :S
 * 
 * We extend JsonRpcHttpClient but try to override everything it does.
 */
public class MyJsonRpcHttpClient extends JsonRpcHttpClient {
    Logger logger = LoggerFactory.getLogger(getClass());
    private URL serviceUrl;
    private JsonRpcClient rpcClient;
    private HttpClient httpClient;

    public MyJsonRpcHttpClient(URL serviceUrl, int connectionTimeout,
            int readTimeout) {
        super(null);
        httpClient = new DefaultHttpClient();
        HttpParams params = httpClient.getParams();
        HttpConnectionParams.setConnectionTimeout(params, connectionTimeout);
        HttpConnectionParams.setSoTimeout(params, readTimeout);
        rpcClient = new JsonRpcClient();
        this.serviceUrl = serviceUrl;
    }

    @Override
    public synchronized Object invoke(
            final String methodName, final Object[] arguments, Type returnType, 
            Map<String, String> extraHeaders)
                    throws Exception {
        EntityTemplate entity = new EntityTemplate(new ContentProducer() {
            @Override
            public void writeTo(OutputStream out) throws IOException {
                try {
                    rpcClient.invoke(methodName, arguments, out);
                } catch (Exception e) {
                    throw new IOException(e);
                }
            }
        });
        entity.setContentType("application/json-rpc");

        HttpPost post = new HttpPost(serviceUrl.toString());

        for (Map.Entry<String, String> entry : extraHeaders.entrySet()) {
            post.addHeader(entry.getKey(), entry.getValue());
        }

        post.setEntity(entity);

        HttpResponse response = httpClient.execute(post);
        HttpEntity responseEntity = response.getEntity();

        return super.readResponse(returnType, responseEntity.getContent());
    }
}
