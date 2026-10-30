package Restaurant_Manager;

public class Order {

    private OrderItem[] items;
    private int size;

    public Order() {
        items = new OrderItem[5];
        size = 0;
    }

    private int findItem(String name) {
        for (int i = 0; i < size; i++) {
            if (items[i].getItem().getName().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    private void grow() {
        OrderItem[] bigger = new OrderItem[items.length * 2];

        for (int i = 0; i < size; i++) {
            bigger[i] = items[i];
        }

        items = bigger;
    }

    public void addItem(MenuItems item, int quantity) {
        if (item == null || quantity <= 0) {
            System.out.println("Invalid item or quantity.");
            return;
        }

        int index = findItem(item.getName());

        if (index != -1) {
            items[index].addQuantity(quantity);
            return;
        }

        if (size == items.length) {
            grow();
        }

        items[size] = new OrderItem(item, quantity);
        size++;
    }

    public void removeItem(String name) {
        int index = findItem(name);

        if (index == -1) {
            System.out.println("Item not found in the order.");
            return;
        }

        for (int i = index; i < size - 1; i++) {
            items[i] = items[i + 1];
        }

        size--;
        items[size] = null;
    }

    public void changeQuantity(String name, int newQuantity) {
        if (newQuantity <= 0) {
            System.out.println("Quantity must be greater than 0.");
            return;
        }

        int index = findItem(name);

        if (index == -1) {
            System.out.println("Item not found in the order.");
            return;
        }

        items[index].setQuantity(newQuantity);
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public OrderItem getOrderItem(int index) {
        if (index < 0 || index >= size) {
            return null;
        }

        return items[index];
    }


    public double calculateTotal() {
        double total = 0;

        for (int i = 0; i < size; i++) {
            total = total + items[i].getSubtotal();
        }

        return total;
    }

    public void printReceipt(int tableNumber) {
        System.out.println("----- RECEIPT: TABLE " + tableNumber + " -----");

        if (isEmpty()) {
            System.out.println("The order is empty.");
            return;
        }

        for (int i = 0; i < size; i++) {
            OrderItem current = items[i];

            System.out.print(current.getQuantity() + " x "
                    + current.getItem().getName() + " = $");

            System.out.printf("%.2f%n", current.getSubtotal());
        }

        System.out.print("TOTAL: $");
        System.out.printf("%.2f%n", calculateTotal());
        System.out.println("------------------------------");
    }

    public Ticket createTicket(int ticketNumber, int tableNumber,
                               int estimatedWaitMinutes) {

        if (ticketNumber <= 0 || tableNumber <= 0 ||
                estimatedWaitMinutes < 0 || isEmpty()) {

            System.out.println("Cannot create ticket: invalid information or empty order.");
            return null;
        }

        return new Ticket(ticketNumber, tableNumber,
                this, estimatedWaitMinutes);
    }
}
