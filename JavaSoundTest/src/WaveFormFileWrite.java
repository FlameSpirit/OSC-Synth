import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;


public class WaveFormFileWrite {
    private static final int SAMPLE_RATE = 41000;
    private static final int BIT_DEPTH = 16;
    private static final int NUM_CH = 1;
    private static final int FREQ1 = 50;
    private static final int AMPLITUDE = 65;
    private static final int PRECISION = 50;
    private static final int DURATION = 15;
    // Определяет размер выходной размер (длительность) аудио файла в виде размера для байтового массива данных (SAMPLE_RATE*BIT_DEPTH/8 - Байтрейт/сек =>> 1 сек буффера)
    private static final int SIZE = SAMPLE_RATE*DURATION*BIT_DEPTH/8;
    private enum WaveType { Sine, Saw, Square, Triangle }


    public static void main(String[] args) {
        //Определяет формат выходного звука с частотой дискретизации 44100 Гц, глубиной битности 16, с 1 моно-каналом в режиме кодирования PCM
        AudioFormat audioFormat = new AudioFormat(SAMPLE_RATE, BIT_DEPTH, NUM_CH, true, true);
        save(WaveType.Sine, "out/Testing Files/Sine.wav", audioFormat);
        save(WaveType.Saw, "out/Testing Files/Saw.wav", audioFormat);
        save(WaveType.Square, "out/Testing Files/Square.wav", audioFormat);
}

    private static void save(WaveType waveType, String filename, AudioFormat audioFormat) {
        ByteArrayInputStream waveData;
        AudioInputStream waveStream;
        byte[] InputWaveStream;
        switch (waveType){
            case Sine:
                InputWaveStream = getByteArrayOfWave(generateSineWave(FREQ1, AMPLITUDE));
                waveData = new ByteArrayInputStream(InputWaveStream);
                waveStream  = new AudioInputStream(waveData, audioFormat, SIZE);
                try {
                    AudioSystem.write(waveStream, AudioFileFormat.Type.WAVE, new File(filename));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case Saw:
                InputWaveStream = getByteArrayOfWave(generateSawWave(FREQ1, AMPLITUDE, PRECISION));
                waveData = new ByteArrayInputStream(InputWaveStream);
                waveStream = new AudioInputStream(waveData, audioFormat, SIZE);
                try {
                    AudioSystem.write(waveStream, AudioFileFormat.Type.WAVE, new File(filename));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case Square:
                InputWaveStream = getByteArrayOfWave(generateSquareWave(FREQ1, AMPLITUDE, PRECISION));
                waveData = new ByteArrayInputStream(InputWaveStream);
                waveStream = new AudioInputStream(waveData, audioFormat, SIZE);
                try {
                    AudioSystem.write(waveStream, AudioFileFormat.Type.WAVE, new File(filename));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case Triangle:
                InputWaveStream = getByteArrayOfWave(generateTriangleWave(FREQ1, AMPLITUDE, PRECISION));
                waveData = new ByteArrayInputStream(InputWaveStream);
                waveStream = new AudioInputStream(waveData, audioFormat, SIZE);
                try {
                    AudioSystem.write(waveStream, AudioFileFormat.Type.WAVE, new File(filename));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            default: break;
        }
    }


    private static byte[] getByteArrayOfWave(double[] doubleArrayOfWave){
        byte[] byteArrayOfWave = new byte[doubleArrayOfWave.length];
        for(int i = 0; i < doubleArrayOfWave.length; i++) {
            byteArrayOfWave[i] = (byte) doubleArrayOfWave[i];
        }
        return byteArrayOfWave;
    }

    private static double[] generateSineWave(int freq, double amp){
        final double[] sine = new double[SIZE];
            for(int i = 0; i < SIZE; i++){
                // "8 / (SAMPLE_RATE * BIT_DEPTH)" требуется для корректного формирования частоты сигнала для работы с java.sound.sampled*
                // в противном случае частота либо уходит за пределы SAMPLE_RATE, либо сдвигается кратно BIT_DEPTH/8.
            double angle = (2*Math.PI * freq * i) * 8 / (SAMPLE_RATE * BIT_DEPTH);
            sine[i] = Math.sin(angle)*amp;
        }
        return sine;
    }

    private static double[] generateSawWave(int freq, double amp, int precision){
        final double[] saw = new double[SIZE];
        final double[][] harmonics = new double[precision][SIZE];

        //Получение синусоидальных гармоник кратных частот включая несущую частоту
        int multiplier;
        for(int i = 0; i < precision; i++){
            multiplier = i + 1;
            if(freq*multiplier > SAMPLE_RATE/2) break;
            double[] harmonic = generateSineWave(freq*multiplier, amp/multiplier);
            System.arraycopy(harmonic,0, harmonics[i],0, SIZE);
        }

        //Суммирование гармоник для получения пилообразной волны
        for(int j = 0; j < SIZE; j++){
            for(int i = 0; i < precision; i++){
                saw[j]+=harmonics[i][j];
            }
        }
        return saw;
    }

    private static double[] generateSquareWave(int freq, double amp, int precision){
        final double[] square = new double[SIZE];
        final double[][] harmonics = new double[precision][SIZE];

        //Получение синусоидальных гармоник нечетных кратных частот включая несушую частоту
        int multiplier;
        for(int i = 0; i < precision; i++){
            multiplier = 2*i + 1;
            if((freq*multiplier > SAMPLE_RATE/2)) break;
            double[] harmonic = generateSineWave(freq*multiplier, amp/multiplier);
            System.arraycopy(harmonic,0, harmonics[i],0, SIZE);
        }

        //Суммирование гармоник для получения волны квадрата
        for(int j = 0; j < SIZE; j++){
            for(int i = 0; i < precision; i++){
                square[j]+=harmonics[i][j];
            }
        }
        return square;
    }

    private static double[] generateTriangleWave(int freq, double amp, int precision){
        final double[] triangle = new double[SIZE];
        final double[][] harmonics = new double[precision][SIZE];

        //Получение синусоидальных гармоник нечетных кратных частот включая несущую частоту
        int multiplier;
        for(int i = 0; i < precision; i++){
            multiplier = 2*i + 1;
            if(freq*multiplier > SAMPLE_RATE/2) break;
            double[] harmonic = generateSineWave(freq*multiplier, amp/multiplier);
            System.arraycopy(harmonic,0, harmonics[i],0, SIZE);
        }

        //Суммирование гармоник для получения пилообразной волны при суммировании гармоник чередуется знак (+/-), начиная с "-"
        for(int j = 0; j < SIZE; j++){
            for(int i = 0; i < precision; i++){
                triangle[j]+= Math.pow(-1,i+1)*harmonics[i][j];
            }
        }
        return triangle;
    }
}
