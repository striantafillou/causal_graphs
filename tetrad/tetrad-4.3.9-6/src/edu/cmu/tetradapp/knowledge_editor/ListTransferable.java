package edu.cmu.tetradapp.knowledge_editor;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;

/**
 * Tyler was lazy and didn't document this....
 *
 * @author Tyler Gibson
 */
class ListTransferable implements Transferable {

    /**
     * The list of graph nodes that constitutes the selection.
     */
    private List list;

    /**
     * Supported dataflavors--only one.
     */
    private final static DataFlavor[] dataFlavors = new DataFlavor[]{
            new DataFlavor(ListTransferable.class, "String List Selection")};


    public final static DataFlavor DATA_FLAVOR = dataFlavors[0];


    /**
     * Constructs a new selection with the given list of graph nodes.
     */
    public ListTransferable(List list) {
        if (list == null) {
            throw new NullPointerException(
                    "List of list must " + "not be null.");
        }

        this.list = list;
    }

    /**
     * Returns an object which represents the data to be transferred.  The
     * class of the object returned is defined by the representation class
     * of the flavor.
     *
     * @param flavor the requested flavor for the data
     * @throws java.io.IOException if the data is no longer available
     *                             in the requested flavor.
     * @throws java.awt.datatransfer.UnsupportedFlavorException
     *                             if the requested data flavor is
     *                             not supported.
     * @see DataFlavor#getRepresentationClass
     */
    public Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException, IOException {
        if (!isDataFlavorSupported(flavor)) {
            throw new UnsupportedFlavorException(flavor);
        }

        return list;
    }

    /**
     * Returns whether or not the specified data flavor is supported for
     * this object.
     *
     * @param flavor the requested flavor for the data
     * @return boolean indicating whether or not the data flavor is
     *         supported
     */
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.equals(dataFlavors[0]);
    }

    /**
     * Returns an array of DataFlavor objects indicating the flavors the
     * data can be provided in.  The array should be ordered according to
     * preference for providing the data (from most richly descriptive to
     * least descriptive).
     *
     * @return an array of data flavors in which this data can be
     *         transferred
     */
    public DataFlavor[] getTransferDataFlavors() {
        return dataFlavors;
    }
}
