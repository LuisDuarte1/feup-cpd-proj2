package feup.cpd.game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Player implements Serializable {
    private static final long serialVersionUID = 1L;
    public final String name;
    private List<Card> hand;
    public Player(String name) {
        this.name=name;
        this.hand=new ArrayList<Card>();
    }


    public void drawCards(Card card) {
        hand.add(card);
    }

    public void playCard(Card card) {
        hand.remove(card);
    }

    public List<Card> getHand() {
        return hand;
    }
}