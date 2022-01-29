package net.frooastside.erde.messages;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.frooastside.erde.Erde;

import java.util.HashMap;
import java.util.Map;

public abstract class ChatHandler {

  private final Erde erde;
  private final boolean privateMessagesAllowed;

  private final Map<String, Command> commands = new HashMap<>();

  protected ChatHandler(Erde erde, boolean privateMessagesAllowed) {
    this.erde = erde;
    this.privateMessagesAllowed = privateMessagesAllowed;
  }

  public abstract void registerCommands();

  public void handleChatEvent(MessageReceivedEvent event) {
    String token = event.getMessage().getContentRaw().split(" ")[0];
    for (Map.Entry<String, Command> entry : commands.entrySet()) {
      if (token.toLowerCase().equalsIgnoreCase((erde.prefix() + entry.getKey()))) {
        if (event.getMessage().getContentRaw().length() >= token.length() + 1) {
          String argumentsAsString = event.getMessage().getContentRaw().substring(token.length() + 1);
          entry.getValue().execute(event, argumentsAsString, argumentsAsString.split(" "));
        } else {
          entry.getValue().execute(event, "", new String[]{});
        }
        return;
      }
    }
  }

  public Erde erde() {
    return erde;
  }

  public boolean privateMessagesAllowed() {
    return privateMessagesAllowed;
  }

  public Map<String, Command> commands() {
    return commands;
  }

  protected interface Command {

    void execute(MessageReceivedEvent event, String argumentString, String[] arguments);

  }

}
