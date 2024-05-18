package feup.cpd.game;

import java.io.Serializable;
import java.util.*;

public class Game implements Serializable {
    private final List<Card> deck;
    private final List<Player> players;

    private int currentPlayer;

    private final List<Card> discardPile;
    private boolean order;
    private int drawNumber;
    public Game() {
        deck = createDeck();
        players = new ArrayList<>();
        currentPlayer = 0;
        discardPile = new ArrayList<>();
        order= true;
        drawNumber=0;
    }

    public String getCurrentPlayerName() {
        return players.get(currentPlayer).name;
    }

    private List<Card> createDeck() {
        Color[] colors = {Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW};
        Value[] values = {Value.REVERSE, Value.SKIP, Value.DRAW2, Value.ONE, Value.TWO, Value.THREE, Value.FOUR, Value.FIVE, Value.SIX, Value.SEVEN, Value.EIGHT, Value.NINE};
        List<Card> deck = new ArrayList<>();

        for (Color color : colors) {
            for (Value value : values) {
                deck.add(new Card(color, value));
            }
        }

        for (int i = 0; i < 4; i++) {
            deck.add(new Card(Color.BLACK, Value.WILD));
            deck.add(new Card(Color.BLACK,Value.WILD4));
        }

        Collections.shuffle(deck);

        return deck;
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public void nextTurn() {

        if(order) currentPlayer = (currentPlayer + 1) % players.size();
        else currentPlayer = (currentPlayer - 1 + players.size()) % players.size();
    }

    public boolean isGameOver() {
        for (Player player : players) {
            if (player.getHand().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public void checkAndDraw(){
        if(drawNumber!=0){
            for(int i=0;i<drawNumber;i++){
                drawCard();

                showHand();
            }

            drawNumber=0;
        }
        while(true) {
            for (Card card : players.get(currentPlayer).getHand()) {
                if (card.canPlayOn(discardPile.getLast()) || card.getColor() == Color.BLACK) {
                    return;
                }
            }

            drawCard();

            showHand();
        }
    }

    public void placeCard(int number){
        discardPile.add(players.get(currentPlayer).getHand().get(number));
        players.get(currentPlayer).getHand().remove(number);
    }

    public Card drawCard(){
        System.out.println("Drawing card...");
        final Card card = deck.getLast();
        players.get(currentPlayer).getHand().add(card);
        deck.removeLast();
        return card;
    }
    public void showHand(){
        for (Card card : players.get(currentPlayer).getHand()) {
            System.out.print(card.toString() + " ");
        }

        System.out.println();
    }

    public void startGame() {
        for(int i=0;i<5;i++){
            Player p= new Player("Jorge");
            addPlayer(p);
            for(int j=0; j<7;j++){
                p.getHand().add(deck.getFirst());
                deck.removeFirst();
            }
        }

        discardPile.add(deck.getLast());
        deck.removeFirst();

        playGame();
    }

    public Optional<Card> checkAndDrawHeadless(){
        if(drawNumber != 0){
            return Optional.of(drawCard());
        }

        for (Card card : players.get(currentPlayer).getHand()) {
            if (card.canPlayOn(discardPile.getLast()) || card.getColor() == Color.BLACK) {
                return Optional.empty();
            }
        }

        return Optional.of(drawCard());
    }

    public void startGameHeadless(List<String> playerNames){
        for(final String playerName : playerNames){
            Player p= new Player(playerName);
            addPlayer(p);
            for(int j=0; j<7;j++){
                p.getHand().add(deck.getFirst());
                deck.removeFirst();
            }
        }

        discardPile.add(deck.getLast());
        deck.removeFirst();
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public boolean playCard(Card card){
        if (drawNumber != 0) return false;
        var hand = players.get(currentPlayer).getHand();
        if (!(hand.contains(card)
                && hand.stream().filter((card1) -> card == card1)
                        .findFirst().get().canPlayOn(discardPile.getFirst())))
            return false;
        //TODO(luisd): complete this
        if(card.getValue()==Value.REVERSE){
            order= !order;
        }

        else if(card.getValue()==Value.SKIP){
            nextTurn();
        }

        else if(card.getValue()==Value.DRAW2){
            drawNumber+=2;
        }
        else if(card.getColor()==Color.BLACK){
            if(card.getNewColor() == null) return false;

            if(card.getValue()==Value.WILD4){
                drawNumber+=4;
            }
            placeCard(hand.indexOf(card));
            nextTurn();
            return true;
        }


        if(card.getNewColor() != null) return false;
        placeCard(hand.indexOf(card));
        return true;
    }

    public void printLastCardPlayed(){
        System.out.println("Last Card:");
        System.out.println(discardPile.getLast());
    }


    public void playGame() {
        Scanner scanner = new Scanner(System.in);

        while(!isGameOver()){
            printLastCardPlayed();

            System.out.println("Player "+(currentPlayer+1)+"'s hand:");
            showHand();

            checkAndDraw();

            int number;

            while(true){
                //VER CASO EM QUE DAO INPUTS ERRADOS
                System.out.println("\nSelect a card to play:");
                String selectedNumber = scanner.nextLine();
                number = Integer.parseInt(selectedNumber)-1;
                if (number<players.get(currentPlayer).getHand().size()
                                && number>0
                                && players.get(currentPlayer).getHand().get(number).canPlayOn(discardPile.getLast())) break;
                System.out.println("Can't play that card");
            }

            if(players.get(currentPlayer).getHand().get(number).getValue()==Value.REVERSE){
                order= !order;
            }

            else if(players.get(currentPlayer).getHand().get(number).getValue()==Value.SKIP){
                nextTurn();
            }

            else if(players.get(currentPlayer).getHand().get(number).getValue()==Value.DRAW2){
                drawNumber+=2;
            }

            else if(players.get(currentPlayer).getHand().get(number).getColor()==Color.BLACK){
                System.out.println("\nSelect a new color for the game:");
                System.out.println("\n\033[94m [1] Blue\n\033[91m [2] Red\n\033[32m [3] Green\n\033[93m [4] Yellow\033[0m");

                String selectedNumber = scanner.nextLine();

                switch(selectedNumber){
                    case "1":
                        players.get(currentPlayer).getHand().get(number).setNewColor(Color.BLUE);
                        break;
                    case "2":
                        players.get(currentPlayer).getHand().get(number).setNewColor(Color.RED);
                        break;
                    case "3":
                        players.get(currentPlayer).getHand().get(number).setNewColor(Color.GREEN);
                        break;
                    case "4":
                        players.get(currentPlayer).getHand().get(number).setNewColor(Color.YELLOW);
                        break;
                }

                if(players.get(currentPlayer).getHand().get(number).getValue()==Value.WILD4){
                    drawNumber+=4;
                }
            }


            placeCard(number);

            System.out.println();

            nextTurn();
        }
    }

}