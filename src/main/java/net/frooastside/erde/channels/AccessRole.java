package net.frooastside.erde.channels;

import java.util.Objects;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.frooastside.erde.Erde;

public class AccessRole {

  private final Erde erde;
  private final long guild;
  private final long role;

  public AccessRole(Erde erde, long guild, long role) {
    this.erde = erde;
    this.guild = guild;
    this.role = role;
  }

  public void addCategory(long categoryId) {
    Guild guild = guild();
    if (guild != null) {
      Category category = guild.getCategoryById(categoryId);
      if (category != null) {
        category.upsertPermissionOverride(role()).grant(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.VOICE_CONNECT).queue();
      }
    }
  }

  public void removeCategory(long categoryId) {
    Guild guild = guild();
    if (guild != null) {
      Category category = guild.getCategoryById(categoryId);
      if (category != null) {
        category.upsertPermissionOverride(role()).deny(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.VOICE_CONNECT).queue();
      }
    }
  }

  public void addTextChannel(long channel) {
    Guild guild = guild();
    if (guild != null) {
      TextChannel textChannel = guild.getTextChannelById(channel);
      if (textChannel != null) {
        textChannel.upsertPermissionOverride(role()).grant(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.VOICE_CONNECT).queue();
      }
    }
  }

  public void removeTextChannel(long channel) {
    Guild guild = guild();
    if (guild != null) {
      TextChannel textChannel = guild.getTextChannelById(channel);
      if (textChannel != null) {
        textChannel.upsertPermissionOverride(role()).deny(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.VOICE_CONNECT).queue();
      }
    }
  }

  public void addUser(long user) {
    Guild guild = guild();
    if (guild != null) {
      guild.addRoleToMember(UserSnowflake.fromId(user), role()).queue();
    }
  }

  public void removeUser(long user) {
    Guild guild = guild();
    if (guild != null) {
      guild.removeRoleFromMember(UserSnowflake.fromId(user), role()).queue();
    }
  }

  public void delete() {
    Guild guild = guild();
    if (guild != null) {
      Objects.requireNonNull(guild.getRoleById(role)).delete().queue();
    }
  }

  public long guildId() {
    return guild;
  }

  public Guild guild() {
    return erde.api().getGuildById(guild);
  }

  public long roleId() {
    return role;
  }

  public Role role() {
    Guild guild = guild();
    return guild != null ? guild.getRoleById(role) : null;
  }
}