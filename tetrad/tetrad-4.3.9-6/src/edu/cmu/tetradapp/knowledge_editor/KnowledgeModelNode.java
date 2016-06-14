///////////////////////////////////////////////////////////////////////////////
// For information as to what this class does, see the Javadoc, below.       //
// Copyright (C) 2005 by Peter Spirtes, Richard Scheines, Joseph Ramsey,     //
// and Clark Glymour.                                                        //
//                                                                           //
// This program is free software; you can redistribute it and/or modify      //
// it under the terms of the GNU General Public License as published by      //
// the Free Software Foundation; either version 2 of the License, or         //
// (at your option) any later version.                                       //
//                                                                           //
// This program is distributed in the hope that it will be useful,           //
// but WITHOUT ANY WARRANTY; without even the implied warranty of            //
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the             //
// GNU General Public License for more details.                              //
//                                                                           //
// You should have received a copy of the GNU General Public License         //
// along with this program; if not, write to the Free Software               //
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA //
///////////////////////////////////////////////////////////////////////////////

package edu.cmu.tetradapp.knowledge_editor;

import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.graph.NodeType;
import edu.cmu.tetrad.util.NamingProtocol;
import edu.cmu.tetrad.util.TetradSerializableExcluded;

import java.beans.PropertyChangeListener;

/**
 * Represents a node that's just a string name.
 *
 * @author Joseph Ramsey
 */
public class KnowledgeModelNode implements Node, TetradSerializableExcluded {
    static final long serialVersionUID = 23L;

    /**
     * @serial
     */
    private String name;

    /**
     * @serial
     */
    private int centerX;

    /**
     * @serial
     */
    private int centerY;

    //=============================CONSTRUCTORS=========================//

    public KnowledgeModelNode(String varName) {
        if (varName == null) {
            throw new NullPointerException();
        }

        this.name = varName;
    }

    public KnowledgeModelNode(KnowledgeModelNode node) {
        this.name = node.name;
        this.centerX = node.centerX;
        this.centerY = node.centerY;
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static KnowledgeModelNode serializableInstance() {
        return new KnowledgeModelNode("X");
    }

    //=============================PUBLIC METHODS=======================//

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        if (name == null) {
            throw new NullPointerException();
        }

        if (!NamingProtocol.isLegalName(name)) {
            throw new IllegalArgumentException(
                    NamingProtocol.getProtocolDescription());
        }

        this.name = name;
    }

    public NodeType getNodeType() {
        return NodeType.NO_TYPE;
    }

    public void setNodeType(NodeType nodeType) {
        throw new UnsupportedOperationException();
    }

    public int getCenterX() {
        return centerX;
    }

    public void setCenterX(int centerX) {
        this.centerX = centerX;
    }

    public int getCenterY() {
        return this.centerY;
    }

    public void setCenterY(int centerY) {
        this.centerY = centerY;
    }

    public void setCenter(int centerX, int centerY) {
        this.centerX = centerX;
        this.centerY = centerY;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        // Ignore.
    }
}


