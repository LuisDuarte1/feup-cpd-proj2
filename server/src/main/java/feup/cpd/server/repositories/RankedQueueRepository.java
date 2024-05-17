package feup.cpd.server.repositories;

import feup.cpd.server.collections.BidirectionalUniqueMap;
import feup.cpd.server.collections.BucketTimedQueue;
import feup.cpd.server.collections.Pair;
import feup.cpd.server.concurrent.helper.RWLockedValue;
import feup.cpd.server.handlers.RankedQueueMatchmakerHandler;
import feup.cpd.server.handlers.RankedQueuePropagateHandler;
import feup.cpd.server.models.Player;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class RankedQueueRepository extends Repository {

    private static RankedQueueRepository instance = null;

    private static final int ELO_INTERVAL = 200;

    private static final int NUM_PLAYERS = 4;

    private static final String RANKED_QUEUE_REPOSITORY_PATH = "rankedQueue.ser";


    public static RankedQueueRepository getInstance(ExecutorService executorService){
        if(instance == null){
            instance = new RankedQueueRepository(executorService);
            executorService.submit(new RankedQueueMatchmakerHandler(executorService, NUM_PLAYERS));
            executorService.submit(new RankedQueuePropagateHandler(executorService));
        }
        return instance;
    }

    private RankedQueueRepository(ExecutorService executorService) {
        super(executorService);
        this.playersInQueue = new RWLockedValue<>(
                new BucketTimedQueue<>(ELO_INTERVAL)
        );
        this.ioLock = new ReentrantLock();
        read();
    }
    RWLockedValue<BucketTimedQueue<Pair<String, UUID>>> playersInQueue;

    final ReentrantLock ioLock;

    void save(){
        ioLock.lock();
        try {


            FileOutputStream fileOutputStream = new FileOutputStream(RANKED_QUEUE_REPOSITORY_PATH);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            playersInQueue.lockAndRead((buckets) -> {
                try {
                    objectOutputStream.writeObject(buckets.bucketList);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return null;
            });
            objectOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            ioLock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    void read(){
        ioLock.lock();
        try {
            if(!(new File(RANKED_QUEUE_REPOSITORY_PATH).exists())){
                return;
            }

            FileInputStream fileInputStream = new FileInputStream(RANKED_QUEUE_REPOSITORY_PATH);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

            //trust me bro check
            List<List<BucketTimedQueue.BucketEntry<Pair<String, UUID>>>> readQueue =
                    (List<List<BucketTimedQueue.BucketEntry<Pair<String, UUID>>>>) objectInputStream.readObject();
            playersInQueue = new RWLockedValue<>(
                    new BucketTimedQueue<>(ELO_INTERVAL, readQueue)
            );
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            ioLock.unlock();
        }
    }



    public UUID addToQueue(String name, int elo){
        var token = UUID.randomUUID();
        playersInQueue.lockAndWrite((val) -> {
                val.put(new Pair<>(name, token), elo);
                return null;
        });
        saveAsync();
        return token;
    }

    public boolean checkIfUserInQueue(String name){
        return playersInQueue.lockAndRead((val) ->
            val.contains((pair) -> Objects.equals(pair.first(), name)));
    }

    public void reAddGameCandidates(Map<String, UUID> gameCandidates){
        var playerRepo = PlayerRepository.getInstance(executorService);
        gameCandidates.forEach((k, v) -> {
            final int elo = playerRepo.getFromPlayer(k, Player::getElo);

            addToQueue(k, elo);
        });
    }

    public List<Map<String, UUID>> getGameCandidates(int numPlayers){
        var gameCandidates = playersInQueue.lockAndWrite((buckets) -> {
            return buckets.getPossibleMatches(numPlayers).map((match) -> {
                final Map<String, UUID> game = new HashMap<>();
                match.forEach((pair -> game.put(pair.first(), pair.second())));
                return game;
            }).collect(Collectors.toList());
        });
        saveAsync();
        return  gameCandidates;
    }

    public void propagateBuckets(int timeout){
        playersInQueue.lockAndWrite((buckets) -> {
            buckets.propagateBucket(timeout);
            return null;
        });
        saveAsync();
    }

}
