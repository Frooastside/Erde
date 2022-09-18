package net.frooastside.erde.language;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Language {

  private final Map<String, String> translatedStrings;
  private final String languageCode;
  private final String languageName;

  public Language(String languageCode, String languageName, Map<String, String> translatedStrings) {
    this.languageCode = languageCode;
    this.languageName = languageName;
    this.translatedStrings = translatedStrings;
  }

  public static Language createFromStream(InputStream inputStream) {
    Map<String, String> translatedStrings = new HashMap<>();
    String languageCode = "[]_[]";
    String languageName = "Invalid Language";
    try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
      while (bufferedReader.ready()) {
        String line = bufferedReader.readLine();
        if (line.contains("=")) {
          String key = line.substring(0, line.indexOf("="));
          String value = line.substring(line.indexOf("=") + 1);
          if (key.equals("language.code")) {
            languageCode = value;
          } else if (key.equals("language.name")) {
            languageName = value;
          } else {
            translatedStrings.put(key, value);
          }
        }
      }
    } catch (IOException ignored) {
    }
    return new Language(languageCode, languageName, translatedStrings);
  }

  public static Language createFromFile(File file) {
    Map<String, String> translatedStrings = new HashMap<>();
    String languageCode = "[]_[]";
    String languageName = "Invalid Language";
    try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
      while (bufferedReader.ready()) {
        String line = bufferedReader.readLine();
        if (line.contains("=")) {
          String key = line.substring(0, line.indexOf("="));
          String value = line.substring(line.indexOf("=") + 1);
          if (key.equals("language.code")) {
            languageCode = value;
          } else if (key.equals("language.name")) {
            languageName = value;
          } else {
            translatedStrings.put(key, value);
          }
        }
      }
    } catch (IOException ignored) {
    }
    return new Language(languageCode, languageName, translatedStrings);
  }

  public boolean contains(String key) {
    return translatedStrings.containsKey(key);
  }

  public String get(String key) {
    return translatedStrings.get(key);
  }

  public String languageCode() {
    return languageCode;
  }

  public String languageName() {
    return languageName;
  }
}
