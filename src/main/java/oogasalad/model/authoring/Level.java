package oogasalad.model.authoring;

import java.util.ArrayList;
import java.util.List;
import oogasalad.shared.observer.Observable;
import oogasalad.shared.observer.Observer;

/**
 * Level abstraction encapsulates a Grid, LevelMetadata. Implements Observable to provide
 * notifications on state changes.
 */
public class Level implements Observable<Level> {

  private Grid grid;
  private LevelMetadata levelMetadata;
  private List<Observer<Level>> observers;

  /**
   * Level Constructor. Initialized with LevelMetadata record and BlockTypeManager
   *
   * @param levelMetadata    The levelMetadata record representing the level.
   * @param blockTypeManager The blockTypeManager being used in the application.
   */
  public Level(LevelMetadata levelMetadata, BlockTypeManager blockTypeManager) {
    this.levelMetadata = levelMetadata;
    grid = new Grid(levelMetadata.rows(), levelMetadata.cols(), blockTypeManager);
    observers = new ArrayList<>();
  }

  /**
   * Set Cell of a level with block type. (type must be in block types properties file).
   *
   * @param row       The row position to be modified.
   * @param col       The column position to be modified.
   * @param blockName New block type.
   * @throws Exception Throws exception if block type is invalid (not in properties file).
   */
  public void setCell(int row, int col, String blockName) throws Exception {
    grid.setCell(row, col, blockName);
  }

  /**
   * Add Grid observer to list of observers.
   *
   * @param o The Observer to add to notification service.
   */
  @Override
  public void addObserver(Observer<Level> o) {
    observers.add(o);
  }

  /**
   * Notify observers of an update.
   */
  @Override
  public void notifyObserver() {
    for (Observer<Level> observer : observers) {
      observer.update(this);
    }
  }
}