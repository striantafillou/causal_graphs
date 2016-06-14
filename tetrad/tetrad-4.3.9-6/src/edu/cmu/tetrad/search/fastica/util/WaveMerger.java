package edu.cmu.tetrad.search.fastica.util;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;

/**
 * This <code>WaveMerger</code> class can be used to merge two sampled mono
 * audio files together to one sampled stereo audio file.
 *
 * @author Michael Lambertz
 */

public class WaveMerger {

    /**
     * The <code>main</code> method can be called to merge two mono audio files to
     * one stereo audio file.
     *
     * @param args expects two filenames of the mono input files and one output
     *             filename
     */
    public static void main(String[] args) {
        try {
            AudioInputStream aistream0 = AudioSystem.getAudioInputStream(new File(args[0]));
            AudioInputStream aistream1 = AudioSystem.getAudioInputStream(new File(args[1]));
            float sampleRate = aistream0.getFormat().getSampleRate();
            AudioFormat format = new AudioFormat(sampleRate, 16, 1, false, false);
            DataInputStream distream0 =
                    new DataInputStream(new BufferedInputStream(AudioSystem.getAudioInputStream(format, aistream0)));
            DataInputStream distream1 =
                    new DataInputStream(new BufferedInputStream(AudioSystem.getAudioInputStream(format, aistream1)));
            int n = (int) aistream0.getFrameLength();
            byte[] data = new byte[n * 4];
            int val;
            for (int j = 0; j < n; ++j) {
                val = (int) distream0.readShort();
                data[j * 4 + 1] = (byte) (val & 255);
                data[j * 4 + 0] = (byte) (val >> 8);
                val = (int) distream1.readShort();
                data[j * 4 + 3] = (byte) (val & 255);
                data[j * 4 + 2] = (byte) (val >> 8);
            }
            format = new AudioFormat(sampleRate, 16, 2, false, false);
            AudioInputStream ostream = new AudioInputStream(new ByteArrayInputStream(data), format, n);
            AudioSystem.write(ostream, AudioFileFormat.Type.WAVE, new File(args[2]));
        }
        catch (Exception exc) {
            exc.printStackTrace(System.err);
        }
    }

}
