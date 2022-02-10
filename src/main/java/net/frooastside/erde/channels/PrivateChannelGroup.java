package net.frooastside.erde.channels;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.Component;
import net.frooastside.erde.Erde;
import net.frooastside.erde.language.I18n;

import java.awt.*;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Random;

public class PrivateChannelGroup {

  public transient static final BiMap<Long, PrivateChannelGroup> OWNERS = HashBiMap.create();
  public transient static final BiMap<Long, PrivateChannelGroup> TEXT_CONTROLLERS = HashBiMap.create();
  public transient static final BiMap<Long, PrivateChannelGroup> VOICE_CHANNELS = HashBiMap.create();
  private static final String PRIVATE_CHANNEL_ID = "pcg::private";
  private static final String PUBLIC_CHANNEL_ID = "pcg::public";
  private transient final Erde erde;
  private final long guild;
  private final long category;
  private final long textControlChannel;
  private final long textChannel;
  private final long voiceChannel;
  private boolean locked;
  private long owner;
  private AccessRole memberRole;

  private PrivateChannelGroup(Erde erde, long owner, long guild, long category, long textControlChannel, long textChannel, long voiceChannel, boolean locked) {
    this.erde = erde;
    this.owner = owner;
    this.guild = guild;
    this.category = category;
    this.textControlChannel = textControlChannel;
    this.textChannel = textChannel;
    this.voiceChannel = voiceChannel;
    this.locked = locked;
  }

  public static void promote(PrivateChannelGroup privateChannelGroup, long user) {
    Guild guild = privateChannelGroup.guild();
    if (guild != null) {
      VoiceChannel voiceChannel = privateChannelGroup.voiceChannel();
      if (voiceChannel != null && voiceChannel.getMembers().stream().anyMatch(member -> member.getIdLong() == user)) {
        long previousOwnerId = privateChannelGroup.ownerId();
        privateChannelGroup.setOwner(user);
        OWNERS.remove(previousOwnerId);
        OWNERS.put(user, privateChannelGroup);
        Member previousOwner = guild.getMemberById(previousOwnerId);
        if (previousOwner != null) {
          PermissionOverride permissionOverride = privateChannelGroup.textControlChannel().getPermissionOverride(previousOwner);
          if (permissionOverride != null) {
            permissionOverride.delete().queue();
          }
        }
        Member nextOwner = guild.getMemberById(user);
        if (nextOwner != null) {
          privateChannelGroup.textControlChannel().createPermissionOverride(nextOwner).setAllow(Permission.VIEW_CHANNEL).queue();
        }
      }
    }
  }

  public static void create(Erde erde, Guild guild, User creator, boolean locked) {
    Random random = new Random();

    String[] categoryNames = I18n.get("private-channels.category").split(":");
    guild.createCategory(categoryNames[random.nextInt(categoryNames.length)].replace("[NAME]", creator.getName()))
      .addRolePermissionOverride(guild.getPublicRole().getIdLong(), null, EnumSet.of(
        Permission.VIEW_CHANNEL,
        Permission.MESSAGE_SEND,
        Permission.VOICE_CONNECT))
      .queue(category ->
        guild.createTextChannel(I18n.get("private-channels.control"), category)
          .addRolePermissionOverride(guild.getPublicRole().getIdLong(), null, EnumSet.of(
            Permission.VIEW_CHANNEL,
            Permission.MESSAGE_SEND,
            Permission.MESSAGE_ADD_REACTION,
            Permission.CREATE_PUBLIC_THREADS,
            Permission.CREATE_PRIVATE_THREADS))
          .addMemberPermissionOverride(creator.getIdLong(), EnumSet.of(Permission.VIEW_CHANNEL), null)
          .queue(textControlChannel -> {
            String[] textChannelNames = I18n.get("private-channels.text").split(":");
            guild.createTextChannel(textChannelNames[random.nextInt(textChannelNames.length)].replace("[NAME]", creator.getName()), category)
              .queue(textChannel -> {
                String[] voiceChannelNames = I18n.get("private-channels.voice").split(":");
                guild.createVoiceChannel(voiceChannelNames[random.nextInt(voiceChannelNames.length)].replace("[NAME]", creator.getName()), category)
                  .queue(voiceChannel -> initializeChannelGroup(erde, creator, guild, category, textControlChannel, textChannel, voiceChannel, locked));
              });
          }));
  }

  protected static void initializeChannelGroup(Erde erde, User creator, Guild guild, Category category, TextChannel textControlChannel, TextChannel textChannel, VoiceChannel voiceChannel, boolean locked) {
    PrivateChannelGroup privateChannelGroup = new PrivateChannelGroup(
      erde,
      creator.getIdLong(),
      guild.getIdLong(),
      category.getIdLong(),
      textControlChannel.getIdLong(),
      textChannel.getIdLong(),
      voiceChannel.getIdLong(),
      locked);
    OWNERS.put(privateChannelGroup.ownerId(), privateChannelGroup);
    TEXT_CONTROLLERS.put(privateChannelGroup.textControlChannelId(), privateChannelGroup);
    VOICE_CHANNELS.put(privateChannelGroup.voiceChannelId(), privateChannelGroup);
    privateChannelGroup.initializeAccess();
    privateChannelGroup.guild().moveVoiceMember(privateChannelGroup.owner(), privateChannelGroup.voiceChannel()).queue();
  }

  public static void delete(PrivateChannelGroup privateChannelGroup) {
    OWNERS.remove(privateChannelGroup.ownerId());
    TEXT_CONTROLLERS.remove(privateChannelGroup.textControlChannelId());
    VOICE_CHANNELS.remove(privateChannelGroup.voiceChannelId());
    Guild guild = privateChannelGroup.guild();
    if (guild != null) {
      try {
        Objects.requireNonNull(guild.getTextChannelById(privateChannelGroup.textControlChannelId())).delete()
          .queue(unused_1 -> Objects.requireNonNull(guild.getTextChannelById(privateChannelGroup.textChannelId())).delete()
            .queue(unused_2 -> Objects.requireNonNull(guild.getVoiceChannelById(privateChannelGroup.voiceChannelId())).delete()
              .queue(unused_3 -> Objects.requireNonNull(guild.getCategoryById(privateChannelGroup.categoryId())).delete().queue())));
        if (privateChannelGroup.memberRole != null) {
          privateChannelGroup.memberRole.delete();
        }
      } catch (NullPointerException ignored) {
      }
    }
  }

  private void initializeAccess() {
    guild().createRole()
      .setMentionable(false)
      .setHoisted(false)
      .setName("_ㅤ")
      .setColor(Color.LIGHT_GRAY).queue(role -> {
        memberRole = new AccessRole(erde, guildId(), role.getIdLong());
        memberRole.addCategory(categoryId());
        memberRole.removeTextChannel(textControlChannelId());
        memberRole.addUser(ownerId());
      });
    if (locked()) {
      lock();
    } else {
      unlock();
    }
    resetSettings();
  }

  public void lock() {
    Category category = category();
    if (category != null) {
      category.putPermissionOverride(guild().getPublicRole()).setDeny(
        Permission.VIEW_CHANNEL,
        Permission.MESSAGE_SEND,
        Permission.VOICE_CONNECT).queue(permissionOverride -> locked = true);
    }
  }

  public void unlock() {
    Category category = category();
    if (category != null) {
      category.putPermissionOverride(guild().getPublicRole()).setAllow(
          Permission.VIEW_CHANNEL,
          Permission.MESSAGE_SEND,
          Permission.VOICE_CONNECT)
        .flatMap(permissionOverride -> textControlChannel().putPermissionOverride(guild().getPublicRole()).setDeny(
          Permission.VIEW_CHANNEL,
          Permission.MESSAGE_SEND,
          Permission.MESSAGE_ADD_REACTION,
          Permission.CREATE_PUBLIC_THREADS,
          Permission.CREATE_PRIVATE_THREADS))
        .queue(permissionOverride -> locked = false);
    }
  }

  public void resetSettings() {
    textControlChannel().getHistory().retrievePast(1).queue(messages -> {
      messages.forEach(message -> message.delete().queue());
      createLockMessage();
    });
  }

  private void createLockMessage() {
    EmbedBuilder builder = new EmbedBuilder()
      .setDescription(I18n.get("private-channels.settings.privacy"))
      .setColor(Color.RED);
    textControlChannel().sendMessageEmbeds(builder.build())
      .flatMap(message -> message.editMessageComponents(ActionRow.of(
        Button.success(PUBLIC_CHANNEL_ID, I18n.get("private-channels.settings.privacy.public")),
        Button.danger(PRIVATE_CHANNEL_ID, I18n.get("private-channels.settings.privacy.private"))))).queue();
  }

  public void handleInteraction(GenericComponentInteractionCreateEvent event) {
    Component component = event.getComponent();
    if (component != null) {
      if (PUBLIC_CHANNEL_ID.equals(component.getId())) {
        if (locked()) {
          unlock();
          EmbedBuilder builder = new EmbedBuilder()
            .setDescription(I18n.get("private-channels.settings.privacy.successful"))
            .setColor(Color.GREEN);
          event.deferReply(true).addEmbeds(builder.build()).queue();
        } else {
          EmbedBuilder builder = new EmbedBuilder()
            .setDescription(I18n.get("private-channels.settings.privacy.already", I18n.get("private-channels.settings.privacy.public")))
            .setColor(Color.RED);
          event.deferReply(true).addEmbeds(builder.build()).queue();
        }
      } else if (PRIVATE_CHANNEL_ID.equals(component.getId())) {
        if (!locked()) {
          lock();
          EmbedBuilder builder = new EmbedBuilder()
            .setDescription(I18n.get("private-channels.settings.privacy.successful"))
            .setColor(Color.GREEN);
          event.deferReply(true).addEmbeds(builder.build()).queue();
        } else {
          EmbedBuilder builder = new EmbedBuilder()
            .setDescription(I18n.get("private-channels.settings.privacy.already", I18n.get("private-channels.settings.privacy.private")))
            .setColor(Color.RED);
          event.deferReply(true).addEmbeds(builder.build()).queue();
        }
      }
    }
  }

  public long guildId() {
    return guild;
  }

  public Guild guild() {
    return erde.api().getGuildById(guild);
  }

  public long categoryId() {
    return category;
  }

  public Category category() {
    return guild() != null ? guild().getCategoryById(category) : null;
  }

  public long textControlChannelId() {
    return textControlChannel;
  }

  public TextChannel textControlChannel() {
    return guild() != null ? guild().getTextChannelById(textControlChannel) : null;
  }

  public long textChannelId() {
    return textChannel;
  }

  public TextChannel textChannel() {
    return guild() != null ? guild().getTextChannelById(textChannel) : null;
  }

  public long voiceChannelId() {
    return voiceChannel;
  }

  public VoiceChannel voiceChannel() {
    return guild() != null ? guild().getVoiceChannelById(voiceChannel) : null;
  }

  public long ownerId() {
    return owner;
  }

  public void setOwner(long owner) {
    this.owner = owner;
  }

  public Member owner() {
    return guild() != null ? guild().getMemberById(owner) : null;
  }

  public boolean locked() {
    return locked;
  }
}
