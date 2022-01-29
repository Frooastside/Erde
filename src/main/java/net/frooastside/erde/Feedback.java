package net.frooastside.erde;

import java.awt.*;

public class Feedback {

  private final String text;
  private final Status status;

  public Feedback(String text, Status status) {
    this.text = text;
    this.status = status;
  }

  public String text() {
    return text;
  }

  public Status status() {
    return status;
  }

  public enum Status {

    POSITIVE(Color.GREEN),
    NEUTRAL(Color.ORANGE),
    NEGATIVE(Color.RED);

    private final Color color;

    Status(Color color) {
      this.color = color;
    }

    public Color color() {
      return color;
    }

  }

}
