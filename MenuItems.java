package Restaurant_Manager;

public class MenuItems {
    private String name;
    private double price;
    private boolean available;

    //constructor
    public MenuItems(String name, double price){
        this.name = name;
        this.price = price;
        this.available = true;
    }

    //returns values
    public String getName(){
        return name;
    }

    public double getPrice(){
        return price;
    }

    public boolean getAvailable(){
        return available;
    }

    //set availability
    public void setAvailable(boolean available){
        this.available = available;
    }

    //converts money from cent amount to dollar amount
    public static String money(double amount){
        return String.format("$%.2f", amount);
    }
}
