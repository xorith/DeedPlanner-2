package pl.wurmonline.deedplanner.data;

import javax.media.opengl.GL2;
import org.w3c.dom.*;
import pl.wurmonline.deedplanner.Globals;
import pl.wurmonline.deedplanner.data.storage.Data;

public class Wall implements TileEntity {

    public final WallData data;
    
    private final boolean reversed;
    
    public Wall(Element wall) {
        String shortname = wall.getAttribute("id");
        this.data = Data.walls.get(shortname);
        this.reversed = wall.getAttribute("reversed").equals("true");
    }
    
    public Wall(WallData data) {
        this.data = data;
        if (data.houseWall) {
            this.reversed = Globals.reverseWall;
        }
        else {
            this.reversed = false;
        }
    }
    
    public void render(GL2 g, Tile tile) {
        if (Globals.upCamera) {
            g.glScalef(1, data.scale, 1);
        }
        if (reversed) {
            g.glScalef(1, -1, 1);
        }
        g.glTranslatef(4, 0, 0);
        data.model.render(g);
    }
    
    public Wall deepCopy() {
        return new Wall(data);
    }
    
    public void serialize(Document doc, Element root) {
        root.setAttribute("id", data.shortName);
        if (data.houseWall) {
            root.setAttribute("reversed", Boolean.toString(reversed));
        }
    }
    
    public Materials getMaterials() {
        return data.getMaterials();
    }
    
    public boolean equals(Object obj) {
        if (!(obj instanceof Wall)) {
            return false;
        }
        else {
            Wall wall = (Wall) obj;
            if (data!=wall.data) {
                return false;
            }
            return true;
        }
    }
    
    public String toString() {
        return data.toString();
    }

}
