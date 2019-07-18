import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class OutputBufferTest {
    private static final int SAMPLE_RATE = 44100;
    private static final int BIT_DEPTH = 16;
    private static final int NUM_CH = 1;
    private static final int FREQ1 = 1000;
    private static final int FREQ2 = 200;

    // Определяет размер выходной буффер из массива байтов длиной 0,002 мс (SAMPLE_RATE*BIT_DEPTH*2 - Байтрейт/сек =>> 1 сек буффера)
    private static final int BUFFER = SAMPLE_RATE*BIT_DEPTH*2/500;

    public static void main(String[] args) {
        // Определяет формат выходного звука с частотой дискретизации 44100 Гц, глубиной битности 16, с 1 моно-каналом в режиме кодирования PCM
        final AudioFormat audioFormat = new AudioFormat(SAMPLE_RATE, BIT_DEPTH, NUM_CH, true, true);
        try(SourceDataLine line = AudioSystem.getSourceDataLine(audioFormat)) {
            line.open(audioFormat, BUFFER);
            line.start();
            // Симмулирует подачу 2-х нот длиной 2 секунды с паузой в 1 секунду
            play(line);
            // Воспроизводит остаток данных в буффере до его опустошения. Блокирует буффер до завершения метода. Останавливает, закрывает и обнуляет поток.
            line.drain();
            line.stop();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    private static void play(SourceDataLine line) {
        byte[] audioByteArray = getAudioByteArray();
        int read = 0;
        int totalToRead = audioByteArray.length;
        /*  Цикл пытается записать в буфер вывода все значения "нот" хранящиелся в промежуточном буфере "синтезатора"
            line.available() предоставляет кол-во байтов, доступных для записи в буффер вывода
            Данный цикл в идеале работает в отдельной ветке бесперерывно*/
        while (read < totalToRead){
            int available = line.available();
            if( (read+available) < totalToRead ) available = totalToRead - read;
            line.write(audioByteArray, read, available);
            read+=available;
        }
    }

    private static byte[] getAudioByteArray() {
        //длинна звучания 5 секунд получается из: частоты дискретизации 44100 Гц; глубины битности 16 (8*2 байт), в течении 5 сек
        final int LENGTH = SAMPLE_RATE * 2 * 5;
        final byte[] arr = new byte[LENGTH];
        final int NOTE_LENGTH = SAMPLE_RATE * 2 * 2;
        final int SILENCE_LENGTH = SAMPLE_RATE * 2;
        int counter = 0;
        //Запись первой ноты
        for(int i = 0; i < LENGTH; i++) {
            double angle =  (2.0 * Math.PI * FREQ1 * i) / SAMPLE_RATE;
            arr[i] = (byte) (Math.sin(angle) * 65);
        }
        counter+=NOTE_LENGTH;
        //Пауза между нотами
        for(int i = counter; i < NOTE_LENGTH + SILENCE_LENGTH; i++) {
            arr[i] = (byte) 0;
        }
        counter+=SILENCE_LENGTH;
        //Запись второй ноты
        for(int i = counter; i < LENGTH; i++) {
            double angle = (2.0 * Math.PI * FREQ2 * i) / SAMPLE_RATE;
            arr[i] = (byte) (Math.sin(angle) * 65);
        }
        return arr;
    }
}
