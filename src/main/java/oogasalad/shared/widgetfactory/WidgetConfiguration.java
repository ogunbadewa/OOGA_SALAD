package oogasalad.shared.widgetfactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javafx.scene.text.Font;
import oogasalad.shared.alert.AlertHandler;

/**
 * Configuration object class for use within the WidgetFactory
 */
public class WidgetConfiguration implements AlertHandler {

  private final Properties styleProperties;
  private int width;
  private int height;
  private String cssMatch;
  private String propertyContents;
  private final Font propertyFont;
  private final String language;

  /**
   * Constructor with a property files match.
   *
   * @param width
   * @param height
   * @param propertyName
   * @param cssMatch
   */
  public WidgetConfiguration(int width, int height, String propertyName, String cssMatch,
      String language) {
    this.language = language;
    this.width = width;
    this.height = height;
    this.cssMatch = cssMatch;
    this.styleProperties = loadProperties();
    this.propertyContents = styleProperties.getProperty(propertyName);
    this.propertyFont = Font.loadFont(
        getClass().getResourceAsStream(styleProperties.getProperty("Font")), 12);
  }

  public WidgetConfiguration(int width, int height, String cssMatch, String language) {
    this.language = language;
    this.width = width;
    this.height = height;
    this.cssMatch = cssMatch;
    this.styleProperties = loadProperties();
    this.propertyFont = Font.loadFont(
        getClass().getResourceAsStream(styleProperties.getProperty("Font")), 12);
  }

  /**
   * Constructor with just a string property.
   */
  public WidgetConfiguration(String propertyName, String language) {
    this.language = language;
    this.styleProperties = loadProperties();
    this.propertyContents = styleProperties.getProperty(propertyName);
    this.propertyFont = Font.loadFont(
        getClass().getResourceAsStream(styleProperties.getProperty("Font")), 12);
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public String getCssMatch() {
    return cssMatch;
  }

  public String getPropertyContents() {
    return propertyContents;
  }

  public void showError(String title, String message) {
    AlertHandler.super.showError(title, message);
  }

  private Properties loadProperties() {
    Properties properties = new Properties();
    String resourcePath = "/languages/" + language + ".properties";
    InputStream inputStream = getClass().getResourceAsStream(resourcePath);
    if (inputStream == null) {
      // Handle the case where the resource file is not found
      showError("Error", "Unable to find resource file: " + resourcePath);
      return properties; // Return an empty Properties object
    }

    try {
      properties.load(inputStream);
    } catch (IOException e) {
      showError("Error", "Unable to load properties file: " + e.getMessage());
    } finally {
      try {
        inputStream.close();
      } catch (IOException e) {
        // Handle exception when closing the input stream
        e.printStackTrace();
      }
    }

    return properties;
  }



  public Font getFont() {
    return propertyFont;
  }

}
