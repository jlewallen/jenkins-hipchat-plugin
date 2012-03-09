package jenkins.plugins.hipchat;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;

import java.util.logging.Level;
import java.util.logging.Logger;

public class StandardHipChatService implements HipChatService {

   private static final Logger logger = Logger.getLogger(StandardHipChatService.class.getName());

   private String host = "api.hipchat.com";
   private String token;
   private String[] roomIds;
   private String from;

   public StandardHipChatService(String token, String roomId, String from) {
      super();
      this.token = token;
      this.roomIds = roomId.split(",");
      this.from = from;
   }

   public void publish(String message) {
      publish(message, "yellow");
   }

   public void publish(String message, String color) {
      for(String roomId : roomIds) {
         logger.info("Posting: " + from + " to " + roomId + ": " + message + " " + color);
         HttpClient client = new HttpClient();
         String url = "https://" + host + "/v1/rooms/message?auth_token=" + token;
         PostMethod post = new PostMethod(url);

         try {
            post.addParameter("from", from);
            post.addParameter("room_id", roomId);
            post.addParameter("message", message);
            post.addParameter("color", color);
            post.addParameter("notify", shouldNotify(color));
            post.getParams().setContentCharset("UTF-8");
            client.executeMethod(post);
         }
         catch(Exception e) {
            logger.log(Level.WARNING, "Error posting to HipChat", e);
         }
         finally {
            post.releaseConnection();
         }
      }
   }

   private String shouldNotify(String color) {
      return color.equalsIgnoreCase("green") ? "0" : "1";
   }

   void setHost(String host) {
      this.host = host;
   }
}
