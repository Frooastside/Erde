package net.frooastside.erde.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import java.awt.Color;
import net.dv8tion.jda.api.EmbedBuilder;
import net.frooastside.erde.Erde;
import net.frooastside.erde.Feedback;
import net.frooastside.erde.language.I18n;

public class LoadResultHandler implements AudioLoadResultHandler {

  private final AudioClient audioClient;
  private final boolean addFirst;

  public LoadResultHandler(AudioClient audioClient, boolean addFirst) {
    this.audioClient = audioClient;
    this.addFirst = addFirst;
  }

  @Override
  public void trackLoaded(AudioTrack track) {
    if (!addFirst) {
      audioClient.addTrackLast(track);
    } else {
      audioClient.addTrackFirst(track);
    }
    EmbedBuilder builder = new EmbedBuilder()
      .setDescription(I18n.get("music.queue.add.single", track.getInfo().title))
      .setTitle(track.getInfo().uri)
      .setColor(Color.GREEN);
    Erde.sendEmbed(audioClient.textChannel(), builder);
    audioClient.playIfNotPlaying();
  }

  @Override
  public void playlistLoaded(AudioPlaylist playlist) {
    if (playlist.isSearchResult()) {
      AudioTrack audioTrack = playlist.getTracks().get(0);
      if (!addFirst) {
        audioClient.addTrackLast(audioTrack);
      } else {
        audioClient.addTrackFirst(audioTrack);
      }
      EmbedBuilder builder = new EmbedBuilder()
        .setDescription(I18n.get("music.queue.add.single", audioTrack.getInfo().title))
        .setTitle(audioTrack.getInfo().uri)
        .setColor(Color.GREEN);
      Erde.sendEmbed(audioClient.textChannel(), builder);
    } else {
      for (AudioTrack audioTrack : playlist.getTracks()) {
        if (!addFirst) {
          audioClient.addTrackLast(audioTrack);
        } else {
          audioClient.addTrackFirst(audioTrack);
        }
      }
      Erde.sendEmbed(audioClient.textChannel(), new Feedback(I18n.get("music.queue.add.multiple", playlist.getTracks().size()), Feedback.Status.POSITIVE));
    }
    audioClient.playIfNotPlaying();
  }

  @Override
  public void noMatches() {
    Erde.sendEmbed(audioClient.textChannel(), new Feedback(I18n.get("music.queue.add.error"), Feedback.Status.NEGATIVE));
  }

  @Override
  public void loadFailed(FriendlyException exception) {
    Erde.sendEmbed(audioClient.textChannel(), new Feedback(exception.severity == FriendlyException.Severity.COMMON ? I18n.get("error.friendly", exception.getLocalizedMessage()) : I18n.get("error.unknown"), Feedback.Status.NEGATIVE));
  }
}
