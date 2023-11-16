package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import data_structures.HashTableSC;
import data_structures.LinkedStack;
import data_structures.SinglyLinkedList;
import data_structures.BasicHashFunction;
import interfaces.List;
import interfaces.Map;
import interfaces.Stack;

public class CarPartFactory {
	
	/**
	 * created all the fields in the class
	 */
	private List<PartMachine> machines;
	private List<Order> orders;
	private Map<Integer, CarPart> partCatalog;
	private Map<Integer, List<CarPart>> inventory;
	private Map<Integer, Integer> defective;
	private Stack<CarPart> productionBin;

    /**
     *     
     * @param orderPath
     * @param partsPath
     * @throws IOException
     * -initialized machines, orders, partCatalog, and inventory using other methods
     * -initialized productionBin as a LinkedStack because I wanted it to work as if I'm putting the parts in the 
     * bin physically and the first one you grab is the last one you put.
     * -initialized the defective map with all the keys and with the value 0 so that I can later sum the defective parts
     */
    public CarPartFactory(String orderPath, String partsPath) throws IOException {
    	setupOrders(orderPath);
    	setupMachines(partsPath);
    	setupInventory();
    	productionBin = new LinkedStack<CarPart>();
    	defective = new HashTableSC<Integer, Integer>(partCatalog.size(), new BasicHashFunction());
    	for (Integer keys : partCatalog.getKeys()) {
    		defective.put(keys, 0);
    	}
    }
    
    public List<PartMachine> getMachines() {
       return  this.machines;
    }
    
    public void setMachines(List<PartMachine> machines) {
        this.machines = machines;
    }
    
    public Stack<CarPart> getProductionBin() {
    	return this.productionBin;
    }
    
    public void setProductionBin(Stack<CarPart> production) {
        this.productionBin = production;
    }
    
    public Map<Integer, CarPart> getPartCatalog() {
        return this.partCatalog;
    }
    
    public void setPartCatalog(Map<Integer, CarPart> partCatalog) {
        this.partCatalog = partCatalog;
    }
    
    public Map<Integer, List<CarPart>> getInventory() {
    	return this.inventory;
    }
    
    public void setInventory(Map<Integer, List<CarPart>> inventory) {
        this.inventory = inventory;
    }
    
    public List<Order> getOrders() {
    	return this.orders;
    }
    
    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }
    
    public Map<Integer, Integer> getDefectives() {
        return this.defective;
    }
    
    public void setDefectives(Map<Integer, Integer> defectives) {
        this.defective = defectives;
    }

    /**
     * 
     * @param path
     * @throws IOException
     * -initialized orders as a singly linked list because i wanted it to simulate a queue so the most efficient is the 
     * singly linked list because i just need to iterate in order and it does not have to reallocate either.
     * -used buffered reader to read the path file. divided the lines into each element and then created the orders individually 
     */
    public void setupOrders(String path) throws IOException {
        orders = new SinglyLinkedList<Order>();
        BufferedReader orderReader = new BufferedReader(new FileReader(path));
        String line = orderReader.readLine();
        line = orderReader.readLine();
        while (line!=null) {
        	String[] divided = line.split(",");
        	Integer id = Integer.parseInt(divided[0]);
        	String name = divided[1];
        	String[] tuples = divided[2].split("-");
        	Map<Integer, Integer> requested = new HashTableSC<Integer, Integer>(tuples.length, new BasicHashFunction());
        	for (int i = 0; i< tuples.length;i++) {
        		 String stripped = tuples[i].substring(1, tuples[i].length()-1);
        		 String[] div = stripped.split(" ");
        		 Integer partID = Integer.parseInt(div[0]);
        		 Integer quantity = Integer.parseInt(div[1]);
        		 requested.put(partID, quantity);
        	}
        	orders.add(new Order(id, name, requested, true));
        	line = orderReader.readLine();
        }
        orderReader.close();
        
    }
    
    /**
     * 
     * @param path
     * @throws IOException
     * -initialize machine as a singly linked list because of low memory usage and easy iteration. 
     * -used the same logic as setupOrders but in this case first I created the machines list to then create the
     * map of the partCatalog with the machines size as initial size and the carParts inside it. 
     */
    public void setupMachines(String path) throws IOException {
    	machines = new SinglyLinkedList<PartMachine>();
    	BufferedReader machineReader = new BufferedReader(new FileReader(path));
    	String line = machineReader.readLine();
    	line = machineReader.readLine();
    	while (line != null) {
    		String[] divided = line.split(",");
    		Integer ID = Integer.parseInt(divided[0]);
    		String partName = divided[1];
    		Double weight = Double.parseDouble(divided[2]);
    		Double weightError = Double.parseDouble(divided[3]);
    		Integer period = Integer.parseInt(divided[4]);
    		Integer chanceOfDefective = Integer.parseInt(divided[5]);
    		CarPart carPart = new CarPart(ID, partName, weight,false);
    		machines.add(new PartMachine(ID, carPart, period, weightError, chanceOfDefective));
    		line = machineReader.readLine();
    	}
    	machineReader.close();
    	partCatalog = new HashTableSC<Integer, CarPart>(machines.size(), new BasicHashFunction());
    	for(PartMachine partMachine : machines) {
    		partCatalog.put(partMachine.getId(), partMachine.getPart());
    	}
    	
    }
    
    /**
     * -initialized inventory as a map. 
     * -ran through the machines and added an emtpy list to the value with the part id as the key. 
     */
    public void setupInventory() {
        inventory = new HashTableSC<Integer, List<CarPart>>(partCatalog.size(), new BasicHashFunction());
        for (PartMachine partMachine : machines) {
        	List<CarPart> tempList = new SinglyLinkedList<CarPart>();
        	inventory.put(partMachine.getId(), tempList);
        }
        
    }
    
    /**
     * ran a while until the production bin was empty, if the part was not detective it added it to the inventory
     * if it was the defective count increased by one.
     * at the end the production bin removed the top. 
     */
    public void storeInInventory() {
    	while (!productionBin.isEmpty()) {
    		CarPart carPart = productionBin.top();
    		Integer id = carPart.getId();
    		if (!carPart.isDefective()) {
    			inventory.get(id).add(carPart);
    		}
    		else {
    			defective.put(id, defective.get(id)+1);
    		} 
    		productionBin.pop();
    	}
    	
    }
    
    /**
     * 
     * @param days
     * @param minutes
     * -ran 3 nested for loops where days is the outer one, the next one is machines and the inner one is minutes
     * -i did this because i wanted to produce all the parts of one machine int the minutes provided before changing machines
     * -since i did this when the minutes loop endedn i could assume that the day was over for that machine so i could emtpy the conveyor
     * belt in that same loop. 
     */
    public void runFactory(int days, int minutes) {
        for (int i = 0; i<days; i++) {
        	for (PartMachine machine : machines) {
        		for (int j = 0; j < minutes; j++) {
        			CarPart machinePart = machine.produceCarPart(); 
        			if (machinePart!=null) {
        				productionBin.push(machinePart);
        			}
        		}
        		while (!machine.getConveyorBelt().isEmpty()) {
        			if (machine.getConveyorBelt().front() != null) {
        				productionBin.push(machine.getConveyorBelt().dequeue());
        			}
        			else {
        			machine.getConveyorBelt().dequeue();
        			}	
        		}
        		machine.resetConveyorBelt();
        	}
        	storeInInventory();
        }
        processOrders();
    }
    
    /**
     * 
     * @param order
     * @return
     * -helper method of processOrders that checks if a order was fulfilled
     * -created a map of the requested parts of the order parameter
     * -ran a loop thought the key and inside if the inventory had the key and the requested parts was greater than
     * the inventory's part list size then it change the fulfilled boolean to false and break.
     * -else just fulfilled false and break
     * -return fulfilled
     */
    public boolean checkFulfilled(Order order) {
    	boolean fulfilled = true;
    	Map<Integer, Integer> requested = order.getRequestedParts();
    	for (Integer key : requested.getKeys()) {
    		if (inventory.containsKey(key)) {
    			if (requested.get(key) 	>  inventory.get(key).size()) {
    				fulfilled = false;
    				break;
    			}
    		}
    		else {
    			fulfilled = false;
    			break;
    		}
    	}
    	return fulfilled;
    }
    
   /**
    * -ran a for each loop through the orders list and called the checkFulfilled function with the order as the parameter
    * -if the checkFulfilled returned true it sets the order setFulfilled to true. 
    * -then ran throught the keys of the order and ran a for loop through the size of the inventory list and the
    * quantity to remove the needed parts. 
    */
    public void processOrders() {
        for (Order order : orders) {
        	if (checkFulfilled(order)) {
        		order.setFulfilled(true);
        		for (Integer key : order.getRequestedParts().getKeys()) {
        			List<CarPart> parts = inventory.get(key);
        			int quantity = order.getRequestedParts().get(key);
        			int count = 0;
            		while (count < parts.size() && quantity > 0) {
            			inventory.get(key).remove(0);
            			quantity--;
            			count++;
            		}
            	}
        	}
        	else {
        		order.setFulfilled(false);
        	}
        }
    }
    /**
     * Generates a report indicating how many parts were produced per machine,
     * how many of those were defective and are still in inventory. Additionally, 
     * it also shows how many orders were successfully fulfilled. 
     */
    public void generateReport() {
        String report = "\t\t\tREPORT\n\n";
        report += "Parts Produced per Machine\n";
        for (PartMachine machine : this.getMachines()) {
            report += machine + "\t(" + 
            this.getDefectives().get(machine.getPart().getId()) +" defective)\t(" + 
            this.getInventory().get(machine.getPart().getId()).size() + " in inventory)\n";
        }
       
        report += "\nORDERS\n\n";
        for (Order transaction : this.getOrders()) {
            report += transaction + "\n";
        }

        
        System.out.println(report);
    }

   

}
