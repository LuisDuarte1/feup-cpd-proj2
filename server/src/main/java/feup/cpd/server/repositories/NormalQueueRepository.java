package feup.cpd.server.repositories;

import feup.cpd.server.concurrent.ConcurrentRWMap;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantLock;

public class NormalQueueRepository extends Repository {

    private static NormalQueueRepository instance = null;

    private static final String NORMAL_QUEUE_REPOSITORY_PATH = "normalQueue.ser";


    public static NormalQueueRepository getInstance(ExecutorService executorService){
        if(instance == null){
            instance = new NormalQueueRepository(executorService);
        }
        return instance;
    }

    private NormalQueueRepository(ExecutorService executorService) {
        super(executorService);
        this.playersInQueue = new ConcurrentRWMap<>();
        this.ioLock = new ReentrantLock();
        read();
    }
    ConcurrentRWMap<String, UUID> playersInQueue;

    final ReentrantLock ioLock;

    void save(){
        ioLock.lock();
        try {
            var tempQueue = playersInQueue.getAll((v) -> v);
            FileOutputStream fileOutputStream = new FileOutputStream(NORMAL_QUEUE_REPOSITORY_PATH);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

            objectOutputStream.writeObject(tempQueue);
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
            if(!(new File(NORMAL_QUEUE_REPOSITORY_PATH).exists())){
                return;
            }

            FileInputStream fileInputStream = new FileInputStream(NORMAL_QUEUE_REPOSITORY_PATH);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

            //trust me bro check
            Map<String, UUID> readQueue = (Map<String, UUID>) objectInputStream.readObject();
            playersInQueue = new ConcurrentRWMap<>(readQueue);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            ioLock.unlock();
        }
    }



    public UUID addToQueue(String name){
        var token = UUID.randomUUID();
        playersInQueue.put(name, token);
        saveAsync();
        return token;
    }

    public void reAddGameCandidates(Map<String, UUID> gameCandidates){
        playersInQueue.putAll(gameCandidates);
        saveAsync();
    }

    public boolean checkIfGameCanBeStarted(int numPlayers){
        return playersInQueue.size() >= numPlayers;
    }

    public Map<String, UUID> getGameCandidates(int numPlayers){
        var gameCandidates = playersInQueue.removeCount(numPlayers);
        saveAsync();
        return  gameCandidates;
    }

}
