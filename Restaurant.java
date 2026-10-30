package Restaurant_Manager;

public class Restaurant {
    private MenuItems[] menu;
    private Floor floor;
    private double totalEarnings;
    private Kitchen kitchen;
    private int nextTicketNumber;

    //constructor
    public Restaurant(){
        //set menu
        menu = new MenuItems[]{
                new MenuItems("Pizza", 15.99),
                new MenuItems("Cheese Burger", 10.99),
                new MenuItems("Fries", 5.99),
                new MenuItems("Pasta", 12.99),
                new MenuItems("Salad", 7.99),
                new MenuItems("Soda", 3.99)

        };
        //set floor plan with tables
        floor = new Floor(new int[] {2,2,2,6,6,10});
        kitchen = new Kitchen();
        totalEarnings = 0;
        nextTicketNumber = 1;
    }

    //SEATING SECTION
    //find smallest empty table that fits group
    public Table seatGroup(int groupSize){
        if (groupSize < 1){
            return null;
        }
        Table t = floor.findSmallest(groupSize);
        if(t == null){
            return null;
        }
        t.seat();
        return t;
    }

    //if a group leaves
    public boolean freeTable(int tableNum){
        Table t = floor.getTable(tableNum);
        if (t == null){
            return false;
        }
        return t.free();
    }

    //busser cleans it
    public boolean cleanTable(int tableNum){
        Table t = floor.getTable(tableNum);
        if (t == null){
            return false;
        }
        return t.clean();
    }

    //MENU SECTION
    // menu numbers start at 1
    public MenuItems getMenuItems(int num){
        if (num < 1 || num > menu.length){
            return null;
        }
        return menu[num - 1];
    }

    public MenuItems[] getMenu(){
        return menu;
    }

    //ORDERS SECTION
    private Order getOrder(int tableNum){
        Table t = floor.getTable(tableNum);
        if (t == null){
            return null;
        }
        return t.getOrder();
    }

    //Returns true if the order contains the item
    private boolean containsItem(Order order, String name){
        for (int i = 0; i < order.size(); i++){
            if(order.getOrderItem(i).getItem().equals(name)){
                return true;
            }
        }
        return false;
    }

    //Add item
    public boolean addItem(int tableNum, int menuNum, int quantity){
        Order order = getOrder(tableNum);
        MenuItems item = getMenuItems(menuNum);

        if (order == null || item == null || quantity < 1){
            return false;
        }
        if (!item.getAvailable()){
            return false;
        }
        order.addItem(item, quantity);
        return true;
    }

    //Remove item completely
    public boolean removeItem(int tableNum, String itemName){
        Order order = getOrder(tableNum);

        if(order == null || !containsItem(order, itemName)){
            return false;
        }
        order.removeItem(itemName);
        return true;
    }

    //Change Quantity of something in the order
    public boolean changeQuantity(int tableNum, String itemName, int newQuantity){
        Order order = getOrder(tableNum);

        if (order == null || newQuantity < 1 || !containsItem(order, itemName)){
            return false;
        }
        order.changeQuantity(itemName, newQuantity);
        return true;
    }

    //KITCHEN SECTION

    //wait time is 5 minutes per item type and 2 per ticket waiting
    private Ticket makeTicket(int tableNum){
        Order order = getOrder(tableNum);
        if (order == null || order.isEmpty()){
            return null;
        }
        int wait = order.size() * 5 + kitchen.size() * 2; //wait and size
        Ticket ticket = order.createTicket(nextTicketNumber, tableNum, wait);
        if (ticket!= null){
            nextTicketNumber++;
        }
        return ticket;
    }

    //Normal order
    public Ticket sendOrder(int tableNum){
        Ticket ticket = makeTicket(tableNum);
        if(ticket == null){
            return null;
        }
        kitchen.sendTicketToBack(ticket);
        return ticket;
    }

    //rush order
    public Ticket rushOrder(int tableNum) {
        Ticket ticket = makeTicket(tableNum);
        if (ticket == null) {
            return null;
        }
        kitchen.sendTicketToFront(ticket);
        return ticket;
    }

    //remove newest ticket
    public Ticket undoLastTicket(){
        return kitchen.undoNewestTicket();
    }

    // cook takes ticket
    public Ticket getNextTicket(){
        return kitchen.getNextTicket();
    }

    // CHECKOUT SECTION
    //adds order to earnings, frees the table, returns finished order
    public Order checkout(int tableNum){
        Table t = floor.getTable(tableNum);
        if (t == null){
            return null;
        }
        Order order = t.getOrder();
        if(order == null || order.isEmpty()){
            return null;
        }
        totalEarnings = totalEarnings + order.calculateTotal();
        t.free();
        return order;
    }

    //Getters for UI
    public Floor getFloor(){
        return floor;
    }

    public Kitchen getKitchen(){
        return kitchen;
    }

    public double getTotalEarnings(){
        return totalEarnings;
    }
}
