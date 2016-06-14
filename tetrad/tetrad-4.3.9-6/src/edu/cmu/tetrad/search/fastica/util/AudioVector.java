package edu.cmu.tetrad.search.fastica.util;

import edu.cmu.tetrad.search.fastica.math.Matrix;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;

/**
 * The <code>AudioVector</code> class provides some useful static functions to
 * compute audio signals.
 *
 * @author Michael Lambertz
 */

public class AudioVector {

    public static double[] readAudioFile(
            File audioFile,
            float sampleRate)
            throws IOException, UnsupportedAudioFileException {
        AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
        AudioFormat audioFormat =
                new AudioFormat(sampleRate, 16, 1, true, true);
        DataInputStream dataStream =
                new DataInputStream(
                        new BufferedInputStream(
                                AudioSystem.getAudioInputStream(audioFormat, audioStream)));
        int m = (int) audioStream.getFrameLength();
        double[] audioData = new double[m];
        for (int i = 0; i < m; ++i)
            audioData[i] = dataStream.readShort() / ((double) -Short.MIN_VALUE);
        return (audioData);
    }

    public static double[] normalise(
            double[] audioData,
            double value) {
        double max = 0.0;
        int m = audioData.length;
        double tmp;
        // determine absolute maximum
        for (int i = 0; i < m; ++i) {
            tmp = Math.abs(audioData[i]);
            if (tmp > max)
                max = tmp;
        }
        // copy and normalise audio data
        double[] audioDataRes = new double[m];
        max = value / max;
        for (int i = 0; i < m; ++i)
            audioDataRes[i] = audioData[i] * max;
        return (audioDataRes);
    }

    public static double[] normalise(
            double[] audioData) {
        return (normalise(audioData, 1.0));
    }

    public static double[][] toMatrix(
            double[][] audioVectors) {
        int m = audioVectors.length;
        int n = 0;
        for (int i = 0; i < m; ++i) {
            int tmp = audioVectors[i].length;
            if (tmp > n)
                n = tmp;
        }
        double[][] audioMatrix = Matrix.newMatrix(m, n);
        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < audioVectors[i].length; ++j)
                audioMatrix[i][j] = audioVectors[i][j];
            for (int j = audioVectors[i].length; j < n; ++j)
                audioMatrix[i][j] = 0.0;
        }
        return (audioMatrix);
    }

    public static AudioInputStream toAudioInputStream(
            double[] audioData,
            float sampleRate) {
        int m = audioData.length;
        AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
        // build byte array
        byte[] byteArray = new byte[m * 2];
        int val;
        for (int i = 0; i < m; ++i) {
            val = (int) (audioData[i] * Short.MAX_VALUE);
            if (val < 0) {
                val = -val - 1;
                byteArray[i * 2] = (byte) (~(val & 255));
                byteArray[i * 2 + 1] = (byte) (~(val >> 8));
            }
            else {
                byteArray[i * 2] = (byte) (val & 255);
                byteArray[i * 2 + 1] = (byte) (val >> 8);
            }
        }
        AudioInputStream audioStream =
                new AudioInputStream(new ByteArrayInputStream(byteArray), format, m);
        return (audioStream);
    }

}

