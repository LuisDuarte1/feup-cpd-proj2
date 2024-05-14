package feup.cpd.server.repositories;

import feup.cpd.server.concurrent.ConcurrentRWList;
import feup.cpd.server.concurrent.ConcurrentRWMap;
import feup.cpd.server.models.Player;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

public class PlayerRepository {

    private static PlayerRepository instance = null;

    private static final String PLAYER_REPOSITORY_PATH = "playerRepo.ser";


    public static PlayerRepository getInstance(ExecutorService executorService){
        if(instance == null){
            instance = new PlayerRepository(executorService);
        }
        return instance;
    }

    private PlayerRepository(ExecutorService executorService) {
        this.registeredPlayerList = new ConcurrentRWMap<>();
        this.ioLock = new ReentrantLock();
        readPlayerList();
    }
    ConcurrentRWMap<String, Player> registeredPlayerList;

    final ReentrantLock ioLock;

    void savePlayerList(){
        ioLock.lock();
        try {
            List<Player> tempPlayers = registeredPlayerList.getAllValues(Player::clonePlayer);
            FileOutputStream fileOutputStream = new FileOutputStream(PLAYER_REPOSITORY_PATH);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

            objectOutputStream.writeObject(tempPlayers);
            objectOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            ioLock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    void readPlayerList(){
        ioLock.lock();
        try {
            if(!(new File(PLAYER_REPOSITORY_PATH).exists())){
                return;
            }

            FileInputStream fileInputStream = new FileInputStream(PLAYER_REPOSITORY_PATH);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

            //trust me bro check
            List<Player> players = (List<Player>) objectInputStream.readObject();
            Map<String, Player> playerMap = new HashMap<>();
            players.forEach(player -> playerMap.put(player.name, player));
            registeredPlayerList = new ConcurrentRWMap<>(playerMap);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            ioLock.unlock();
        }
    }

    public void savePlayer(Player player){
        registeredPlayerList.put(player.name, player);
        savePlayerList();
    }

    public void mutatePlayer(String name, Function<Player, String> mutateFunc){
        registeredPlayerList.mutateValue(name, mutateFunc);
        savePlayerList();
    }

    public <R> R getFromPlayer(String name, Function<Player, R> getFunc){
        return registeredPlayerList.get(name, getFunc);
    }

    public boolean checkIfPlayerExists(String name){
        return registeredPlayerList.get(name, (player) -> player) != null;
    }

}
