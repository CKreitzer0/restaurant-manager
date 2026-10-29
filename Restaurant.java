package Restaurant_Manager;

public class Restaurant {
    private MenuItems[] menu;
    private Floor floor;
    private int totalEarningsInCents;

    //constructor
    public Restaurant(){
        //set menu
        menu = new MenuItems[]{
                new MenuItems("Item 1", 1099),
                new MenuItems("Item 1", 1099),
                new MenuItems("Item 1", 1099)
        };
        //set floor plan with tables
        floor = new Floor(new int[] {2,2,2,6,6,10});
        totalEarningsInCents = 0;
    }

    //SEATING SECTION----
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

    //MENU SECTION----
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

    //Getters for UI
    public Floor getFloor(){
        return floor;
    }

    public int getTotalEarningsInCents(){
        return totalEarningsInCents;
    }
}
