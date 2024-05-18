package feup.cpd.game;


public class Card {

    public static final boolean linux = true;
    private Color color;
    private Value value;
    private Color newColor;

    public Card(Color color, Value value){
        this.color = color;
        this.value = value;
    }

    public void setNewColor(Color newColor) { this.newColor=newColor; }

    public Color getColor() { return this.color; }
    public Value getValue() { return this.value; }
    public Color getNewColor() { return this.newColor; }


    public String toString(){
        String result = "";
        if (linux){
            if(newColor!=null){
                switch (newColor){
                    case BLUE -> result += "\033[94m Blue ";
                    case RED -> result += "\033[91m Red ";
                    case GREEN -> result += "\033[32m Green ";
                    case YELLOW -> result += "\033[93m Yellow ";
                }
            }
            else switch (color){
                case BLUE -> result += "\033[94m Blue ";
                case RED -> result += "\033[91m Red ";
                case GREEN -> result += "\033[32m Green ";
                case YELLOW -> result += "\033[93m Yellow ";
                case BLACK -> result += "\033[1m ";
            }
        }
        else {
            if(newColor!=null){
                switch (newColor){
                    case BLUE -> result += "Blue ";
                    case RED -> result += "Red ";
                    case GREEN -> result += "Green ";
                    case YELLOW -> result += "Yellow ";
                }
            }
            else switch (color){
                case BLUE -> result += "Blue ";
                case RED -> result += "Red ";
                case GREEN -> result += "Green ";
                case YELLOW -> result += "Yellow ";
                case BLACK -> result += "";
            }
        }
        switch (value){
            case ONE -> result += "1";
            case TWO -> result += "2";
            case THREE -> result += "3";
            case FOUR -> result += "4";
            case FIVE -> result += "5";
            case SIX -> result += "6";
            case SEVEN -> result += "7";
            case EIGHT -> result += "8";
            case NINE -> result += "9";
            case REVERSE -> result += "Reverse";
            case SKIP -> result += "Skip";
            case DRAW2 -> result += "Draw 2";
            case WILD -> result += "Wild Card";
            case WILD4 -> result += "Wild 4";
        }

        if (linux) result += "\033[37m\033[0m";

        return result;
    }
    public boolean canPlayOn(Card card, Color newColor){
        return color==Color.BLACK || value == Value.WILD || value == Value.WILD4 ||
                color == card.getColor() || color == card.getNewColor() ||
                (color != card.getColor() && value == card.getValue());
    }

   /* public boolean applyEffect(){

    }*/
}
