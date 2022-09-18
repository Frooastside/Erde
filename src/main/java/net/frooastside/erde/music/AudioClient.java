package net.frooastside.erde.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import java.nio.ByteBuffer;
import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import net.frooastside.erde.Erde;

public class AudioClient extends AudioEventAdapter implements AudioSendHandler {

  private final Erde erde;
  private final long guild;
  private final AudioPlayer audioPlayer;
  private final Deque<AudioTrack> audioQueue = new LinkedBlockingDeque<>();
  private AudioManager audioManager;
  private AudioFrame frame;
  private AudioTrack loopedSong;

  public AudioClient(Erde erde, AudioChannel audioChannel) {
    this.erde = erde;
    this.guild = audioChannel.getGuild().getIdLong();
    this.audioPlayer = erde.remotePlayerManager().createPlayer();
    this.audioPlayer.addListener(this);
    connect(audioChannel);
  }

  private void connect(AudioChannel audioChannel) {
    audioManager = audioChannel.getGuild().getAudioManager();
    audioManager.openAudioConnection(audioChannel);
    audioManager.setSendingHandler(this);
    setVolume(10);
  }

  public void disconnect() {
    erde.removeClient(guild);
    audioManager.closeAudioConnection();
    audioPlayer.destroy();
  }

  public boolean playNextTrack() {
    if (!audioQueue.isEmpty()) {
      AudioTrack newTrack = audioQueue.poll();
      if (newTrack != null) {
        playTrack(newTrack);
        return true;
      }
    }
    return false;
  }

  public void playIfNotPlaying() {
    if (!audioQueue.isEmpty()) {
      AudioTrack audioTrack = audioQueue.peek();
      if (audioTrack != null) {
        if (audioPlayer.startTrack(audioTrack, true)) {
          audioQueue.poll();
        }
      }
    }
  }

  public boolean togglePlaying() {
    boolean paused = audioPlayer.isPaused();
    audioPlayer.setPaused(!paused);
    return paused;
  }

  public boolean toggleLooping() {
    boolean looping = loopedSong != null;
    if (looping) {
      loopedSong = null;
    } else {
      AudioTrack currentTrack = currentTrack();
      if (currentTrack != null) {
        loopedSong = currentTrack.makeClone();
      }
    }
    return loopedSong != null;
  }

  public void playTrack(AudioTrack audioTrack) {
    audioPlayer.playTrack(audioTrack);
  }

  public void addTrackFirst(AudioTrack audioTrack) {
    audioQueue.offerFirst(audioTrack);
  }

  public void addTrackLast(AudioTrack audioTrack) {
    audioQueue.offerLast(audioTrack);
  }

  public AudioTrack currentTrack() {
    return audioPlayer.getPlayingTrack();
  }

  public void setVolume(int volume) {
    audioPlayer.setVolume(volume);
  }

  @Override
  public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
    if (loopedSong != null) {
      playTrack(loopedSong.makeClone());
    } else {
      if (endReason.mayStartNext) {
        playNextTrack();
      }
    }
  }

  @Override
  public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
    if (loopedSong != null) {
      playTrack(loopedSong.makeClone());
    } else {
      playNextTrack();
    }
  }

  @Override
  public boolean canProvide() {
    frame = audioPlayer.provide();
    return frame != null;
  }

  @Override
  public ByteBuffer provide20MsAudio() {
    return ByteBuffer.wrap(frame.getData());
  }

  @Override
  public boolean isOpus() {
    return true;
  }

  public long guild() {
    return guild;
  }

  public MessageChannel textChannel() {
    return erde.textChannel(guild);
  }
}
