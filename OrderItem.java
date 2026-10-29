package Restaurant;

public class OrderItem {

    private MenuItem item;
    private int quantity;

    public OrderItem(MenuItem item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }

    public MenuItem getItem() {
        return item;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int newQuantity) {
        if (newQuantity > 0) {
            quantity = newQuantity;
        } else {
            System.out.println("Quantity must be greater than 0.");
        }
    }

    public void addQuantity(int amount) {
        if (amount > 0) {
            quantity = quantity + amount;
        } else {
            System.out.println("Amount must be greater than 0.");
        }
    }

    public double getSubtotal() {
        return item.getPrice() * quantity;
    }

    public void display() {
        System.out.println(quantity + " x " + item.getName()
                + " = $" + getSubtotal());
    }
}

