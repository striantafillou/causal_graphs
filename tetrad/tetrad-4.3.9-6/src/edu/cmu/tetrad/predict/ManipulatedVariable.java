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

package edu.cmu.tetrad.predict;

import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.util.TetradSerializable;

import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * An object representing a variable and values to manipulate its expected
 * value and variance to.  Variance must be given; exp val defaults to 0 if
 * not given. The values can be both set and retrieved; the variable itself can
 * only be retrieved.
 *
 * @author Erin Korber
 */
public final class ManipulatedVariable implements TetradSerializable {
    static final long serialVersionUID = 23L;

    /**
     * @serial
     */
    private Node node;

    /**
     * @serial
     */
    private double expectecValue;

    /**
     * @serial
     */
    private double variance;

    public ManipulatedVariable(Node v, double variance) {
        this.node = v;
        this.expectecValue = 0.0;
        this.variance = variance;
    }

    public ManipulatedVariable(Node v, double expected, double variance) {
        this.node = v;
        this.expectecValue = expected;
        this.variance = variance;
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static ManipulatedVariable serializableInstance() {
        return new ManipulatedVariable(new GraphNode("X"), 1.0);
    }

    public Node getNode() {
        return node;
    }

    public double getExpectecValue() {
        return expectecValue;
    }

    public void setExpectecValue(double expectecValue) {
        this.expectecValue = expectecValue;
    }

    public double getVariance() {
        return variance;
    }

    public void setVariance(double variance) {
        this.variance = variance;
    }

    /**
     * Adds semantic checks to the default deserialization method. This method
     * must have the standard signature for a readObject method, and the body of
     * the method must begin with "s.defaultReadObject();". Other than that, any
     * semantic checks can be specified and do not need to stay the same from
     * version to version. A readObject method of this form may be added to any
     * class, even if Tetrad sessions were previously saved out using a version
     * of the class that didn't include it. (That's what the
     * "s.defaultReadObject();" is for. See J. Bloch, Effective Java, for help.
     *
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();

        if (node == null) {
            throw new NullPointerException();
        }

        if (variance < 0.0) {
            throw new IllegalStateException();
        }
    }
}


