package hudson.plugins.hipchat;

import org.apache.commons.httpclient.HttpClient;

import com.gargoylesoftware.htmlunit.HttpWebConnection;
import com.gargoylesoftware.htmlunit.WebClient;

public class HttpClientBackedWebConnection extends HttpWebConnection {
   private HttpClient client;

   public HttpClientBackedWebConnection(WebClient webClient, HttpClient client) {
      super(webClient);
      this.client = client;
   }

   @Override
   protected HttpClient getHttpClient() {
      return client;
   }
}
