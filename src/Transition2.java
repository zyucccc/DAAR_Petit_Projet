public class Transition2 {

    private char symbol;
    private int tostate;

    public Transition2( char symbol , int tostate){

        this.symbol= symbol;
        this.tostate= tostate;
    }

    public char getSymbol() { return symbol; }

    public void setSymbol(char symbol) { this.symbol = symbol; }

    public int getTostate() { return tostate; }

    public void setTostate(int tostate) { this.tostate = tostate; }

}
