/*
* Many thanks to @Radiodef for his amazing post on StackOverflow
* describing how to properly decode WAV files with Java.sound.sampled lib
* Some (almost all...) code blocks were directly copied from his post with slight modifications
*
* This code focuses specifically on decoding PCM_SIGNED WAV files for simplicity
* */


import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import static java.lang.Math.*;


public class WaveFormFileRead {

    public static void main(String[] args) {

        String testFile = "JavaSoundTest/Resources/Reference_SineWave_500Hz_0dBFS_0.01s.wav";

        AudioFileFormat audioFileFormat = getFileAudioFormat(testFile);
        AudioFormat audioFormat = audioFileFormat.getFormat();
        int bitsPerSample = audioFormat.getSampleSizeInBits();
        boolean isBigEndian = audioFormat.isBigEndian();

        byte[] byteSamplesArray = read(testFile, audioFormat);
        long[] tempSamplesArray = unpackBits(byteSamplesArray, isBigEndian, bitsPerSample);
        tempSamplesArray = extendSign(tempSamplesArray, bitsPerSample);
        float[] samples = new float[tempSamplesArray.length];
        long max = Long.MIN_VALUE;
        long min = Long.MAX_VALUE;
        for(int i = 0; i < samples.length; i++){
            samples[i] = (float) tempSamplesArray[i];
           max = Math.max(max, tempSamplesArray[i]);
           min = Math.min(min, tempSamplesArray[i]);
        }
        System.out.println("MAX: " + max + "; MIN: " + min);
    }

    private static AudioFileFormat getFileAudioFormat(String filePath){
        File file = new File(filePath);
        AudioFileFormat audioFileFormat = null;
        try {
            audioFileFormat = AudioSystem.getAudioFileFormat(file);
        } catch (UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
        }
        return audioFileFormat;
    }

    private static byte[] read(String filePath, AudioFormat audioFormat) {
        File file = new File(filePath);
        byte[] waveByteArray = null;
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
            if(audioFormat.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) throw new UnsupportedAudioFileException();
            int bytesPerFrame = audioFormat.getFrameSize();

            byte[] readBuffer = new byte[bytesPerFrame];
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            while ((audioInputStream.read(readBuffer)) != -1) outputStream.write(readBuffer);
            waveByteArray = outputStream.toByteArray();
        } catch (IOException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
        return waveByteArray;
    }

    private static long[] extendSign(long[] temp, int bitsPerSample) {
        int bitsToExtend = Long.SIZE - bitsPerSample;
        for (int i = 0; i < temp.length; i++) {
            temp[i] = (temp[i] << bitsToExtend) >> bitsToExtend;
        }
        return temp;
    }

    /**
     * Computes the largest magnitude representable by the audio format,
     * using Math.pow(2.0, bitsPerSample - 1). Note that for two's complement
     * audio, the largest positive value is one less than the return value of
     * this method.
     * The result is returned as a double because in the case that
     * bitsPerSample is 64, a long would overflow.
     *
     * @param bitsPerSample the return value of AudioFormat.getBitsPerSample
     * @return the largest magnitude representable by the audio format
     */
    public static double fullScale(int bitsPerSample) {
        return pow(2.0, bitsPerSample - 1); // optimization: (1L << (bitsPerSample - 1))
    }

    /* Unpacks given sound sample byteArray into Long temp variable, based on bitrate
    * */

    private static long[] unpackBits(byte[]  bytes, boolean isBigEndian, int bitsPerSample) {
        switch (bitsPerSample) {
            case  8: return unpack8Bit(bytes);
            case  16: return unpack16Bit(bytes, isBigEndian);
            case  24: return unpack24Bit(bytes, isBigEndian);
            default: return unpackAnyBit(bytes, isBigEndian, bitsPerSample);
        }
    }

    private static long[] unpack8Bit(byte[] bytes) {
        long[] samples = new long[bytes.length];
        for(int i = 0; i < samples.length; i ++) {
            samples[i] = bytes[i] & 0xffL;
        }
        return samples;
    }

    private static long[] unpack16Bit(byte[]  bytes, boolean isBigEndian) {
        int size = (int) ceil(bytes.length/2);
        long[] samples = new long[size];
        for(int i = 0; i < size; i ++) {
            if (isBigEndian) {
                samples[i] = (((bytes[i] & 0xffL) << 8) |  (bytes[i + 1] & 0xffL));
            } else {
                samples[i] = ((bytes[i] & 0xffL) | ((bytes[i + 1] & 0xffL) << 8));
            }
        }
        return samples;
    }

    private static long[] unpack24Bit(byte[]  bytes, boolean isBigEndian) {
        int size = (int) ceil(bytes.length/3);
        long[] samples = new long[size];
        for(int i = 0; i < size; i ++) {
            if (isBigEndian) {
                samples[i] = (((bytes[i] & 0xffL) << 16) | ((bytes[i + 1] & 0xffL) << 8) | (bytes[i + 2] & 0xffL));
            } else {
                samples[i] = ((bytes[i] & 0xffL) | ((bytes[i + 1] & 0xffL) << 8) | ((bytes[i + 2] & 0xffL) << 16));
            }
        }
        return samples;
    }

    private static long[] unpackAnyBit(byte[]  bytes, boolean isBigEndian, int bitsPerSample) {
        int bytesPerSample = bitsPerSample*8;
        int size = (int) ceil(bytes.length/bytesPerSample);
        long[] samples = new long[size];
        for(int i = 0; i < size; i ++) {
            if (isBigEndian) {
              for (int b = 0; b < bitsPerSample; b++) samples[i] |= (bytes[i + b] & 0xffL) << (8 * (bytesPerSample - b - 1));
            } else {
             for (int b = 0; b < bytesPerSample; b++) samples[i] |= (bytes[i + b] & 0xffL) << (8 * b);
            }
        }
        return samples;
    }

}