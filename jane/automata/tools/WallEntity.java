package com.sqweebloid.jane.automata.tools;

import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import net.***REMOVED***.api.WallObject;
import net.***REMOVED***.api.Point;
import net.***REMOVED***.api.coords.WorldPoint;
import net.***REMOVED***.api.queries.WallObjectQuery;
import net.***REMOVED***.client.util.QueryRunner;

public class WallEntity extends Entity {
    private int[] targets = new int[0];

    public void setTarget(int... ids) {
        targets = ids;
    }

    public void setTarget(int id) {
        targets = new int[]{ id };
    }

    /**
     * Transforms an Object (one of the candidates) into a WorldPoint
     * which represents its location.
     */
    public WorldPoint yieldLocation(Object obj) {
        WallObject object = (WallObject) obj;
        return object.getWorldLocation();
    }

    /**
     * Transforms an Object (one of the candidates) into a Polygon
     * which represents its clickbox on the screen.
     */
    public Polygon yieldPolygon(Object obj) {
        return ((WallObject) obj).getCanvasTilePoly();
    }

    /**
     * Fetches the list of candidates.
     */
    public Object[] getCandidates() {
        WallObjectQuery query = new WallObjectQuery().idEquals(targets);
        WallObject[] objects = getQueryRunner().runQuery(query);
        return objects;
    }
}