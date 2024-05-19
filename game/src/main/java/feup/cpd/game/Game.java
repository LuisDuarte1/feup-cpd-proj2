package feup.cpd.game;

import feup.cpd.protocol.models.enums.QueueType;

import java.io.Serializable;
import java.util.*;

public class Game {
    private final List<Card> deck;
    private final List<Player> players;


    private int currentPlayer;

    private final List<Card> discardPile;
    private boolean order;
    private QueueType queueType;
    private int drawNumber;
    public Game() {
        deck = createDeck();
        players = new ArrayList<>();
        currentPlayer = 0;
        discardPile = new ArrayList<>();
        order= true;
        drawNumber=0;
    }

    public QueueType getQueueType() {
        return queueType;
    }

    public void setQueueType(QueueType queueType) {
        this.queueType = queueType;
    }

    public Card getTopCard() { return discardPile.getLast(); }
    public String getCurrentPlayerName() {
        return players.get(currentPlayer).name;
    }

    public List<Card> getCurrentPlayerHand() {
        return players.get(currentPlayer).getHand();
    }

    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public boolean checkIfPlayerBelongs(String playerName){
        return players.stream().filter(player -> Objects.equals(player.name, playerName)).count() == 1;
    }

    private List<Card> createDeck() {
        Color[] colors = {Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW};
        Value[] values = {Value.REVERSE, Value.SKIP, Value.DRAW2, Value.ONE, Value.TWO, Value.THREE, Value.FOUR, Value.FIVE, Value.SIX, Value.SEVEN, Value.EIGHT, Value.NINE};
        List<Card> deck = new ArrayList<>();

        for (Color color : colors) {
            for (Value value : values) {
                //duplicate cards in order to be played better
                deck.add(new Card(color, value));
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

    public void placeCard(Card card){
        discardPile.add(card);
        players.get(currentPlayer).getHand().remove(card);
    }

    public Optional<Card> drawCard(){
        if (deck.isEmpty()) return Optional.empty();
        final Card card = deck.getLast();
        players.get(currentPlayer).getHand().add(card);
        deck.removeLast();
        if (deck.isEmpty()) reshuffleDeck();
        return Optional.of(card);
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
        while (discardPile.isEmpty() || discardPile.getLast().getColor() == Color.BLACK
                || discardPile.getLast().getValue() == Value.DRAW2){
            discardPile.add(deck.getLast());
            deck.removeLast();
        }

        playGame();
    }

    public Optional<Card> checkAndDrawHeadless(){
        if(drawNumber != 0){
            drawNumber -= 1;
            return drawCard();
        }

        for (Card card : players.get(currentPlayer).getHand()) {
            if (card.canPlayOn(discardPile.getLast()) || card.getColor() == Color.BLACK) {
                return Optional.empty();
            }
        }

        return drawCard();
    }

    public boolean checkIfCanPlay(){
        for (Card card : players.get(currentPlayer).getHand()) {
            if (card.canPlayOn(discardPile.getLast()) || card.getColor() == Color.BLACK) {
                return true;
            }
        }
        return false;
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
                && hand.stream().filter(card::equals)
                        .findFirst().get().canPlayOn(discardPile.getLast())))
            return false;
        if(card.getValue()==Value.REVERSE){
            order= !order;
        }

        else if(card.getValue()==Value.SKIP){
            placeCard(card);
            nextTurn();
            nextTurn();
            return true;
        }

        else if(card.getValue()==Value.DRAW2){
            drawNumber = 2;
        }
        else if(card.getColor()==Color.BLACK){
            if(card.getNewColor() == null) return false;

            if(card.getValue()==Value.WILD4){
                drawNumber = 4;
            }
            placeCard(card);
            nextTurn();
            return true;
        }


        if(card.getNewColor() != null) return false;
        placeCard(card);
        nextTurn();
        return true;
    }

    public void printLastCardPlayed(){
        System.out.println("Last Card:");
        System.out.println(discardPile.getLast());
    }

    public void reshuffleDeck(){

        deck.addAll(discardPile);
        discardPile.clear();
        discardPile.add(deck.getLast());
        deck.removeLast();
        Collections.shuffle(deck);
    }

    public void selectNewColor(Card card){
        Scanner scanner = new Scanner(System.in);

        while(true){
            System.out.println("\nSelect a new color for the game:");
            System.out.println("\n\033[94m [1] Blue\n\033[91m [2] Red\n\033[32m [3] Green\n\033[93m [4] Yellow\033[0m");

            String selectedNumber = scanner.nextLine();

            switch(selectedNumber){
                case "1":
                    card.setNewColor(Color.BLUE);
                    return;
                case "2":
                    card.setNewColor(Color.RED);
                    return;
                case "3":
                    card.setNewColor(Color.GREEN);
                    return;
                case "4":
                    card.setNewColor(Color.YELLOW);
                    return;
                default:
                    System.out.println("Invalid input. Please enter a number from 1 to 4.");
            }
        }
    }


    public Card selectCard(Scanner scanner){
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

        Card card = players.get(currentPlayer).getHand().get(number);
        if(card.getColor()==Color.BLACK){
            System.out.println("\nSelect a new color for the game:");
            System.out.println("\n\033[94m [1] Blue\n\033[91m [2] Red\n\033[32m [3] Green\n\033[93m [4] Yellow\033[0m");

            String selectedNumber = scanner.nextLine();

            switch(selectedNumber){
                case "1":
                    card.setNewColor(Color.BLUE);
                    break;
                case "2":
                    card.setNewColor(Color.RED);
                    break;
                case "3":
                    card.setNewColor(Color.GREEN);
                    break;
                case "4":
                    card.setNewColor(Color.YELLOW);
                    break;
            }
        }
        return card;
    }

    public void playGame() {
        Scanner scanner = new Scanner(System.in);

        while(!isGameOver()){
            printLastCardPlayed();

            System.out.println("Player "+(currentPlayer+1)+"'s hand:");
            showHand();

            checkAndDraw();

            int number;

            while (true) {
                System.out.println("\nSelect a card to play:");
                String selectedNumber = scanner.nextLine();
                try {
                    number = Integer.parseInt(selectedNumber) - 1;
                    if (number >= 0 && number < players.get(currentPlayer).getHand().size()
                            && players.get(currentPlayer).getHand().get(number).canPlayOn(discardPile.get(discardPile.size() - 1))) {
                        break;
                    } else {
                        System.out.println("Can't play that card");
                        printLastCardPlayed();
                        System.out.println("Player "+(currentPlayer+1)+"'s hand:");
                        showHand();
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a number.");
                    printLastCardPlayed();
                }
            }

            Card card = players.get(currentPlayer).getHand().get(number);
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
                selectNewColor(card);

                if(card.getValue()==Value.WILD4){
                    drawNumber+=4;
                }
            }

            placeCard(card);

            System.out.println();

            nextTurn();
        }
    }

}