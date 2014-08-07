package jenkins.plugins.hipchat;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;

import java.util.logging.Level;
import java.util.logging.Logger;

import jenkins.model.Jenkins;
import hudson.ProxyConfiguration;

public class StandardHipChatService implements HipChatService {

    private static final Logger logger = Logger.getLogger(StandardHipChatService.class.getName());

    private String host = "api.hipchat.com";
    private String token;
    private String[] roomIds;
    private String from;

    public StandardHipChatService(String host, String token, String roomId, String from) {
        super();
        if (host != null && !String.trim().isEmpty()) {
            this.host = host;
        }
        this.token = token;
        this.roomIds = roomId.split(",");
        this.from = from;
    }

    public void publish(String message) {
        publish(message, "yellow");
    }

    public void publish(String message, String color) {
        logger.info("Publishing messages to HipChat server [" + host + "]...");
        for (String roomId : roomIds) {
            logger.info("`" + from + "` says to room `" + roomId + "`: `" + message + "` in the color `" + color + "`");
            HttpClient client = getHttpClient();
            String url = "https://" + host + "/v1/rooms/message?auth_token=" + token;
            PostMethod post = new PostMethod(url);

            try {
                post.addParameter("from", from);
                post.addParameter("room_id", roomId);
                post.addParameter("message", message);
                post.addParameter("color", color);
                post.addParameter("notify", shouldNotify(color));
                post.getParams().setContentCharset("UTF-8");
                int responseCode = client.executeMethod(post);
                String response = post.getResponseBodyAsString();
                if(responseCode != HttpStatus.SC_OK || ! response.contains("\"sent\"")) {
                    logger.log(Level.WARNING, "HipChat post may have failed. Response: " + response);
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error posting to HipChat", e);
            } finally {
                post.releaseConnection();
            }
        }
        
        logger.info("Done publishing messages to HipChat server");
    }
    
    private HttpClient getHttpClient() {
        HttpClient client = new HttpClient();
        if (Jenkins.getInstance() != null) {
            ProxyConfiguration proxy = Jenkins.getInstance().proxy;
            if (proxy != null) {
                client.getHostConfiguration().setProxy(proxy.name, proxy.port);
            }
        }
        return client;
    }

    private String shouldNotify(String color) {
        return color.equalsIgnoreCase("green") ? "0" : "1";
    }

    void setHost(String host) {
        this.host = host;
    }
}
