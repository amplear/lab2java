import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.*;

public class Main {
    
    public static void main(String[] args) {
        int minRange = 0;
        int maxRange = 100;
        int arraySize = getRandomNumber(40, 60);
        int[] numbers = generateRandomArray(arraySize, minRange, maxRange);
        int chunkSize = 10;
        List<int[]> chunks = splitArray(numbers, chunkSize);
        ExecutorService executor = Executors.newFixedThreadPool(chunks.size());
        List<Future<int[]>> futures = new ArrayList<>();
        long startTime = System.nanoTime();
        
        for (int[] chunk : chunks) {
            Callable<int[]> task = new PairwiseMultiplicationTask(chunk);
            Future<int[]> future = executor.submit(task);
            futures.add(future);
        }

        Set<Integer> results = new CopyOnWriteArraySet<>();
        for (Future<int[]> future : futures) {
            try {
                if (future.isDone() && !future.isCancelled()) {
                    int[] resultChunk = future.get();
                    for (int value : resultChunk) {
                        results.add(value);
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        
        executor.shutdown();

        long elapsedTime = System.nanoTime() - startTime;
        System.out.println("Результати обробки: " + results);
        System.out.println("Час виконання: " + (elapsedTime / 1_000_000) + " мс");
    }

    private static int[] generateRandomArray(int size, int min, int max) {
        int[] array = new int[size];
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            array[i] = random.nextInt(max - min + 1) + min;
        }
        return array;
    }

    private static List<int[]> splitArray(int[] array, int chunkSize) {
        List<int[]> chunks = new ArrayList<>();
        for (int i = 0; i < array.length; i += chunkSize) {
            int end = Math.min(array.length, i + chunkSize);
            int[] chunk = new int[end - i];
            System.arraycopy(array, i, chunk, 0, chunk.length);
            chunks.add(chunk);
        }
        return chunks;
    }

    private static int getRandomNumber(int min, int max) {
        return new Random().nextInt((max - min) + 1) + min;
    }

    static class PairwiseMultiplicationTask implements Callable<int[]> {
        private final int[] chunk;

        public PairwiseMultiplicationTask(int[] chunk) {
            this.chunk = chunk;
        }

        @Override
        public int[] call() {
            int[] result = new int[chunk.length / 2];
            for (int i = 0; i < chunk.length - 1; i += 2) {
                result[i / 2] = chunk[i] * chunk[i + 1];
            }
            return result;
        }
    }
}
