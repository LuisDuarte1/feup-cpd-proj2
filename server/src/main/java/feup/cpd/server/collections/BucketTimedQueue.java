package feup.cpd.server.collections;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class BucketTimedQueue<T> implements Serializable {

    public static class BucketEntry<T> implements Serializable{
        public final T val;
        public LocalDateTime localDateTime;

        public int minBucket;

        BucketEntry(T val, LocalDateTime localDateTime, int minBucket) {
            this.val = val;
            this.localDateTime = localDateTime;
            this.minBucket = minBucket;
        }
    }

    final int eloInterval;

    //store a bucket of T, with the last time "updated"
    public final List<List<BucketEntry<T>>> bucketList;

    public BucketTimedQueue(int eloInterval) {
        this.eloInterval = eloInterval;
        bucketList = new ArrayList<>();
    }

    public BucketTimedQueue(int eloInterval, List<List<BucketEntry<T>>> bucketList) {
        this.eloInterval = eloInterval;
        this.bucketList = bucketList;
    }

    public void put(T value, int elo){
        final int bucket = (int) Math.floor((double) elo/eloInterval);

        if(bucket - bucketList.size() >= 0){
            bucketList.addAll(
                    Stream.generate(() -> new ArrayList<BucketEntry<T>>())
                        .limit(bucket - bucketList.size() + 1)
                        .toList()
            );
        }
        //timezones do not matter because it's server only
        bucketList.get(bucket).add(new BucketEntry<>(value, LocalDateTime.now(), bucket));
    }

    public Stream<List<T>> getPossibleMatches(int matchSize){
        return bucketList.stream().map((bucket) -> {
            if (bucket.size() < matchSize) return new ArrayList<List<T>>();
            List<List<T>> matches = new ArrayList<>();

            for(int i = 0; i < bucket.size()/matchSize; i++){
                List<T> match = new ArrayList<>(matchSize);
                for(int e = 0; e < matchSize; e++){
                    match.add(bucket.get(i*matchSize+e).val);
                }
                matches.add(match);
            }
            //remove from matches queue
            IntStream.range(0, (bucket.size()/matchSize)*matchSize).forEach((e) -> {
                var removed = bucket.removeFirst();
                bucketList.forEach((otherBucket) -> {
                    if (otherBucket.equals(bucket)) return;
                    otherBucket.remove(removed);
                });
            });

            return matches;
        }).flatMap(Collection::stream);
    }

    public void propagateBucket(int maxTimeoutSeconds){
        for(int i = 1; i < bucketList.size(); i++){
            for(var entry : bucketList.get(i)){
                final long difference = ChronoUnit.SECONDS.between(LocalDateTime.now(), entry.localDateTime);
                if(difference >= maxTimeoutSeconds && entry.minBucket > 0){
                    entry.localDateTime = LocalDateTime.now();
                    bucketList.get(i-1).add(entry);
                    entry.minBucket -= 1;
                }
            }
        }
    }

    public boolean contains(Function<T, Boolean> function){
        for(var bucket : bucketList){
            for(var entry: bucket){
                if(function.apply(entry.val)) return true;
            }
        }
        return false;
    }
}
