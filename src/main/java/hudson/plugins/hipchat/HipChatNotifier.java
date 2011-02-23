package hudson.plugins.hipchat;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;

import java.io.IOException;
import java.util.logging.Logger;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;

public class HipChatNotifier extends Notifier {

   private static final Logger logger = Logger.getLogger(HipChatNotifier.class.getName());

   private String hudsonUrl;
   private String roomId;

   public String getConfiguredRoomId() {
      if(DESCRIPTOR.getRoom().equals(roomId)) {
         return null;
      }
      else {
         return roomId;
      }
   }

   @Extension
   public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

   public HipChatNotifier() throws IOException {
      super();
      initialize();
   }

   public HipChatNotifier(String token, String room, String hudsonUrl) throws IOException {
      super();
      initialize(token, room, hudsonUrl);
   }

   public BuildStepMonitor getRequiredMonitorService() {
      return BuildStepMonitor.BUILD;
   }

   private void publish(AbstractBuild<?, ?> build) throws IOException {
      Result result = build.getResult();
      String resultString = result.toString();
      String message = build.getProject().getName() + " " + build.getDisplayName() + " \"" + "" + "\": " + resultString;
      if(hudsonUrl != null && hudsonUrl.length() > 1) {
         message = message + " (" + hudsonUrl + build.getUrl() + ")";
      }
      HttpClient client = new HttpClient();
      String url = "https://api.hipchat.com/v1/rooms/message?auth_token=" + DESCRIPTOR.getToken();
      PostMethod post = new PostMethod(url);
      post.addParameter("from", "Hudson");
      post.addParameter("room_id", DESCRIPTOR.getRoom());
      post.addParameter("message", message);
      try {
         client.executeMethod(post);
      }
      finally {
         post.releaseConnection();
      }
   }

   private void initialize() throws IOException {
      initialize(DESCRIPTOR.getToken(), DESCRIPTOR.getRoom(), DESCRIPTOR.getHudsonUrl());
   }

   private void initialize(String token, String roomId, String hudsonUrl) throws IOException {
      this.hudsonUrl = hudsonUrl;
      this.roomId = roomId;
   }

   @Override
   public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
      publish(build);
      return true;
   }
}
