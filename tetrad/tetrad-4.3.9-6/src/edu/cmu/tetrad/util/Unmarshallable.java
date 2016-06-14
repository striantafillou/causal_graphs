package edu.cmu.tetrad.util;

import java.io.Serializable;

/**
 * <p>Interface to tag classes that should not be cloned by marshalling.</p>
 *
 * @author Joseph Ramsey
 * @see edu.cmu.TestSerialization
 * @see edu.cmu.tetradapp.util.TetradSerializableUtils
 */
public interface Unmarshallable extends Serializable {
}
