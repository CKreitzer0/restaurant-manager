package Restaurant_Manager;

public class Table {
    //table number
    private int num;
    //capacity of table
    private int capacity;
    //status (empty, busy,dirty)
    private String status;

    //constructor
    public Table(int num, int capacity){
        this.num = num;
        this.capacity = capacity;
        this.status = Status.EMPTY; //start as empty
    }

    //seat a group. true = table was empty. false = busy
    public boolean seat(){
        if(status.equals(Status.EMPTY)){
            status = Status.BUSY;
            return true;
        }
        return false;
    }

    //If a group leaves, this frees the table
    public boolean free(){
        if (status.equals(Status.BUSY)){
            status = Status.DIRTY;
            return true;
        }
        return false;
    }

    //For the busser to clean the table
    public boolean clean(){
        if(status.equals(Status.DIRTY)){
            status = Status.EMPTY;
            return true;
        }
        return false;
    }

    //return the values
    public int getNum(){
        return num;
    }
    public int getCapacity(){
        return capacity;
    }
    public String getStatus(){
        return status;
    }
}
