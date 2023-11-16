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
	
	private List<PartMachine> machines;
	private List<Order> orders;
	private Map<Integer, CarPart> partCatalog;
	private Map<Integer, List<CarPart>> inventory;
	private Map<Integer, Integer> defective;
	private Stack<CarPart> productionBin;

        
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

    public void setupInventory() {
        inventory = new HashTableSC<Integer, List<CarPart>>(partCatalog.size(), new BasicHashFunction());
        for (PartMachine partMachine : machines) {
        	List<CarPart> tempList = new SinglyLinkedList<CarPart>();
        	inventory.put(partMachine.getId(), tempList);
        }
        
    }
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
    
    public boolean checkFulfilled(Order order) {
    	boolean fulfilled = true;
    	Map<Integer, Integer> requested = order.getRequestedParts();
    	for (Integer key : requested.getKeys()) {
    		if (inventory.containsKey(key)) {
    			if (requested.get(key) >  inventory.get(key).size()) {
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
   
    public void processOrders() {
        for (Order order : orders) {
        	if (checkFulfilled(order)) {
        		order.setFulfilled(true);
        		for (Integer key : order.getRequestedParts().getKeys()) {
        			List<CarPart> parts = inventory.get(key);
        			int quantity = order.getRequestedParts().get(key);
            		for (int count = 0; count < parts.size() && quantity > 0; count++) {
            			inventory.get(key).remove(0);
            			quantity--;
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
