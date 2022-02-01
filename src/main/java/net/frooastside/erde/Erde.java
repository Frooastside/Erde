package net.frooastside.erde;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.frooastside.erde.channels.PrivateChannelEventAdapter;
import net.frooastside.erde.language.I18n;
import net.frooastside.erde.language.Language;
import net.frooastside.erde.messages.EventAdapter;
import net.frooastside.erde.music.AudioClient;

import javax.security.auth.login.LoginException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class Erde {

  private final JDA api;
  private final String prefix;

  private final Map<Long, AudioClient> audioClients = new HashMap<>();
  private final Map<Long, MessageChannel> textChannels = new HashMap<>();

  private AudioPlayerManager remotePlayerManager;

  public Erde(JDA api, String prefix) {
    this.api = api;
    this.prefix = prefix;
  }

  public static void main(String[] args) {
    OptionParser parser = new OptionParser("t:p:");
    OptionSet options = parser.parse(args);
    if (!options.has("t")) {
      throw new IllegalArgumentException("I need an API Token!");
    }
    try {
      JDA jda = JDABuilder.createDefault((String) options.valueOf("t")).build();
      Erde erde = new Erde(jda, options.has("p") ? (String) options.valueOf("p") : "!");
      erde.initialize();
    } catch (LoginException exception) {
      throw new IllegalStateException(exception);
    }
  }

  public static void sendEmbed(MessageChannel messageChannel, Feedback feedback) {
    EmbedBuilder builder = new EmbedBuilder().setDescription(feedback.text()).setColor(feedback.status().color());
    sendEmbed(messageChannel, builder);
  }

  public static void sendEmbed(MessageChannel messageChannel, EmbedBuilder builder) {
    sendEmbed(messageChannel, builder.build());
  }

  public static void sendEmbed(MessageChannel messageChannel, MessageEmbed messageEmbed) {
    messageChannel.sendMessageEmbeds(messageEmbed).queue();
  }

  public void initialize() {
    Language language = Language.createFromStream(Erde.class.getResourceAsStream("de_DE.lang"));
    I18n.addLanguages(language);
    I18n.setCurrentLanguage(language);

    remotePlayerManager = new DefaultAudioPlayerManager();
    AudioSourceManagers.registerRemoteSources(remotePlayerManager);
    api.addEventListener(new EventAdapter(this), new PrivateChannelEventAdapter(this));
  }

  public GuildVoiceState fetchGuildVoiceState(User user) {
    AtomicReference<GuildVoiceState> voiceState = new AtomicReference<>();
    user.getMutualGuilds().forEach(guild -> {
      Member member = guild.getMember(user);
      if (member != null) {
        GuildVoiceState guildVoiceState = member.getVoiceState();
        if (guildVoiceState != null) {
          voiceState.set(guildVoiceState);
        }
      }
    });
    return voiceState.get();
  }

  public AudioClient provideAudioClient(GuildVoiceState voiceState) {
    if (voiceState == null) {
      return null;
    }
    long guild = voiceState.getGuild().getIdLong();
    if (audioClients.containsKey(guild)) {
      return audioClients.get(guild);
    } else {
      AudioChannel audioChannel = voiceState.getChannel();
      if (voiceState.inAudioChannel() && audioChannel != null) {
        AudioClient audioClient = new AudioClient(this, audioChannel);
        audioClients.put(guild, audioClient);
        return audioClient;
      }
    }
    return null;
  }

  public Map<Long, MessageChannel> textChannels() {
    return textChannels;
  }

  public MessageChannel textChannel(long guild) {
    return textChannels.get(guild);
  }

  public void removeClient(long guild) {
    audioClients.remove(guild);
  }

  public JDA api() {
    return api;
  }

  public String prefix() {
    return prefix;
  }

  public AudioPlayerManager remotePlayerManager() {
    return remotePlayerManager;
  }
}
