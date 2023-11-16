package main;

import interfaces.Queue;
import data_structures.ListQueue;
import java.util.Random;

public class PartMachine {
   
	/**
	 * private fields of PartMachine class, some variables are initialized here.
	 * -initialized timer as queue
	 * -initialized conveyorBelt as queue because it simulate a real one were new produced parts are at the end and older ones are at the front
	 */
	private int id;
	private CarPart p1;
	private double weightError;
	private int period;
	private int defectiveChance;
	private Queue<Integer> timer = new ListQueue<Integer>();
	private int totalPartsProduced = 0;
	private Queue<CarPart> conveyorBelt = new ListQueue<CarPart>();
	private Random random = new Random();
	
	/**
	 * 
	 * @param id
	 * @param p1
	 * @param period
	 * @param weightError
	 * @param chanceOfDefective
	 * initialized the private fields of the class
	 * -for time i created a queue using period
	 * -for conveyorBelt i filled it with 10 null values
	 */
    public PartMachine(int id, CarPart p1, int period, double weightError, int chanceOfDefective) {
        this.id = id;
        this.p1 = p1;
        this.period = period;
        this.weightError = weightError;
        this.defectiveChance = chanceOfDefective;
        for (int time = this.period-1; time >=0 ; time--){
        	this.timer.enqueue(time);
        }
        for (int iterator = 0; iterator<10; iterator++) {
        	this.conveyorBelt.enqueue(null);
        }
    }
    
    public int getId() {
    	return this.id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public Queue<Integer> getTimer() {
    	return this.timer;
    }
    
    public void setTimer(Queue<Integer> timer) {
        this.timer = timer;
    }
    
    public CarPart getPart() {
    	return this.p1;
    }
    
    public void setPart(CarPart part1) {
        this.p1 = part1;
    }
    
    public Queue<CarPart> getConveyorBelt() {
        return this.conveyorBelt;
    }
    
    public void setConveyorBelt(Queue<CarPart> conveyorBelt) {
    	this.conveyorBelt = conveyorBelt;
    }
    
    public int getTotalPartsProduced() {
        return this.totalPartsProduced;
    }
    
    public void setTotalPartsProduced(int count) {
    	this.totalPartsProduced = count;
    }
    
    public double getPartWeightError() {
        return this.weightError;
    }
    
    public void setPartWeightError(double partWeightError) {
        this.weightError = partWeightError;
    }
    public int getChanceOfDefective() {
        return this.defectiveChance;
    }
    
    public void setChanceOfDefective(int chanceOfDefective) {
        this.defectiveChance = chanceOfDefective;
    }
    
    /**
     * cleared the belt and the filled it again with 10 null elements. 
     */
    public void resetConveyorBelt() {
        this.conveyorBelt.clear();
        int iterator = 0;
        while (iterator < 10) {
        	conveyorBelt.enqueue(null);
        	iterator++;
        }
    }
    
    /**
     * returns the front of the queue and enqueues it again after dequeuing it. 
     * @return
     */
    public int tickTimer() {
        int result = this.getTimer().front();
        this.getTimer().enqueue(this.getTimer().dequeue());
        return result;
    }
    
    /**
     * 
     * @return
     * -set the weight, min and max weight with the error, and if it its defective
     * -used the given equation to check if it was defective
     * -if timer is 0 created the randomWeight and created a new car part with the information and added it ot the conveyor belt
     * -increased total parts produced by one
     * -if no part was produced a null element was enqueued
     * -it returns the front of the conveyor belt
     */
    public CarPart produceCarPart() {
    	double weight = this.getPart().getWeight();
    	double min = weight - this.getPartWeightError();
    	double max = weight + this.getPartWeightError();
    	boolean defective = false;
    	if (this.getTotalPartsProduced()%this.getChanceOfDefective() == 0) {
    		defective = true;
    	}
    	if (this.tickTimer() == 0) {
    		double randomWeight = min + (max-min) * random.nextDouble();
    		CarPart newPart = new CarPart(this.getPart().getId(),this.getPart().getName(), randomWeight, defective);
    		this.getConveyorBelt().enqueue(newPart);
    		this.setTotalPartsProduced(this.getTotalPartsProduced()+1);
    	}
    	else {
    		this.getConveyorBelt().enqueue(null);
    	}
    	return this.getConveyorBelt().dequeue();
    }

    /**
     * Returns string representation of a Part Machine in the following format:
     * Machine {id} Produced: {part name} {total parts produced}
     */
    @Override
    public String toString() {
        return "Machine " + this.getId() + " Produced: " + this.getPart().getName() + " " + this.getTotalPartsProduced();
    }
    /**
     * Prints the content of the conveyor belt. 
     * The machine is shown as |Machine {id}|.
     * If the is a part it is presented as |P| and an empty space as _.
     */
    public void printConveyorBelt() {
        // String we will print
        String str = "";
        // Iterate through the conveyor belt
        for(int i = 0; i < this.getConveyorBelt().size(); i++){
            // When the current position is empty
            if (this.getConveyorBelt().front() == null) {
                str = "_" + str;
            }
            // When there is a CarPart
            else {
                str = "|P|" + str;
            }
            // Rotate the values
            this.getConveyorBelt().enqueue(this.getConveyorBelt().dequeue());
        }
        System.out.println("|Machine " + this.getId() + "|" + str);
    }
}
