package oogasalad.view.authoring;

import java.io.File;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Pair;

public class ElementsScene {

  private ScrollPane scrollPane;
  private VBox layout;
  private final String IMAGE_FILE_PATH = "src/main/resources/blocktypes/blocktypes.json";
  private boolean removeMode = false;
  private final BuilderScene builderScene;
  private final BlockLoader blockLoader = new BlockLoader();

  public ElementsScene(BuilderScene builderScene) {
    this.builderScene = builderScene;
    initializeElementsLayout();
  }

  private void initializeElementsLayout() {
    layout = new VBox(10); // Adjust spacing as needed

    // Load the "babaisyou" block image
    ImageView babaisyouImage = loadImageFromDirectory("BabaIsYouHeader", 300, 300);

    // Set the "babaisyou" block image as the graphic for the titleLabel
    Label titleLabel = new Label();
    titleLabel.setGraphic(babaisyouImage);
    titleLabel.setContentDisplay(ContentDisplay.CENTER);
    titleLabel.setAlignment(Pos.CENTER);

    // Add descriptive text
    Label descriptionLabel = new Label("Drag blocks from this panel:");
    descriptionLabel.setStyle("-fx-font-size: 14pt;");

    // Create a container for blocks
    FlowPane blocksContainer = new FlowPane(); // FlowPane instead of VBox
    blocksContainer.setHgap(30); // Adjust spacing between blocks
    blocksContainer.setVgap(30); // Adjust spacing between rows
    blocksContainer.setAlignment(Pos.CENTER); // Center the blocks horizontally

    loadBlocks(blocksContainer);

    // Button for changing grid size
    Button changeGridSizeButton = new Button("Change Grid Size");
    changeGridSizeDialog(changeGridSizeButton);

    // Button for toggling remove mode
    Button removeButton = new Button("Remove block");
    removeButton.setOnAction(event -> {
      removeMode = !removeMode;
      if (removeMode) {
        removeButton.setText("Removing mode: Press blocks to remove");
      } else {
        removeButton.setText("Remove block");
      }
      builderScene.setRemove(removeMode);
    });

    // Scroll pane for blocks container
    scrollPane = new ScrollPane(blocksContainer);
    scrollPane.setFitToWidth(true);
    scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

    layout.setStyle("-fx-background-color: linear-gradient(to bottom, #777DA1, #9773FD)");
    blocksContainer.setStyle("-fx-background-color: linear-gradient(to bottom, #777DA1, #9773FD)");

    // Add components to layout
    layout.getChildren()
        .addAll(titleLabel, changeGridSizeButton, removeButton, descriptionLabel, scrollPane);
    VBox.setVgrow(scrollPane, Priority.ALWAYS); // Allow the scroll pane to grow and fill space
  }

  // Method to access the complete scene layout
  public VBox getLayout() {
    return layout;
  }


  private void loadBlocks(FlowPane blocksContainer) {
    blockLoader.loadBlocks(blocksContainer, IMAGE_FILE_PATH);
  }


  private ImageView loadImageFromDirectory(String imageName, double width, double height) {
    String directoryPath = "src/main/resources/images";
    File selectedDirectory = new File(directoryPath);
    if (selectedDirectory.exists() && selectedDirectory.isDirectory()) {
      File imageFile = new File(selectedDirectory, imageName + ".png");
      if (imageFile.exists()) {
        Image image = new Image(imageFile.toURI().toString(), width, height, true, true);
        return new ImageView(image);
      } else {
        System.err.println("Image file not found: " + imageName);
      }
    } else {
      System.err.println("Directory not found or is not a valid directory.");
    }
    return null;
  }

  private void changeGridSizeDialog(Button button) {
    button.setOnAction(event -> {
      // Create the custom dialog.
      Dialog<Pair<Integer, Integer>> dialog = new Dialog<>();
      dialog.setTitle("Change Grid Size");

      // Set the button types.
      ButtonType confirmButtonType = ButtonType.OK;
      dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

      // Create the width and height labels and fields.
      GridPane grid = new GridPane();
      grid.setHgap(10);
      grid.setVgap(10);

      TextField widthField = new TextField();
      widthField.setPromptText("Width");
      TextField heightField = new TextField();
      heightField.setPromptText("Height");

      grid.add(new Label("Width:"), 0, 0);
      grid.add(widthField, 1, 0);
      grid.add(new Label("Height:"), 0, 1);
      grid.add(heightField, 1, 1);

      // Enable/Disable confirm button depending on whether a width and height were entered.
      Button confirmButton = (Button) dialog.getDialogPane().lookupButton(confirmButtonType);
      confirmButton.setDisable(true);

      // Do some validation (using the Java 8 lambda syntax).
      widthField.textProperty().addListener((observable, oldValue, newValue) -> {
        confirmButton.setDisable(
            newValue.trim().isEmpty() || heightField.getText().trim().isEmpty());
      });
      heightField.textProperty().addListener((observable, oldValue, newValue) -> {
        confirmButton.setDisable(
            newValue.trim().isEmpty() || widthField.getText().trim().isEmpty());
      });

      dialog.getDialogPane().setContent(grid);

      // Convert the result to a pair of width and height when the confirm button is clicked.
      dialog.setResultConverter(dialogButton -> {
        if (dialogButton == confirmButtonType) {
          try {
            int width = Integer.parseInt(widthField.getText());
            int height = Integer.parseInt(heightField.getText());
            return new Pair<>(width, height);
          } catch (NumberFormatException e) {
            // Handle the case where one or both of the inputs are not valid integers
            return null;
          }
        }
        return null;
      });

      dialog.showAndWait().ifPresent(pair -> {
        int width = pair.getKey();
        int height = pair.getValue();// Assuming width and height are the same for a grid
        // Assuming builderScene is a reference to your BuilderScene instance
        builderScene.updateGridSize(width, height);
      });
    });
  }
}




