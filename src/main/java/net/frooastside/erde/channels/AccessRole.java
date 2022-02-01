package net.frooastside.erde.channels;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.frooastside.erde.Erde;

import java.util.Objects;

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
        category.createPermissionOverride(role()).setAllow(
          Permission.VIEW_CHANNEL,
          Permission.MESSAGE_SEND,
          Permission.VOICE_CONNECT).queue();
      }
    }
  }

  public void removeCategory(long categoryId) {
    Guild guild = guild();
    if (guild != null) {
      Category category = guild.getCategoryById(categoryId);
      if (category != null) {
        category.createPermissionOverride(role()).setDeny(
          Permission.VIEW_CHANNEL,
          Permission.MESSAGE_SEND,
          Permission.VOICE_CONNECT).queue();
      }
    }
  }

  public void addTextChannel(long channel) {
    Guild guild = guild();
    if (guild != null) {
      TextChannel textChannel = guild.getTextChannelById(channel);
      if (textChannel != null) {
        textChannel.createPermissionOverride(role()).setAllow(
          Permission.VIEW_CHANNEL,
          Permission.MESSAGE_SEND,
          Permission.VOICE_CONNECT).queue();
      }
    }
  }

  public void removeTextChannel(long channel) {
    Guild guild = guild();
    if (guild != null) {
      TextChannel textChannel = guild.getTextChannelById(channel);
      if (textChannel != null) {
        textChannel.createPermissionOverride(role()).setDeny(
          Permission.VIEW_CHANNEL,
          Permission.MESSAGE_SEND,
          Permission.VOICE_CONNECT).queue();
      }
    }
  }

  public void addUser(long user) {
    Guild guild = guild();
    if (guild != null) {
      guild.addRoleToMember(user, role()).queue();
    }
  }

  public void removeUser(long user) {
    Guild guild = guild();
    if (guild != null) {
      guild.removeRoleFromMember(user, role()).queue();
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