package Restaurant;

public class Ticket {

    private int ticketNumber;
    private int tableNumber;
    private String[] itemNames;
    private int[] quantities;
    private int estimatedWaitMinutes;

    public Ticket(int ticketNumber, int tableNumber,
                  Order order, int estimatedWaitMinutes) {

        this.ticketNumber = ticketNumber;
        this.tableNumber = tableNumber;
        this.estimatedWaitMinutes = estimatedWaitMinutes;

        itemNames = new String[order.size()];
        quantities = new int[order.size()];

        for (int i = 0; i < order.size(); i++) {
            OrderItem current = order.getOrderItem(i);
            itemNames[i] = current.getItem().getName();
            quantities[i] = current.getQuantity();
        }
    }

    public int getTicketNumber() {
        return ticketNumber;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public int getEstimatedWaitMinutes() {
        return estimatedWaitMinutes;
    }
    public int getItemCount() {
        return itemNames.length;
    }

    public String getItemName(int index) {
        if (index < 0 || index >= itemNames.length) {
            return null;
        }

        return itemNames[index];
    }

    public int getItemQuantity(int index) {
        if (index < 0 || index >= quantities.length) {
            return 0;
        }

        return quantities[index];
    }

    public void printTicket() {
        System.out.println("----- KITCHEN TICKET #" + ticketNumber + " -----");
        System.out.println("Table: " + tableNumber);

        for (int i = 0; i < itemNames.length; i++) {
            System.out.println(quantities[i] + " x " + itemNames[i]);
        }

        System.out.println("Estimated wait: " +
                estimatedWaitMinutes + " minutes");
        System.out.println("------------------------------");
    }
}

