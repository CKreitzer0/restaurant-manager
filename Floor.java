package Restaurant_Manager;

public class Floor {
    private Table[] tables;

    //capacities array
    public Floor(int[] capacities){
        tables = new Table[capacities.length];
        for (int i = 0; i < capacities.length; i++){
            tables[i] = new Table(i + 1, capacities[i]); //table #'s start at 1
        }
    }

    //return table number and returns null if number called doesn't exist
    public Table getTable(int num){
        if (num < 1 || num > tables.length){
            return null;
        }
        return tables[num - 1];
    }

    // find the smallest empty table that accomodates the party
    public Table findSmallest(int groupSize){
        Table best = null;
        for (int i = 0; i < tables.length; i++){
            Table t = tables[i];
            if(t.getStatus().equals(Status.EMPTY) && t.getCapacity() >= groupSize){
                if (best == null || t.getCapacity() < best.getCapacity()){
                    best = t;
                }
            }
        }
        return best;
    }

    //count number of tables by status
    public int countStatus(String status){
        int count = 0;
        for (int i = 0; i < tables.length; i++){
            if (tables[i].getStatus().equals(status)){
                count++;
            }
        }
        return count;
    }

    //returns tables
    public Table[] getTables(){
        return tables;
    }

    public int size(){
        return tables.length;
    }
}
