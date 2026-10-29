package Restaurant_Manager;

public class MenuItems {
    private String name;
    private int priceInCents;
    private boolean available;

    //constructor
    public MenuItems(String name, int priceInCents){
        this.name = name;
        this.priceInCents = priceInCents;
        this.available = true;
    }

    //returns values
    public String getName(){
        return name;
    }

    public int getPriceInCents(){
        return priceInCents;
    }

    public boolean getAvailable(){
        return available;
    }

    //set availability
    public void setAvailable(boolean available){
        this.available = available;
    }

    //converts money from cent amount to dollar amount
    public static String dollarConverter(int cents){
        int dollars = cents / 100;
        int extra = cents % 100;
        if (extra < 10){
            return "$" + dollars + ".0" + extra;
        }
        return "$" + dollars + "." + extra;
    }
}
