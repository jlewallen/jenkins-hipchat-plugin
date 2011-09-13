package jenkins.plugins.hipchat;

public interface HipChatService {

   public void publish(String message);

   public void publish(String message, String color);

   public void rooms();

   public enum MessageColor {
      YELLOW, RED, GREEN, PURPLE, RANDOM
   }

}
