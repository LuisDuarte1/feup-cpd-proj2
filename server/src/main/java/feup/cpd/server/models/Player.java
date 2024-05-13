package feup.cpd.server.models;

import java.io.Serial;
import java.io.Serializable;

public class Player implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    public final String name;
    public final byte[] passwordHash;

    private int elo;



    public Player(String name, byte[] passwordHash) {
        this.name = name;
        this.passwordHash = passwordHash;
        this.elo = 500;
    }

    public Player(String name, byte[] passwordHash, int elo){
        this.name = name;
        this.passwordHash = passwordHash;
        this.elo = elo;
    }


    public int getElo() {
        return elo;
    }

    public void setElo(int elo) {
        this.elo = elo;
    }

    public Player clonePlayer(){
        return new Player(name, passwordHash, elo);
    }
}
