package jenkins.plugins.hipchat;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;

import java.io.IOException;
import java.util.logging.Logger;

@SuppressWarnings({ "unchecked" })
public class HipChatNotifier extends Notifier {

   private static final Logger logger = Logger.getLogger(HipChatNotifier.class.getName());

   @Extension
   public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

   private String jenkinsUrl;
   private String authToken;
   private String roomId;

   /**
    * Used by the config.jelly
    */
   public String getProjectSpecificRoomId() {
      if(DESCRIPTOR.getRoom().equals(roomId)) {
         return null;
      }
      else {
         return roomId;
      }
   }

   public String getRoomId() {
      return roomId;
   }

   public String getAuthToken() {
      return authToken;
   }

   public String getJenkinsUrl() {
      return jenkinsUrl;
   }

   public HipChatNotifier() {
      this(DESCRIPTOR.getToken(), DESCRIPTOR.getRoom(), DESCRIPTOR.getJenkinsUrl());
   }

   public HipChatNotifier(String token, String roomId, String jenkinsUrl) {
      super();
      this.authToken = token;
      this.jenkinsUrl = jenkinsUrl;
      this.roomId = roomId;
   }

   @Override
   public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
      FineGrainedNotifier notifier = new ActiveNotifier(this);
      notifier.started(build);
      return super.prebuild(build, listener);
   }

   public boolean perform(Build<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
      return true;
   }

   public BuildStepMonitor getRequiredMonitorService() {
      return BuildStepMonitor.BUILD;
   }

   public HipChatService newHipChatService() {
      return new StandardHipChatService(getAuthToken(), getRoomId(), "Jenkins");
   }
}
