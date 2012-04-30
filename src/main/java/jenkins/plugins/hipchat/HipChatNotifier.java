package jenkins.plugins.hipchat;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.JobPropertyDescriptor;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;

@SuppressWarnings({ "unchecked" })
public class HipChatNotifier extends Notifier {

   private static final Logger logger = Logger.getLogger(HipChatNotifier.class.getName());

   private String authToken;
   private String buildServerUrl;
   private String room;
   private String sendAs;

    @Override
   public DescriptorImpl getDescriptor() {
      return (DescriptorImpl)super.getDescriptor();
   }

   public String getRoom() {
      return room;
   }

   public String getAuthToken() {
      return authToken;
   }

   public String getBuildServerUrl() {
      return buildServerUrl;
   }

   public String getSendAs() {
   return sendAs;
   }
   public void setBuildServerUrl(final String buildServerUrl) {
      this.buildServerUrl = buildServerUrl;
   }

   public void setAuthToken(final String authToken) {
      this.authToken = authToken;
   }

   public void setRoom(final String room) {
      this.room = room;
   }

   public void setSendAs(final String sendAs) {
       this.sendAs = sendAs;
   }

   @DataBoundConstructor
   public HipChatNotifier(final String authToken, final String room, String buildServerUrl, final String sendAs) {
      super();
      this.authToken = authToken;
      this.buildServerUrl = buildServerUrl;
      this.room = room;
      this.sendAs = sendAs;
   }

   public BuildStepMonitor getRequiredMonitorService() {
      return BuildStepMonitor.BUILD;
   }

   public HipChatService newHipChatService(final String room) {
      return new StandardHipChatService(getAuthToken(), room == null ? getRoom() : room, getSendAs() == null ? "Build Server" : getSendAs());
   }

   @Override
   public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
       return true;
   }

   @Extension
   public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {
      private String token;
      private String room;
      private String buildServerUrl;
      private String sendAs;

       public DescriptorImpl() {
         load();
      }

      public String getToken() {
         return token;
      }

      public String getRoom() {
         return room;
      }

      public String getBuildServerUrl() {
         return buildServerUrl;
      }

       public String getSendAs() {
           return sendAs;
       }

      public boolean isApplicable(Class<? extends AbstractProject> aClass) {
         return true;
      }

      @Override
      public HipChatNotifier newInstance(StaplerRequest sr) {
         if(token == null) token = sr.getParameter("hipChatToken");
         if(buildServerUrl == null) buildServerUrl = sr.getParameter("hipChatBuildServerUrl");
         if(room == null) room = sr.getParameter("hipChatRoom");
         if(sendAs == null) sendAs = sr.getParameter("sendAs");
         return new HipChatNotifier(token, room, buildServerUrl, sendAs);
      }

      @Override
      public boolean configure(StaplerRequest sr, JSONObject formData) throws FormException {
         token = sr.getParameter("hipChatToken");
         room = sr.getParameter("hipChatRoom");
         buildServerUrl = sr.getParameter("hipChatBuildServerUrl");
         sendAs = sr.getParameter("hipChatSendAs");
         if(buildServerUrl != null && !buildServerUrl.endsWith("/")) {
            buildServerUrl = buildServerUrl + "/";
         }
         try {
            new HipChatNotifier(token, room, buildServerUrl, sendAs);
         }
         catch(Exception e) {
            throw new FormException("Failed to initialize notifier - check your global notifier configuration settings", e, "");
         }
         save();
         return super.configure(sr, formData);
      }

      @Override
      public String getDisplayName() {
         return "HipChat Notifications";
      }
   }

   public static class HipChatJobProperty extends hudson.model.JobProperty<AbstractProject<?, ?>> {
      private String room;
      private boolean startNotification;


      @DataBoundConstructor
      public HipChatJobProperty(String room, boolean startNotification) {
         this.room = room;
         this.startNotification = startNotification;
      }

      @Exported
      public String getRoom() {
         return room;
      }

      @Exported
      public boolean getStartNotification() {
         return startNotification;
      }

      @Override
      public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
         if(startNotification) {
            Map<Descriptor<Publisher>, Publisher> map = build.getProject().getPublishersList().toMap();
            for(Publisher publisher : map.values()) {
               if(publisher instanceof HipChatNotifier) {
                  logger.info("Invoking Started...");
                  new ActiveNotifier((HipChatNotifier)publisher).started(build);
               }
            }
         }
         return super.prebuild(build, listener);
      }

      @Extension
      public static final class DescriptorImpl extends JobPropertyDescriptor {
         public String getDisplayName() {
            return "HipChat Notifications";
         }

         @Override
         public boolean isApplicable(Class<? extends Job> jobType) {
            return true;
         }

         @Override
         public HipChatJobProperty newInstance(StaplerRequest sr, JSONObject formData) throws hudson.model.Descriptor.FormException {
            return new HipChatJobProperty(sr.getParameter("hipChatProjectRoom"), sr.getParameter("hipChatStartNotification") != null);
         }
      }
   }
}
