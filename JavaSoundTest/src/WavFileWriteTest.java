import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

public class WavFileWriteTest {
    public static void main(String[] args) {
        int sampleRate = 192000;
        int bitsPerSample = 64;
        boolean isBigEndian = false;
        int freq = 1000;
        float amp_dB = 0.5f;
        int duration = 1; // in seconds
        AudioFormat audioFormat;
        audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, bitsPerSample, 1, bitsPerSample*2, sampleRate, isBigEndian);

        double[] sine = genSine(freq, sampleRate);
        long[] waveForm = genLongWave(sine, duration, sampleRate, bitsPerSample);


        byte[] byteArrayOfWave = packBits(waveForm, isBigEndian, bitsPerSample);
        ByteArrayInputStream waveData = new ByteArrayInputStream(byteArrayOfWave);
        AudioInputStream waveStream = new AudioInputStream(waveData, audioFormat, sampleRate);
        try {
            AudioSystem.write(waveStream, AudioFileFormat.Type.WAVE, new File("JavaSoundTest/out/Testing Files/Sine_Amp-3dBFS_Freq1kHz_64bit192kHz.wav"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //  Generates long-duration waveform, based on its single cycle
    private static long[] genLongWave(double[] wave, int duration, int sampleRate, int bitsPerSample){
        int longWaveSize = duration*sampleRate;
        int cycles = longWaveSize / wave.length;
        double[] samples = new double[longWaveSize];
        long[] longWave = new long[longWaveSize];
        for(int i = 0; i < cycles; i ++){
            System.arraycopy(wave, 0, samples, i*wave.length, wave.length);
        }

        double fullScale = fullScale(bitsPerSample);

        for(int i = 0; i < longWaveSize; i++){
            longWave[i] = (long) (samples[i] * fullScale);
        }
        return longWave;
    }

    //  Generates single cycle of sine wave based on a given frequency and sampleRate
    private static double[] genSine(int freq, int sampleRate){
        int samplesPerCycle = (int) Math.ceil(sampleRate/freq);
        double[] sine = new double[samplesPerCycle];
        for(int i = 0; i < samplesPerCycle; i++){
            //Formula derived based on time difference between each sample with given sampleRate
            double angle = (2 * Math.PI * freq * i / sampleRate);
            sine[i] = Math.sin(angle)/2;
        }
        return sine;
    }

    //  Pack given array of samples from Long to ByteArray to encode into wav format
    private static byte[] packBits(long[] samples, boolean isBigEndian, int bitsPerSample) {
        switch (bitsPerSample) {
            case  8: return pack8Bit(samples);
            case  16: return pack16Bit(samples, isBigEndian);
            case  24: return pack24Bit(samples, isBigEndian);
            default:return packAnyBit(samples, isBigEndian, bitsPerSample);
        }
    }

    private static byte[] pack8Bit(long[] samples) {
        int numOfBytes = samples.length;
        byte[] byteArray = new byte[numOfBytes];
        for(int i = 0; i < samples.length; i++){
            byteArray[i] = (byte) (samples[i] & 0xffL);
        }
        return byteArray;
    }

    private static byte[] pack16Bit(long[] samples, boolean isBigEndian) {
        int numOfBytes = samples.length*2;
        byte[] byteArray = new byte[numOfBytes];
        for(int i = 0; i < samples.length; i++) {
            if (isBigEndian) {
                byteArray[i*2] = (byte) ((samples[i] >>> 8) & 0xffL);
                byteArray[i*2 + 1] = (byte) (samples[i] & 0xffL);
            } else {
                byteArray[i*2] = (byte) (samples[i] & 0xffL);
                byteArray[i*2 + 1] = (byte) ((samples[i] >>> 8) & 0xffL);
            }
        }
        return byteArray;
    }

    private static byte[] pack24Bit(long[] samples, boolean isBigEndian) {
        int numOfBytes = samples.length*3;
        byte[] byteArray = new byte[numOfBytes];
        for(int i = 0; i < samples.length; i++) {
            if (isBigEndian) {
                byteArray[i*3] = (byte) ((samples[i] >>> 16) & 0xffL);
                byteArray[i*3 + 1] = (byte) ((samples[i] >>> 8) & 0xffL);
                byteArray[i*3 + 2] = (byte) (samples[i] & 0xffL);
            } else {
                byteArray[i*3] = (byte) (samples[i] & 0xffL);
                byteArray[i*3 + 1] = (byte) ((samples[i] >>> 8) & 0xffL);
                byteArray[i*3 + 2] = (byte) ((samples[i] >>> 16) & 0xffL);
            }
        }
        return byteArray;
    }

    private static byte[]  packAnyBit(long[] samples, boolean isBigEndian, int bitsPerSample) {
        int bytesPerSample = (int) Math.ceil(bitsPerSample/8);
        int numOfBytes = samples.length*bytesPerSample;
        byte[] byteArray = new byte[numOfBytes];
        int offset = 0;
        for(int i = 0; i < samples.length; i++) {
            offset = i*bytesPerSample;
            if (isBigEndian) {
                for (int b = 0; b < bytesPerSample; b++) byteArray[offset + b] = (byte) ((samples[i] >>> (8 * (bytesPerSample - b - 1))) & 0xffL);
            } else {
                for (int b = 0; b < bytesPerSample; b++) byteArray[offset + b] = (byte) ((samples[i]  >>> (8 * b)) & 0xffL);
            }
        }
        return byteArray;
    }

    //  Returns range of possible integer values of sample by it size in bits
    private static double fullScale(int bitsPerSample) {
        return Math.pow(2.0, bitsPerSample - 1); // optimization: (1L << (bitsPerSample - 1))
    }
}
