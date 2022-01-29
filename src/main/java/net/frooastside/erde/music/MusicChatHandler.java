package net.frooastside.erde.music;

import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.frooastside.erde.Erde;
import net.frooastside.erde.Feedback;
import net.frooastside.erde.language.I18n;
import net.frooastside.erde.messages.ChatHandler;

public class MusicChatHandler extends ChatHandler {

  private final MusicController musicController;

  public MusicChatHandler(Erde erde) {
    super(erde, true);
    this.musicController = new MusicController(erde);
  }

  @Override
  public void registerCommands() {
    commands().put("play", this::play);
    commands().put("p", this::play);

    commands().put("playfirst", this::play);
    commands().put("pf", this::play);
    commands().put("playnext", this::playNext);
    commands().put("pn", this::playNext);

    commands().put("pause", this::playPause);
    commands().put("start", this::playPause);
    commands().put("stop", this::playPause);

    commands().put("nowplaying", this::nowPlaying);
    commands().put("np", this::nowPlaying);
    commands().put("cp", this::nowPlaying);

    commands().put("l", this::loop);
    commands().put("loop", this::loop);

    commands().put("skip", this::skip);

    commands().put("volume", this::setVolume);
    commands().put("vol", this::setVolume);
    commands().put("vl", this::setVolume);

    commands().put("dc", this::disconnect);
    commands().put("disconnect", this::disconnect);
  }

  private GuildVoiceState fetchVoiceState(MessageReceivedEvent event) {
    return erde().fetchGuildVoiceState(event.getAuthor());
  }

  private void play(MessageReceivedEvent event, String argumentString, String[] arguments) {
    AudioClient audioClient = erde().provideAudioClient(fetchVoiceState(event));
    if (audioClient == null) {
      Erde.sendEmbed(event.getChannel(), new Feedback(I18n.get("error.no-voice-channel"), Feedback.Status.NEGATIVE));
      return;
    }
    Feedback feedback = musicController.play(audioClient, new LoadResultHandler(audioClient, false), argumentString, arguments);
    if (feedback != null) {
      Erde.sendEmbed(event.getChannel(), feedback);
    }
  }

  private void playNext(MessageReceivedEvent event, String argumentString, String[] arguments) {
    AudioClient audioClient = erde().provideAudioClient(fetchVoiceState(event));
    if (audioClient == null) {
      Erde.sendEmbed(event.getChannel(), new Feedback(I18n.get("error.no-voice-channel"), Feedback.Status.NEGATIVE));
      return;
    }
    Feedback feedback = musicController.play(audioClient, new LoadResultHandler(audioClient, true), argumentString, arguments);
    if (feedback != null) {
      Erde.sendEmbed(event.getChannel(), feedback);
    }
  }

  private void playPause(MessageReceivedEvent event, String argumentString, String[] arguments) {
    AudioClient audioClient = erde().provideAudioClient(fetchVoiceState(event));
    if (audioClient == null) {
      Erde.sendEmbed(event.getChannel(), new Feedback(I18n.get("error.no-voice-channel"), Feedback.Status.NEGATIVE));
      return;
    }
    Feedback feedback = musicController.playPause(audioClient);
    Erde.sendEmbed(event.getChannel(), feedback);
  }

  private void nowPlaying(MessageReceivedEvent event, String argumentString, String[] arguments) {
    AudioClient audioClient = erde().provideAudioClient(fetchVoiceState(event));
    if (audioClient == null) {
      Erde.sendEmbed(event.getChannel(), new Feedback(I18n.get("error.no-voice-channel"), Feedback.Status.NEGATIVE));
      return;
    }
    Feedback feedback = musicController.currentTitle(audioClient);
    Erde.sendEmbed(event.getChannel(), feedback);
  }

  private void loop(MessageReceivedEvent event, String argumentString, String[] arguments) {
    AudioClient audioClient = erde().provideAudioClient(fetchVoiceState(event));
    if (audioClient == null) {
      Erde.sendEmbed(event.getChannel(), new Feedback(I18n.get("error.no-voice-channel"), Feedback.Status.NEGATIVE));
      return;
    }
    Feedback feedback = musicController.loop(audioClient);
    Erde.sendEmbed(event.getChannel(), feedback);
  }

  private void skip(MessageReceivedEvent event, String argumentString, String[] arguments) {
    AudioClient audioClient = erde().provideAudioClient(fetchVoiceState(event));
    if (audioClient == null) {
      Erde.sendEmbed(event.getChannel(), new Feedback(I18n.get("error.no-voice-channel"), Feedback.Status.NEGATIVE));
      return;
    }
    Feedback feedback = musicController.skip(audioClient);
    Erde.sendEmbed(event.getChannel(), feedback);
  }

  private void setVolume(MessageReceivedEvent event, String argumentString, String[] arguments) {
    AudioClient audioClient = erde().provideAudioClient(fetchVoiceState(event));
    if (audioClient == null) {
      Erde.sendEmbed(event.getChannel(), new Feedback(I18n.get("error.no-voice-channel"), Feedback.Status.NEGATIVE));
      return;
    }
    Feedback feedback = musicController.setVolume(audioClient, arguments);
    Erde.sendEmbed(event.getChannel(), feedback);
  }

  private void disconnect(MessageReceivedEvent event, String argumentString, String[] arguments) {
    AudioClient audioClient = erde().provideAudioClient(fetchVoiceState(event));
    if (audioClient != null) {
      audioClient.disconnect();
    }
  }

}
