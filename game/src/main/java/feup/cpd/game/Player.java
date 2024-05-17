package feup.cpd.game;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private String name;
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