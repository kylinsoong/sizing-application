package org.teiid.sizing;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MapPerfBenchmark {
    
    private static final int SIZE = 500000;
    
    public static class Timer {
        long startTime = 0, endTime = 0, total = 0;
        
        public Timer(long startTime){
            this.startTime = startTime;
        }
        
        public void setEnd(long endTime) {
            if(this.endTime > 0) {
                return;
            }
            this.endTime = endTime;
        }
        
        public long getTotal() {
            return (endTime - startTime) / 1000000L;
        }
    }
    
    public Timer benchmark(final Map<String, Integer> map, int threads) throws InterruptedException{
        
        System.out.println("Performance Benchmark for " + map.getClass().getSimpleName() + ", with " + threads + " threads");
        
        final Timer timer = new Timer(System.nanoTime());
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        for(int i = 0 ; i < threads ; i ++){
            executor.execute(new Runnable(){

                @Override
                public void run() {
                    while(true){
                        int size = map.size();
                        if(size >= SIZE){
                            timer.setEnd(System.nanoTime());
                            break;
                        }
                        String key = UUID.randomUUID().toString();
                        map.put(key, 2048);
                        map.get(key);
                    }             
                }
            });
        }
        executor.shutdown();
        executor.awaitTermination(1000 * 10, TimeUnit.SECONDS);
        
        return timer;
    }

    public static void main(String[] args) throws InterruptedException {
        
        MapPerfBenchmark benchmark = new MapPerfBenchmark();
        
        System.out.println("500K entried added/retrieved in " + benchmark.benchmark(new ConcurrentHashMap<String, Integer>(), 1).getTotal() + " ms");
        
        System.out.println("500K entried added/retrieved in " + benchmark.benchmark(new ConcurrentHashMap<String, Integer>(), 4).getTotal() + " ms");
        
        System.out.println("500K entried added/retrieved in " + benchmark.benchmark(new ConcurrentHashMap<String, Integer>(), 8).getTotal() + " ms");
    }

}
