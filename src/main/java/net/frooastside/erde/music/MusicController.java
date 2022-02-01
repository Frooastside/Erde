package net.frooastside.erde.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import net.frooastside.erde.Erde;
import net.frooastside.erde.Feedback;
import net.frooastside.erde.language.I18n;

public class MusicController {

  private final Erde erde;

  public MusicController(Erde erde) {
    this.erde = erde;
  }

  public Feedback setVolume(AudioClient audioClient, String[] arguments) {
    if (arguments.length >= 1) {
      try {
        int volume = Integer.parseInt(arguments[0]);
        audioClient.setVolume(volume);
        return new Feedback(I18n.get("music.volume.update", volume), Feedback.Status.POSITIVE);
      } catch (NumberFormatException exception) {
        return new Feedback(I18n.get("music.volume.error", arguments[0]), Feedback.Status.NEGATIVE);
      }
    } else {
      return new Feedback(I18n.get("error.not-enough-arguments"), Feedback.Status.NEGATIVE);
    }
  }

  public Feedback play(AudioClient audioClient, AudioLoadResultHandler loadResultHandler, String argumentString, String[] arguments) {
    if (arguments.length >= 1) {
      if (arguments[0].startsWith("http")) {
        erde.remotePlayerManager().loadItem(argumentString, loadResultHandler);
      } else {
        erde.remotePlayerManager().loadItem("ytsearch: " + argumentString, loadResultHandler);
      }
    } else {
      return playPause(audioClient);
    }
    return null;
  }

  public Feedback currentTitle(AudioClient audioClient) {
    return new Feedback(audioClient.currentTrack() != null ? I18n.get("music.queue.current") : I18n.get("music.queue.empty"), Feedback.Status.NEUTRAL);
  }

  public Feedback skip(AudioClient audioClient) {
    boolean playNextTrack = audioClient.playNextTrack();
    if (!playNextTrack) {
      audioClient.disconnect();
    }
    return new Feedback(playNextTrack ? I18n.get("music.queue.next") : I18n.get("music.queue.empty"), playNextTrack ? Feedback.Status.POSITIVE : Feedback.Status.NEGATIVE);
  }

  public Feedback playPause(AudioClient audioClient) {
    boolean togglePlaying = audioClient.togglePlaying();
    return new Feedback(togglePlaying ? I18n.get("music.queue.resumed") : I18n.get("music.queue.paused"), togglePlaying ? Feedback.Status.POSITIVE : Feedback.Status.NEGATIVE);
  }

  public Feedback loop(AudioClient audioClient) {
    boolean toggleLooping = audioClient.toggleLooping();
    return new Feedback(toggleLooping ? I18n.get("music.loop.start") : I18n.get("music.loop.end"), toggleLooping ? Feedback.Status.POSITIVE : Feedback.Status.NEGATIVE);
  }

}
