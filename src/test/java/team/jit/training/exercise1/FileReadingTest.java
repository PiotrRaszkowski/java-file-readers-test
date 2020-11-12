package team.jit.training.exercise1;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.io.BufferedReader;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FileReadingTest {

    private static final String TEST_FILE = "src/test/resources/63556-0.txt";

    @Test
    public void runBenchmarks() throws Exception {
        Options options = new OptionsBuilder()
                .include(this.getClass().getName() + ".*")
                .mode(Mode.AverageTime)
                .warmupTime(TimeValue.seconds(1))
                .warmupIterations(10)
                .threads(1)
                .measurementIterations(10)
                .forks(1)
                .shouldFailOnError(true)
                .shouldDoGC(true)
                .addProfiler(GCProfiler.class)
                .build();

        new Runner(options).run();
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void bufferedReader() throws Exception {
        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(TEST_FILE));

        long fileLength = 0;
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            fileLength += line.length();
        }

        Assertions.assertEquals(419450, fileLength);
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void bufferedReaderWithStreamOfLines() throws Exception {
        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(TEST_FILE));

        int fileLength = bufferedReader.lines().mapToInt(String::length).sum();

        Assertions.assertEquals(419450, fileLength);
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void filesReadAllLines() throws Exception {
        List<String> allLines = Files.readAllLines(Paths.get(TEST_FILE));

        long fileLength = 0;
        for (String line : allLines) {
            fileLength += line.length();
        }

        Assertions.assertEquals(419450, fileLength);
    }


    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void fileChannell() throws Exception {
        RandomAccessFile reader = new RandomAccessFile(TEST_FILE, "r");
        FileChannel channel = reader.getChannel();

        int bufferSize = 1024;

        ByteBuffer buff = ByteBuffer.allocate(bufferSize);

        long fileLenght = 0;
        while(channel.read(buff) > 0) {
            buff.flip();
            String content = new String(buff.array());
            fileLenght += content.length();
            buff.clear();
        }

        Assertions.assertEquals(434061, fileLenght);
    }
}
