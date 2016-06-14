package edu.cmu.tetradapp.workbench;

/**
 * Created by IntelliJ IDEA. User: jdramsey Date: Apr 4, 2006 Time: 4:39:38 PM
 * To change this template use File | Settings | File Templates.
 */
public interface DisplayComp {
    void setName(String name);
    boolean contains(int x, int y);
    void setSelected(boolean selected);
}
