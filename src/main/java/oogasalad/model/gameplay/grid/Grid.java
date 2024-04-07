package oogasalad.model.gameplay.grid;

import java.util.ArrayList;
import java.util.List;
import oogasalad.model.gameplay.blocks.AbstractBlock;
import oogasalad.model.gameplay.factory.BlockFactory;
import oogasalad.model.gameplay.interpreter.RuleInterpreter;
import oogasalad.model.gameplay.strategies.*;
import oogasalad.model.gameplay.utils.exceptions.InvalidBlockName;
import oogasalad.model.gameplay.utils.exceptions.VisitorReflectionException;
import oogasalad.shared.observer.Observable;
import oogasalad.shared.observer.Observer;

public class Grid implements Observable<Grid> {
  private List<Observer<Grid>> observers = new ArrayList<>();
  private List<AbstractBlock>[][] grid;

  private RuleInterpreter parser;
  private BlockFactory factory;

  public Grid(int rows, int cols) throws InvalidBlockName {
    this.grid = new ArrayList[rows][cols];
    this.parser = new RuleInterpreter();
    this.factory = new BlockFactory();
    InitializeGrid();
  }

  // Call everytime there's a handle key press
  public void checkForRules() throws VisitorReflectionException {
    parser.interpretRules(grid);
  }
  public void moveBlock(int fromI, int fromJ, int fromK, int ToI, int ToJ){
    grid[ToI][ToJ].add(grid[fromI][fromJ].get(fromK));
  }
  public void setBlock(int i, int j, int k, String BlockType){
    grid[i][j].set(k, factory.createBlock(BlockType));
  }
  public AbstractBlock getBlock(int i, int j, int k){
    return grid[i][j].get(k);
  }

  public List<AbstractBlock>[][] getGrid() {
    return this.grid;
  }

  @Override
  public void addObserver(Observer<Grid> o) {
    observers.add(o);
  }

  @Override
  public void notifyObserver() {
    for (Observer<Grid> observer : observers) {
      observer.update(this);
    }
  }

  public int gridWidth(){
    return grid.length;
  }

  public int gridHeight(){
    return grid[0].length;
  }

  public int cellSize(int i, int j){
    return grid[i][j].size();
  }



  public List<int[]> findControllableBlock() { //record class
    List<int[]> AllControllableBlocks = new ArrayList<>();
    for(int i = 0; i < grid.length; i++){
      for(int j = 0; j < grid[i].length; j++){
        for(int k= 0; k < grid[i][j].size(); k++){
          AbstractBlock block = grid[i][j].get(k);
          if(block != null && block.hasBehavior(Controllable.class)){
            int [] a = {i, j, k};
            AllControllableBlocks.add(a);
          }
        }
      }
    }
    return AllControllableBlocks;
  }

  public boolean isMovableToMargin(int endI, int endJ, int endK, int controllableintialI, int controllableintialJ, int controllableinitialK){
    boolean already_in_margin = isAlreadyInMargin(controllableintialI, controllableintialJ);
    if(already_in_margin){
      return true;
    }
    if((endI == grid.length -1 || endI == 0)){
      int indexI;
      indexI = (endI == 0) ? endI + 1 : endI - 1;

      return grid[indexI][endJ].get(endK).hasBehavior(Controllable.class);
    }
    else if ((endJ == grid[0].length -1 || endJ == 0)){
      int indexJ;
      indexJ = (endJ == 0) ? endJ + 1 : endJ -1;
      return grid[endI][indexJ].get(endK).hasBehavior(Controllable.class);
    }
    else{
      return true;
    }
  }

  private boolean isAlreadyInMargin(int i, int j){
    return ((i == grid.length -1 || i == 0) || (j == grid[0].length -1 || j == 0));
  }

  public void checkBehaviors(){
    for(int i = 0; i< grid.length; i++){
      for(int j = 0; j < grid[i].length; j++){
        for(int k = 0; k< grid[i][j].size(); k++){
          AbstractBlock block = grid[i][j].get(k);

          if (block != null && block.hasBehavior(BecomesWall.class)) {
            changeBlockToWall(i, j, k);
          }
          // temporary, needs refactoring
          if (block != null && block.hasBehavior(BecomesEmpty.class)) {
            changeBlockToEmpty(i, j, k);
          }
        }
      }
    }
  }

  public boolean cellHasPushable(int i , int j){
    return grid[i][j].stream().anyMatch(block -> block.hasBehavior(Pushable.class));
    //check for pushable, stoppable, and phasable.
  }

  public List<Integer> allPushableBlocksIndex(int i, int j) {
    List<Integer> indicesList = new ArrayList<>();

    grid[i][j].stream()
            .filter(block -> block.hasBehavior(Pushable.class))
            .mapToInt(block -> grid[i][j].indexOf(block))
            .forEach(indicesList::add);

    return indicesList;
  }

  public boolean isNotOutOfBounds(int i, int j){
    return i >= 0 && i < grid.length && j >= 0 && j < grid[i].length;
  }
  private void changeBlockToEmpty(int i, int j, int k) {
    grid[i][j].set(k, factory.createBlock("EmptyVisualBlock"));
  }

  private void changeBlockToWall(int i, int j, int k) {
    grid[i][j].set(k, factory.createBlock("WallVisualBlock"));
  }


  String[][][] tempConfiguration = {
      {
        {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"},
        {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"},
        {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"},
        {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"}
      },
      {
        {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"},
        {"EmptyVisualBlock"}, {"EmptyTextBlock"}, {"EmptyVisualBlock"}, {"RockVisualBlock"},
        {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"},
        {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"}
      },
      {
        {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"},
        {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"IsTextBlock"}, {"WallTextBlock"},
        {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"},
        {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"}
      },
      {
        {"EmptyVisualBlock"}, {"BabaTextBlock"}, {"IsTextBlock"}, {"YouTextBlock"},
        {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"},
        {"EmptyVisualBlock"}, {"WinTextBlock"}, {"WallVisualBlock"}, {"EmptyVisualBlock"},
        {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"}
      },
      {
        {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"},
        {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"},
        {"EmptyVisualBlock"}, {"WallVisualBlock"}, {"WallVisualBlock"}, {"EmptyVisualBlock"},
        {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"}
      },
      {
        {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"WallVisualBlock"},
        {"WallVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"},
        {"EmptyVisualBlock"}, {"WallVisualBlock"}, {"EmptyVisualBlock"}, {"StopTextBlock"},
        {"EmptyVisualBlock"}, {"BabaTextBlock"}, {"EmptyVisualBlock"}
      },
      {
        {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"WallVisualBlock"},
        {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"},
        {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"},
        {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"}
      },
      {
        {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"WallVisualBlock"},
        {"BabaVisualBlock"}, {"WallVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"},
        {"EmptyVisualBlock"}, {"FlagTextBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"},
        {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"}
      },
      {
        {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"WallVisualBlock"},
        {"WallVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"},
        {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"},
        {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"}
      },
      {
        {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"},
        {"EmptyVisualBlock"}, {"FlagVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"},
        {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"},
        {"EmptyVisualBlock"}, {"WallVisualBlock"}, {"EmptyVisualBlock"}
      },
      {
        {"EmptyVisualBlock"}, {"WallVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"},
        {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"},
        {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"},
        {"EmptyVisualBlock"}, {"WallVisualBlock"}, {"EmptyVisualBlock"}
    },
      {
        {"EmptyVisualBlock"}, {"WallVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"},
        {"IsTextBlock"}, {"YouTextBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"},
        {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"},
        {"EmptyVisualBlock"}, {"WallVisualBlock"}, {"EmptyVisualBlock"}
      },
      {
        {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"},
        {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"},
        {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"},
        {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"}
      },
      {
        {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"RockTextBlock"}, {"EmptyVisualBlock"},
        {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"WallVisualBlock"},
        {"WallVisualBlock"}, {"WallVisualBlock"}, {"EmptyVisualBlock"}, {"WallTextBlock"},
        {"IsTextBlock"}, {"StopTextBlock"}, {"EmptyVisualBlock"}
      },
      {
        {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"},
        {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"},
        {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"},
        {"EmptyVisualBlock"}, {"EmptyVisualBlock"}, {"EmptyVisualBlock"}
      }
  };

  private void createBlocks(List<AbstractBlock> AbstractBlocks, String[] Blocktypes){
    for(int i=0; i< Blocktypes.length; i++){
      AbstractBlocks.add(factory.createBlock(Blocktypes[i]));
    }
  }

  private void InitializeGrid(){
    // Initializing elements
    for (int i = 0; i < grid.length; i++) {
      for (int j = 0; j < grid[i].length; j++) {
        grid[i][j] = new ArrayList<AbstractBlock>();
        createBlocks(grid[i][j], tempConfiguration[i][j]);
      }
    }
  }


}
