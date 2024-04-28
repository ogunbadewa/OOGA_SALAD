package oogasalad.view.authoring;

import com.google.gson.JsonObject;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import oogasalad.controller.authoring.JsonParser;
import oogasalad.controller.authoring.LevelController;
import oogasalad.model.authoring.block.Block;
import oogasalad.model.authoring.level.Grid;
import oogasalad.model.authoring.level.Level;
import oogasalad.model.authoring.level.LevelMetadata;
import oogasalad.shared.blockview.BlockViewFactory;
import oogasalad.shared.widgetfactory.WidgetFactory;

import java.awt.*;
import java.io.IOException;
import java.util.Stack;


public class BuilderPane {

  private final int GRID_MARGIN = 10;
  private final String BLOCK_CONFIG_FILE_PATH = "/blocktypes/blocktypes.json";
  protected Pane root; // Your root node for the builder scene
  protected GridPane gridPane;
  public int gridWidth;
  protected boolean removeMode;
  public int gridHeight;
  private double cellSize; // Set the cell size
  private BlockViewFactory blockViewFactory;
  private final LevelController levelController;
  private final String language;
  private final WidgetFactory factory;
  private VBox container;

  public BuilderPane(LevelController levelController, String language) {
    this.factory = new WidgetFactory();
    this.levelController = levelController;
    this.language = language;
    try {
      this.blockViewFactory = new BlockViewFactory(BLOCK_CONFIG_FILE_PATH);
    } catch (Exception e) {
      e.printStackTrace();
    }
    initializeBuilderScene();
  }

  public void initializeBuilderScene() {
    this.root = new Pane();

    container = new VBox(10);
    container.setMinWidth(root.getWidth());
    container.setAlignment(Pos.CENTER);

    this.gridPane = new GridPane();
    LevelMetadata levelMetadata = levelController.getLevelMetadata();
    this.gridWidth = levelMetadata.cols();
    this.gridHeight = levelMetadata.rows();

    // Listen for size changes on root to re-setup the grid
    root.widthProperty().addListener((obs, oldVal, newVal) -> setUpGrid());
    root.heightProperty().addListener((obs, oldVal, newVal) -> setUpGrid());

    setUpGrid();
    setUpDropHandling();
  }

  protected void setUpGrid() {
    gridPane.getChildren().clear(); // Clear the existing grid

    // Adjust the maximum width and height available for the grid, accounting for margins
    double availableWidth = root.getWidth() - 2 * GRID_MARGIN - 2 * gridWidth;
    double availableHeight = root.getHeight() - 2 * GRID_MARGIN - 2 * gridHeight;

    // Calculate cell size based on the available space and the grid dimensions
    this.cellSize = Math.min((availableWidth) / gridWidth, (availableHeight) / gridHeight);

    // Calculate total size of the grid
    double totalGridWidth = gridWidth * (cellSize);
    double totalGridHeight = gridHeight * (cellSize);

    // Calculate the starting positions to center the grid within the root pane, considering margins
    double layoutX = (root.getWidth() - totalGridWidth) / 2;
    double layoutY = (root.getHeight() - totalGridHeight) / 2;

    // Apply the calculated layout positions to the gridPane
    gridPane.setLayoutX(layoutX);
    gridPane.setLayoutY(layoutY);

    for (int i = 0; i < gridWidth; i++) {
      for (int j = 0; j < gridHeight; j++) {
        Pane cell = new Pane();
        cell.setPrefSize(cellSize, cellSize);
        cell.getStyleClass().add("grid");
        gridPane.add(cell, i, j);
      }
    }

    // Ensure the gridPane is added to the root if not already present
    if (!root.getChildren().contains(gridPane)) {
//      container.getChildren().add(factory.wrapInHBox(gridPane, (int)root.getWidth(), 15));
      root.getChildren().add(gridPane);
    }
  }

  private void setUpDropHandling() {
    gridPane.setOnDragOver(event -> {
      if (event.getGestureSource() != gridPane && event.getDragboard().hasString()) {
        event.acceptTransferModes(TransferMode.MOVE);
      }
      event.consume();
    });

    gridPane.setOnDragDropped(event -> {
      Dragboard db = event.getDragboard();
      boolean success = false;
      if (db.hasString()) {
        String blockType = db.getString();
        ImageView blockView = createBlockView(blockType);
        if (blockView != null) {
          Point2D cellCoords = getCellCoordinates(event.getX(), event.getY());
          Point2D cellIndices = getCellIndices(event.getX(), event.getY());
          if (cellCoords != null && cellIndices != null) {
            blockView.setFitWidth(cellSize);
            blockView.setFitHeight(cellSize);
            blockView.setLayoutX(cellCoords.getX());
            blockView.setLayoutY(cellCoords.getY());
            root.getChildren().add(blockView);
            try {
              // x corresponds to column, y corresponds to row
              levelController.addBlockToCell((int) cellIndices.getY(), (int) cellIndices.getX(),
                  blockType);
            } catch (Exception e) {
              e.printStackTrace();
            }
            setRemoveModeEventHandlers(); //allows removing after adding blocks
            success = true;
          }
        }
      }
      event.setDropCompleted(success);
      event.consume();
    });
  }

  protected Point2D getCellIndices(double x, double y) {
    for (Node node : gridPane.getChildren()) {
      if (node instanceof Pane cell) {
        Bounds boundsInParent = cell.getBoundsInParent();
        if (boundsInParent.contains(x, y)) {
          // Assuming you know the row and column due to how you added the cell
          Integer colIndex = GridPane.getColumnIndex(cell);
          Integer rowIndex = GridPane.getRowIndex(cell);

          // These can be null if not set; defaulting to 0
          colIndex = colIndex == null ? 0 : colIndex;
          rowIndex = rowIndex == null ? 0 : rowIndex;

          return new Point2D(colIndex, rowIndex);
        }
      }
    }
    return null; // Position does not fall within any cell
  }


  protected Point2D getCellCoordinates(double x, double y) {
    for (Node node : gridPane.getChildren()) {
      if (node instanceof Pane cell) {
        Bounds boundsInParent = cell.getBoundsInParent();
        if (boundsInParent.contains(x, y)) {
          // Calculate the cell coordinates based on the layout coordinates of the cell
          double cellX = cell.getLayoutX() + gridPane.getLayoutX();
          double cellY = cell.getLayoutY() + gridPane.getLayoutY();
          return new Point2D(cellX, cellY);
        }
      }
    }
    return null; // Coordinates (x, y) do not fall within any cell
  }


  protected ImageView createBlockView(String blockType) {
    try {
      return blockViewFactory.createBlockView(blockType);
    } catch (Exception e) {
      System.err.println(
          "Failed to create block view for type: " + blockType + " with error: " + e.getMessage());
      return null; // Or handle this case as needed.
    }
  }


  public Pane getRoot() {
    return root;
  }

  protected void setRemoveModeEventHandlers() {
    root.getChildren().forEach(node -> {
      if (node instanceof ImageView) {
        node.setOnMouseClicked(event -> {
          if (removeMode) {
            Point2D cellIndices = getCellIndices(node.getLayoutX(), node.getLayoutY());
            try {
              levelController.removeBlockFromCell((int) cellIndices.getY(),
                  (int) cellIndices.getX());
            } catch (Exception e) {
              e.printStackTrace();
            }
            root.getChildren().remove(node);
          }
        });
      }
    });
  }

  public void setRemove(boolean remove_bool) {
    removeMode = remove_bool;
    setRemoveModeEventHandlers();
  }

  public void renderLoadedGrid(Grid loadedGrid) {
    // Clear the existing content of the builder pane
    gridPane.getChildren().clear();

    // Update the grid dimensions based on the loaded grid
    this.gridWidth = loadedGrid.getNumColumns()-2;
    this.gridHeight = loadedGrid.getNumRows()-2;

    // Set up the grid again based on the updated dimensions
    setUpGrid();

    // Iterate over the loaded grid and render each block
    for (int row = 0; row < loadedGrid.getNumRows(); row++) {
      for (int col = 0; col < loadedGrid.getNumColumns(); col++) {
        String[] blockTypes = loadedGrid.getCell(row, col);

        // Render each block type in the cell
        for (String blockType : blockTypes) {
          // Create and add block view to the grid pane
          System.out.println(blockType);
          ImageView blockView = createBlockView(blockType);
          if (blockView != null) {
            blockView.setFitWidth(cellSize);
            blockView.setFitHeight(cellSize);
            blockView.setLayoutX(col * cellSize + gridPane.getLayoutX());
            blockView.setLayoutY(row * cellSize + gridPane.getLayoutY());
            gridPane.getChildren().add(blockView);
          }
        }
      }
    }
  }





}
