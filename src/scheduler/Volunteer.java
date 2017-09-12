// Marcus Riley, Summer 2017
//
// This class represents a volunteer to be scheduled, containing their schedule along with information
// about what they've already been assigned to. 
//
// Can be compared to other volunteers, with volunteers having more required shifts being considered greater
// so that they have less priority in the PriorityQueue.

package scheduler;

public class Volunteer implements Comparable<Volunteer> {
	private String name;  			// Their name
	private boolean isTeen;  		// UNUSED - for facepainting
	private int lastStation;  		// UNUSED - The index of the last station worked at
	private int[] schedule;  		// An array of the volunteer's shifts in order (use map for names)
	int breakTime;          		// The index of the volunteer's break timeslot
	int requiredShifts;  			// The number of high priority shifts, for comparable
	int totalShifts;  				// Number of shifts assigned
	private static int numVolunteers = 0; // Number of volunteers created, for naming
	
	// Constructs a new volunteer with the given name, saying if it is a teen, 
	// and making a schedule array with the given number of timeslots.
	public Volunteer(String name, int scheduleSize, boolean isTeen) {
		this.name = name;
		this.isTeen = isTeen;
		this.schedule = new int[scheduleSize];
		this.lastStation = -1;
		this.breakTime = -1;
		this.requiredShifts = 0;
		this.totalShifts = 0;
		numVolunteers++;
	}
	
	// Constructor without teen specifier but with name
	public Volunteer(String name, int scheduleSize) {
		this(name, scheduleSize, true);
	}
	
	// Constructor without teen specifier, for debugging
	public Volunteer(int scheduleSize) {
		this("v" + (numVolunteers + 1), scheduleSize, true);
	}
	
	// Inserts a shift's value at the given index (timeslot), incrementing total if it was previously empty
	// Assumes that no shifts were assigned the value of 0 
	public void insertShift(int time, int shift) {
		if (time < 0) {
			System.out.println("invalid index");
			return;
		}
		if (schedule[time] == 0) {
			totalShifts++;
		}
		schedule[time] = shift;
	}
	
	// Same as insertshift, but increments requiredShift count also
	public void insertPriorityShift(int time, int shift) {
		if (time < 0) {
			System.out.println("invalid index");
			return;
		}
		if (schedule[time] == 0) {
			requiredShifts++;
			totalShifts++;
		}
		schedule[time] = shift;
	}
	
	// Returns true if all shifts are assigned, as a sanity check
	public boolean fullyAssigned() {
		return totalShifts == schedule.length;
	}
	
	// Returns a copy of the schedule
	public int[] getSchedule() {
		return schedule.clone();
	}
	
	@Override
	// A volunteer with more required shifts is given a larger value (lower priority).
	// This ensures they are assigned more filler shifts in the assignment process.
	public int compareTo(Volunteer other) {
		return this.requiredShifts - other.requiredShifts;
	}
	
	public String toString() {
		return "{" + name + ":" + this.requiredShifts + "}";
	}

}
