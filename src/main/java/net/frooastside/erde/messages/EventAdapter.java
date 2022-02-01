package net.frooastside.erde.messages;

import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.frooastside.erde.Erde;
import net.frooastside.erde.Feedback;
import net.frooastside.erde.language.I18n;
import net.frooastside.erde.music.MusicChatHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventAdapter extends ListenerAdapter {

  private static final List<String> COMMAND_CHANNEL_NAMES = Arrays.asList("boot", "Boot", "commands", "Commands", "bot", "Bot", "🤖");
  private final List<ChatHandler> chatHandlers = new ArrayList<>();
  private final Erde erde;

  public EventAdapter(Erde erde) {
    this.erde = erde;
    initialize();
  }

  public void initialize() {
    chatHandlers.add(new MusicChatHandler(erde));
    chatHandlers.forEach(ChatHandler::registerCommands);
  }

  @Override
  public void onReady(@NotNull ReadyEvent event) {

  }

  @Override
  public void onGuildJoin(@NotNull GuildJoinEvent event) {
    for (TextChannel textChannel : event.getGuild().getTextChannels()) {
      if (COMMAND_CHANNEL_NAMES.stream().anyMatch(commandChannelName -> textChannel.getName().contains(commandChannelName))) {
        erde.textChannels().put(event.getGuild().getIdLong(), textChannel);
        return;
      }
    }
    event.getGuild().createTextChannel("🤖-bot-commands").queue(textChannel -> erde.textChannels().put(event.getGuild().getIdLong(), textChannel));
  }

  @Override
  public void onGuildReady(@NotNull GuildReadyEvent event) {
    for (TextChannel textChannel : event.getGuild().getTextChannels()) {
      if (COMMAND_CHANNEL_NAMES.stream().anyMatch(commandChannelName -> textChannel.getName().contains(commandChannelName))) {
        erde.textChannels().put(event.getGuild().getIdLong(), textChannel);
        return;
      }
    }
    event.getGuild().createTextChannel("🤖-bot-commands").queue(textChannel -> erde.textChannels().put(event.getGuild().getIdLong(), textChannel));
  }

  @Override
  public void onMessageReceived(@NotNull MessageReceivedEvent event) {
    if (event.getChannelType().isGuild()) {
      if (event.getMessage().getContentRaw().startsWith(erde.prefix())) {
        if (event.getChannel() == erde.textChannel(event.getGuild().getIdLong())) {
          chatHandlers.forEach(chatHandler -> chatHandler.handleChatEvent(event));
        } else {
          event.getMessage().delete().queue();
          Erde.sendEmbed(erde.textChannel(event.getGuild().getIdLong()), new Feedback(I18n.get("error.wrong-channel", event.getAuthor().getAsMention()), Feedback.Status.NEGATIVE));
        }
      }
    } else {
      if (event.getMessage().getContentRaw().startsWith(erde.prefix())) {
        chatHandlers.stream().filter(ChatHandler::privateMessagesAllowed).forEach(chatHandler -> chatHandler.handleChatEvent(event));
      }
    }
  }
}
