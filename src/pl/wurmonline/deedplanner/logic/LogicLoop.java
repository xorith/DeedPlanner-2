package pl.wurmonline.deedplanner.logic;

import java.util.*;
import pl.wurmonline.deedplanner.*;
import pl.wurmonline.deedplanner.Properties;
import pl.wurmonline.deedplanner.data.*;
import pl.wurmonline.deedplanner.forms.Planner;
import pl.wurmonline.deedplanner.input.*;
import pl.wurmonline.deedplanner.logic.floors.FloorUpdater;
import pl.wurmonline.deedplanner.logic.ground.GroundUpdater;
import pl.wurmonline.deedplanner.logic.height.HeightUpdater;
import pl.wurmonline.deedplanner.logic.objects.ObjectsUpdater;
import pl.wurmonline.deedplanner.logic.roofs.RoofUpdater;
import pl.wurmonline.deedplanner.logic.walls.WallUpdater;

public class LogicLoop extends TimerTask {

    private Planner planner;
    private final MapPanel panel;
    
    private final Keyboard keyboard;
    private final Keybindings keybindings;
    private final Mouse mouse;
    
    private final Timer timer;
    private int timerFPS;
    
    private boolean runFlag = true;
    private boolean stopped = false;
    
    public LogicLoop(MapPanel panel) {
        this.panel = panel;
        
        keyboard = new Keyboard(panel);
        keybindings = new Keybindings(keyboard);
        mouse = new Mouse(panel);
        
        timer = new Timer();
    }
    
    public void start(Planner planner) {
        this.planner = planner;
        
        timerFPS = Properties.logicFps;
        timer.scheduleAtFixedRate(this, 0, 1000/Properties.logicFps);
    }
    
    public void run() {
        if (runFlag) {
            stopped = false;
            if (mouse.hold.left) {
                panel.requestFocus();
            }
            mouse.update();
            keyboard.update();

            if (Globals.upCamera) {
                panel.getUpCamera().update(mouse, keybindings);
                if (panel.getUpCamera().tile!=null) {
                    switch (Globals.tab) {
                        case ground:
                            GroundUpdater.update(mouse, panel.getMap(), panel.getUpCamera());
                            break;
                        case height:
                            SelectionType selectionType = HeightUpdater.update(mouse, panel.getMap(), panel.getUpCamera());
                            TileSelection.update(mouse, keyboard, panel.getMap(), panel.getUpCamera(), selectionType);
                            break;
                        case floors:
                            FloorUpdater.update(mouse, panel.getMap(), panel.getUpCamera());
                            break;
                        case walls:
                            WallUpdater.update(mouse, panel.getMap(), panel.getUpCamera());
                            break;
                        case roofs:
                            RoofUpdater.update(mouse, panel.getMap(), panel.getUpCamera());
                            break;
                        case objects:
                            ObjectsUpdater.update(mouse, panel.getMap(), panel.getUpCamera());
                            break;
                        case labels:
                            TileSelection.update(mouse, keyboard, panel.getMap(), panel.getUpCamera(), SelectionType.MULTIPLE);
                            break;
                    }
                }
                if (panel.getUpCamera().tile!=null) {
                    Tile t = panel.getUpCamera().tile;
                    TileFragment frag = TileFragment.calculateTileFragment(panel.getUpCamera().xTile, panel.getUpCamera().yTile);
                    planner.heightShow.setUpCamera(panel.getUpCamera());
                    StringBuilder build = new StringBuilder();
                    switch (Globals.tab) {
                        case ground:
                            build.append(t.getGround());
                            break;
                        case floors: case roofs:
                            if (t.getTileContent(Globals.floor)!=null) {
                                build.append(t.getTileContent(Globals.floor));
                            }
                            break;
                        case walls:
                            if (frag == TileFragment.S) {
                                appendWalls(t.getHorizontalWall(Globals.floor), t.getHorizontalFence(Globals.floor), build);
                            }
                            else if (frag == TileFragment.W) {
                                appendWalls(t.getVerticalWall(Globals.floor), t.getVerticalFence(Globals.floor), build);
                            }
                            else if (frag == TileFragment.N) {
                                Tile temp = t.getMap().getTile(t, 0, 1);
                                appendWalls(temp.getHorizontalWall(Globals.floor), temp.getHorizontalFence(Globals.floor), build);
                            }
                            else if (frag == TileFragment.E) {
                                Tile temp = t.getMap().getTile(t, 1, 0);
                                appendWalls(temp.getVerticalWall(Globals.floor), temp.getVerticalFence(Globals.floor), build);
                            }
                            break;
                        case objects:
                            ObjectLocation loc = ObjectLocation.calculateObjectLocation(panel.getUpCamera().xTile, panel.getUpCamera().yTile);
                            GameObject obj = t.getGameObject(Globals.floor, loc);
                            if (obj!=null) {
                                build.append(obj);
                            }
                            break;
                    }
                    if (frag.isCorner()) {
                        build.append("     Height: ").append(frag.getTileByCorner(t).getHeight());	
                    }
                    build.append("     X: ").append(t.getX()).append(" Y: ").append(t.getY());
                    planner.tileLabel.setText(build.toString());
                }
            }
            else {
                panel.getFPPCamera().update(mouse, keybindings);
            }

            if (timerFPS!=Properties.logicFps) {
                timer.cancel();
                timer.scheduleAtFixedRate(this, 0, 1000/Properties.logicFps);
            }
            stopped = true;
        }
        else {
            stopped = true;
        }
    }
    
    private void appendWalls(Wall wall, Wall fence, StringBuilder build) {
        if (wall!=null) {
            build.append(wall.toString());
        }
        if (wall!=null && fence!=null) {
            build.append("     ");
        }
        if (fence!=null) {
            build.append(fence.toString());
        }
    }
    
    public void setRunFlag(boolean runFlag) {
        this.runFlag = runFlag;
    }
    
    public boolean isStopped() {
        return stopped;
    }
    
}
