package edu.cmu.tetrad.search.fastica.util;

import edu.cmu.tetrad.search.fastica.math.Matrix;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;

/**
 * This <code>AudioBuffer</code> class can be used to comfortably process audio
 * data.
 *
 * @author Michael Lambertz
 */

public class AudioBuffer {

    private double[][] data;
    private float sampleRate;

    /**
     * This constructor creates an <code>AudioBuffer</code> object, which contains
     * the given sampled audio data at the given sample rate.
     *
     * @param data       the sampled data
     * @param sampleRate the data's sample rate
     */
    public AudioBuffer(double[][] data, float sampleRate) {
        this.data = Matrix.clone(data);
        this.sampleRate = sampleRate;
    }

    /**
     * This constructor creates an <code>AudioBuffer</code> object by reading
     * sampled audio data from a file.
     *
     * @param file the file, which contains the sampled data
     * @throws IOException                   if there is an error reading the file
     * @throws UnsupportedAudioFileException if the audio file format is not
     *                                       supported
     */
    public AudioBuffer(File file)
            throws IOException, UnsupportedAudioFileException {
        AudioInputStream aistream = AudioSystem.getAudioInputStream(file);
        sampleRate = aistream.getFormat().getSampleRate();
        int chs = aistream.getFormat().getChannels();
        AudioFormat format =
                new AudioFormat(
                        sampleRate,
                        16,
                        chs,
                        true,
                        true);
        DataInputStream distream =
                new DataInputStream(
                        new BufferedInputStream(
                                AudioSystem.getAudioInputStream(format, aistream)));
        int n = (int) aistream.getFrameLength();
        data = new double[chs][];
        for (int i = 0; i < chs; ++i)
            data[i] = new double[n];
        for (int j = 0; j < n; ++j)
            for (int i = 0; i < chs; ++i)
                data[i][j] = distream.readShort() / ((double) -Short.MIN_VALUE);
    }

    /**
     * Returns the sampled audio data.
     *
     * @return the sampled audio data
     */
    public double[][] getData() {
        return (data);
    }

    /**
     * Returns the sample rate of the audio data.
     *
     * @return the sample rate
     */
    public float getSampleRate() {
        return (sampleRate);
    }

    /**
     * Returns an <code>AudioInputStream</code> object, which can be used to play
     * the content of this <code>AudioBuffer</code> object.
     *
     * @return the <code>AudioInputStream</code>
     */
    public AudioInputStream getStream() {
        int chs = Matrix.getNumOfRows(data);
        int len = Matrix.getNumOfColumns(data);
        AudioFormat format = new AudioFormat(sampleRate, 16, chs, true, false);
        // determine absolute maximum
        double max = 0.0;
        for (int i = 0; i < chs; ++i)
            for (int j = 0; j < len; ++j)
                if (Math.abs(data[i][j]) > max)
                    max = Math.abs(data[i][j]);
        max = 32000 / max;
        // build byte array
        byte[] byteArray = new byte[len * chs * 2];
        int val;
        for (int j = 0; j < len; ++j)
            for (int i = 0; i < chs; ++i) {
                val = (int) (data[i][j] * max);
                if (val < 0) {
                    val = -val - 1;
                    byteArray[j * chs * 2 + i * 2] = (byte) (~(val & 255));
                    byteArray[j * chs * 2 + i * 2 + 1] = (byte) (~(val >> 8));
                }
                else {
                    byteArray[j * chs * 2 + i * 2] = (byte) (val & 255);
                    byteArray[j * chs * 2 + i * 2 + 1] = (byte) (val >> 8);
                }
            }
        AudioInputStream stream = new AudioInputStream(new ByteArrayInputStream(byteArray), format, len);
        return (stream);
    }

}
