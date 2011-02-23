package hudson.plugins.hipchat;

import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

public class DescriptorImpl extends BuildStepDescriptor<Publisher> {
   private boolean enabled = false;
   private String token;
   private String room;
   private String hudsonUrl;

   public DescriptorImpl() {
      super(HipChatNotifier.class);
      load();
   }

   public boolean isEnabled() {
      return enabled;
   }

   public String getToken() {
      return token;
   }

   public String getRoom() {
      return room;
   }

   public String getHudsonUrl() {
      return hudsonUrl;
   }

   public boolean isApplicable(Class<? extends AbstractProject> aClass) {
      return true;
   }

   /**
    * @see hudson.model.Descriptor#newInstance(org.kohsuke.stapler.StaplerRequest)
    */
   @Override
   public Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
      String projectRoom = req.getParameter("roomName");
      if(projectRoom == null || projectRoom.trim().length() == 0) {
         projectRoom = room;
      }
      try {
         return new HipChatNotifier(token, projectRoom, hudsonUrl);
      }
      catch(Exception e) {
         throw new FormException("Failed to initialize campfire notifier - check your campfire notifier configuration settings", e, "");
      }
   }

   @Override
   public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
      token = req.getParameter("hipchatToken");
      room = req.getParameter("hipchatRoom");
      hudsonUrl = req.getParameter("hipchatHudsonUrl");
      if(hudsonUrl != null && !hudsonUrl.endsWith("/")) {
         hudsonUrl = hudsonUrl + "/";
      }
      try {
         new HipChatNotifier(token, room, hudsonUrl);
      }
      catch(Exception e) {
         throw new FormException("Failed to initialize campfire notifier - check your global campfire notifier configuration settings", e, "");
      }
      save();
      return super.configure(req, json);
   }

   /**
    * @see hudson.model.Descriptor#getDisplayName()
    */
   @Override
   public String getDisplayName() {
      return "HipChat Notification";
   }

   /**
    * @see hudson.model.Descriptor#getHelpFile()
    */
   @Override
   public String getHelpFile() {
      return "/plugin/hipchat/help.html";
   }
}
