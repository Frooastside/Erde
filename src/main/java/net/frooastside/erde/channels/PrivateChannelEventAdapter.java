package net.frooastside.erde.channels;

import java.util.stream.Stream;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.frooastside.erde.Erde;
import org.jetbrains.annotations.NotNull;

public class PrivateChannelEventAdapter extends ListenerAdapter {

  private static final String LOCKED_PRIVATE_CHANNEL_MARKER = "🔒";
  private static final String UNLOCKED_PRIVATE_CHANNEL_MARKER = "🔓";
  private final Erde erde;

  public PrivateChannelEventAdapter(Erde erde) {
    this.erde = erde;
  }

  @Override
  public void onGuildJoin(@NotNull GuildJoinEvent event) {
    createChannels(event.getGuild());
  }

  @Override
  public void onGuildReady(@NotNull GuildReadyEvent event) {
    createChannels(event.getGuild());
  }

  public void createChannels(Guild guild) {
    boolean lockedPrivateChannelCreatorExists = false;
    boolean unlockedPrivateChannelCreatorExists = false;
    for (VoiceChannel voiceChannel : guild.getVoiceChannels()) {
      if (voiceChannel.getName().contains(LOCKED_PRIVATE_CHANNEL_MARKER)) {
        lockedPrivateChannelCreatorExists = true;
      } else if (voiceChannel.getName().contains(UNLOCKED_PRIVATE_CHANNEL_MARKER)) {
        unlockedPrivateChannelCreatorExists = true;
      }
    }
    if (!lockedPrivateChannelCreatorExists) {
      guild.createVoiceChannel(LOCKED_PRIVATE_CHANNEL_MARKER + " create channel").queue();
    }
    if (!unlockedPrivateChannelCreatorExists) {
      guild.createVoiceChannel(UNLOCKED_PRIVATE_CHANNEL_MARKER + " create channel").queue();
    }
  }

  @Override
  public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
    if(event.getChannelLeft() != null && event.getChannelJoined() != null) {
      PrivateChannelGroup privateChannelGroup = PrivateChannelGroup.VOICE_CHANNELS.get(event.getChannelLeft().getIdLong());
      if (privateChannelGroup != null) {
        VoiceChannel voiceChannel = privateChannelGroup.voiceChannel();
        if (voiceChannel != null) {
          if (voiceChannel.getMembers().isEmpty()) {
            PrivateChannelGroup.delete(privateChannelGroup);
          } else if (event.getMember().getIdLong() == privateChannelGroup.ownerId()) {
            PrivateChannelGroup.promote(privateChannelGroup, voiceChannel.getMembers().get(0).getIdLong());
          }
        }
      }
      AudioChannel channelJoined = event.getChannelJoined();
      if (Stream.of(
        LOCKED_PRIVATE_CHANNEL_MARKER,
        UNLOCKED_PRIVATE_CHANNEL_MARKER).anyMatch(createChannelName -> channelJoined.getName().contains(createChannelName))) {
        PrivateChannelGroup.create(erde, event.getGuild(), event.getMember().getUser(), event.getChannelJoined().getName().contains(LOCKED_PRIVATE_CHANNEL_MARKER));
      }
    }else if(event.getChannelJoined() != null) {
      if (Stream.of(
        LOCKED_PRIVATE_CHANNEL_MARKER,
        UNLOCKED_PRIVATE_CHANNEL_MARKER).anyMatch(createChannelName -> event.getChannelJoined().getName().contains(createChannelName))) {
        PrivateChannelGroup.create(erde, event.getGuild(), event.getMember().getUser(), event.getChannelJoined().getName().contains(LOCKED_PRIVATE_CHANNEL_MARKER));
      }
    }else if(event.getChannelLeft() != null) {
      PrivateChannelGroup privateChannelGroup = PrivateChannelGroup.VOICE_CHANNELS.get(event.getChannelLeft().getIdLong());
      if (privateChannelGroup != null) {
        VoiceChannel voiceChannel = privateChannelGroup.voiceChannel();
        if (voiceChannel != null) {
          if (voiceChannel.getMembers().isEmpty()) {
            PrivateChannelGroup.delete(privateChannelGroup);
          } else if (event.getMember().getIdLong() == privateChannelGroup.ownerId()) {
            PrivateChannelGroup.promote(privateChannelGroup, voiceChannel.getMembers().get(0).getIdLong());
          }
        }
      }
      AudioChannel channelJoined = event.getChannelJoined();
      if (channelJoined != null) {
        if (Stream.of(
          LOCKED_PRIVATE_CHANNEL_MARKER,
          UNLOCKED_PRIVATE_CHANNEL_MARKER).anyMatch(createChannelName -> channelJoined.getName().contains(createChannelName))) {
          PrivateChannelGroup.create(erde, event.getGuild(), event.getMember().getUser(), event.getChannelJoined().getName().contains(LOCKED_PRIVATE_CHANNEL_MARKER));
        }
      }
    }
  }

  @Override
  public void onGenericComponentInteractionCreate(@NotNull GenericComponentInteractionCreateEvent event) {
    if (PrivateChannelGroup.TEXT_CONTROLLERS.containsKey(event.getChannel().getIdLong())) {
      PrivateChannelGroup privateChannelGroup = PrivateChannelGroup.TEXT_CONTROLLERS.get(event.getChannel().getIdLong());
      privateChannelGroup.handleInteraction(event);
    }
  }
}
