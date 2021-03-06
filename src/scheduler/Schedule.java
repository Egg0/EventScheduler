// The schedule is represented by a 2-dimensional int array, with the outer array being
// the timeslot, and the inner array being the "index" of the shift, consistent with the header
// Two internal maps will be used to relate each volunteer name to a number and vice versa,
// To save on storage space but make insertion via name possible.

package scheduler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;

public class Schedule {
// TODO: make methods to build the schedule: One to assign break, one to assign rest? maybe a shuffle method?
	private HashSet<Volunteer> volunteers;
	private HashMap<Volunteer, Integer> volToId;	// convert a volunteer into their int ID
	private HashMap<Integer, Volunteer> idToVol;	// convert a int (volunteer ID) into the volunteer
	private HashMap<String, TreeSet<Integer>> stationToIndex;	// convert a station name into the list of indices
	private HashMap<Integer, String> indexToStation;  // convert a station index into the station name 
	
	private int timeslotCount;
	private HashSet<Integer> priorityShifts;	// The list of priority indices
	Set<Integer> nonPriorityShifts;				// Nonpriority indices
	private String[] header; 	// Header provided by client
	private int[][] schedule;	// The actual schedule
	
	// Construct a schedule with a header, important indices, a list of volunteers, and the number of timeslots
	public Schedule(String[] header, HashSet<Integer> important, HashSet<Volunteer> volunteers, int timeslotCount) {
		this.header = header;
		this.priorityShifts = important;
		this.timeslotCount = timeslotCount;
		this.volunteers = volunteers;
		
		// Insert volunteers into hashmap
		volToId = new HashMap<Volunteer, Integer>();
		idToVol = new HashMap<Integer, Volunteer>();
		int id = 1;  // Volunteer ID starts at 1 to avoid issues on schedule
		for (Volunteer v : volunteers) {
			volToId.put(v, id);
			idToVol.put(id,  v);
			id++;
		}
		
		// Insert stations into hashmap
		stationToIndex = new HashMap<String, TreeSet<Integer>>();
		indexToStation = new HashMap<Integer, String>();
		for (int i = 0; i < header.length; i++) {
			// Not in the map yet, create a new set
			if (stationToIndex.get(header[i]) == null) {
				TreeSet<Integer> indices = new TreeSet<Integer>();
				indices.add(i);
				stationToIndex.put(header[i], indices);
			// Otherwise set exists, just add to it
			} else {
				stationToIndex.get(header[i]).add(i);
			}
			indexToStation.put(i, header[i]);
		}
		// System.out.println(stationToIndex);
		// Make a set of non-priority shifts
		nonPriorityShifts = indexToStation.keySet();
		for (int i : stationToIndex.get("br")) {
			nonPriorityShifts.remove(i);
		}
		for (int i : priorityShifts) {
			nonPriorityShifts.remove(i);
		}
		
		// Finally, create the shell of the schedule.
		schedule = new int[timeslotCount][header.length];
	}
	
	// Assigns all volunteers to breaks, avoiding the first two shifts of the day and the last where possible
	// Pre: stationToIndex contains a key "br" representing the break indices
	//		schedule is not already filled
	// TODO: Eventually add support for volunteers working full days, giving them two breaks before the end
	public void scheduleBreaks() {
		TreeSet<Volunteer> volTree = new TreeSet<Volunteer>(); 
		for (Volunteer v : volunteers) {
			volTree.add(v);
		}
		Iterator<Volunteer> vols = volTree.iterator();
		TreeSet<Integer> breakSet = stationToIndex.get("br");
		ArrayList<Integer> breakIndices = new ArrayList<Integer>(breakSet);
		HashMap<Integer, ArrayList<Integer>> emptySlots = new HashMap<Integer, ArrayList<Integer>>(); // empty slots
		
		int minTime = 2;  // Avoid assigning break before two shifts have passed 
		int maxTime = timeslotCount - 2;  // Don't make the last shift have break
		
		//System.out.println("Assigning breaks from slot " + minTime + " to " + maxTime);
		
		// Assign middle slots: max / 2 and max / 2 + 1 if odd, only max / 2 if even
		int slotA = timeslotCount / 2;
		int slotB;
		slotB = (timeslotCount % 2 == 1) ? (timeslotCount / 2 + 1) : -1;
		// Now fill them completely
		for (int idx : breakIndices) {
			assignBreak(vols.next(), timeslotCount / 2, idx);
			if (!vols.hasNext()) {
				break;
			}
		}
		if (slotB > 0) {
			for (int idx : breakIndices) {
				assignBreak(vols.next(), timeslotCount / 2 + 1, idx);
				if (!vols.hasNext()) {
					break;
				}
			}
		}
		
		// From here, go outwards one at a time, putting one less than break index each time
		int maxBreaks = breakIndices.size() - 1;
		slotB = (slotB < 0) ? slotA : slotB;  // Update slotB location to slotA if not already assigned
		boolean flip = true;  // To go one direction at a time
		boolean aDone = false;  // Cutting the loop if a is done
		boolean bDone = false;  // Same for b
		while (vols.hasNext()) {
			if (flip && !bDone) { // Go forward
				slotB++;
				if (slotB > maxTime) {
					bDone = true;
					flip = !flip;
					continue;
				}
				// Assign through maxbreaks
				for (int i = 0; i < maxBreaks; i++) {
					assignBreak(vols.next(), slotB, breakIndices.get(i));
					if (!vols.hasNext()) {
						break;
					}
				}
				// Add to empty slots map for later
				ArrayList<Integer> empties = new ArrayList<Integer>();
				for (int i = maxBreaks; i < breakIndices.size(); i++) {
					empties.add(breakIndices.get(i));
				}
				emptySlots.put(slotB, empties);
				flip = !flip;
				if (aDone) {
					maxBreaks--;
				}
			} else if (!flip && !aDone) { // Go backwards
				slotA--;
				if (slotA < minTime) {
					aDone = true;
					flip = !flip;
					continue;
				}
				// Assign through maxbreaks
				for (int i = 0; i < maxBreaks; i++) {
					assignBreak(vols.next(), slotA, breakIndices.get(i));
					if (!vols.hasNext()) {
						break;
					}
				}
				// Add to empty slots map for later
				ArrayList<Integer> empties = new ArrayList<Integer>();
				for (int i = maxBreaks; i < breakIndices.size(); i++) {
					empties.add(breakIndices.get(i));
				}
				emptySlots.put(slotA, empties);
				flip = !flip;
				maxBreaks--;  // decrement maxbreaks
			} else {  // Both a and b are done, break the loop
				break;
			}
		}
		
		// TODO: Now, if there are still volunteers that need assigning, do the following:
		// 1. Scan through stations and fill any with empty slots (use emptySlots to find out what has space)
		// 2. Fill in slot 2 if still more volunteers
		// 3. At this point the last 2 slots cannot be filled (unless full day), so print an error message
		//System.out.println(emptySlots);
		while (vols.hasNext()) {
			// Step 1
			if (!emptySlots.isEmpty()) {
				for (int time : emptySlots.keySet()) {
					ArrayList<Integer> indices = emptySlots.get(time);
					for (int i = 0; i < indices.size() && vols.hasNext(); i++) {
						assignBreak(vols.next(), time, indices.get(i));
					}
					if (!vols.hasNext()) {
						break;
					}
				}
				emptySlots.clear();
			}
			// Step 2
			for (int i = 0; i < breakIndices.size(); i++) {
				if (!vols.hasNext()) {
					break;
				} else {
					assignBreak(vols.next(), 1, breakIndices.get(i));
				}
			}
			if (vols.hasNext()) {
				System.out.println("Too many volunteers to fit in break slots, exiting...");
				return;
			}
		}
	}
	
	// The meat of the program.
	// Uses all the volunteer info to schedule a day, using a priority queue 
	// and updating priorities based on required shifts
	// TODO: Make it so a volunteer cannot have two shifts in a row!
	public void schedule () {
		// Step 1: Set up a priorityQueue of volunteers
		PriorityQueue<Volunteer> vols = new PriorityQueue<Volunteer>(volToId.keySet());
		HashSet<Volunteer> volsOnBreak = new HashSet<Volunteer>();  // Volunteers on break to be ignored for a loop
		
		// Now, for each row in the schedule, fill it first with priorityShifts, then nonPriorityShifts
		for (int i = 0; i < schedule.length; i++) {
			// Priority first, assume there are enough volunteers
			for (int pshift : priorityShifts) {
				Volunteer next = vols.poll();
				assignPriority(next, i, pshift);
			}
			// Now non-priority, cut short if all volunteers used
			for (int shift : nonPriorityShifts) {
				Volunteer next = vols.poll();
				if (next == null) {
					break;
				}
				assign(next, i, shift);
			}
			// Queue exhausted, reset it, accounting for volunteers on break.
			// Only this if it's not the last iteration
			if (i + 1 != schedule.length) {
				// Add volsOnBreak back to the volunteers set
				for (Volunteer v : volsOnBreak) {
					volunteers.add(v);
				}
				volsOnBreak.clear();
				// Remove if on break at time i + 1
				int time = i + 1;
				Iterator<Volunteer> itr = volunteers.iterator();
				while (itr.hasNext()) {
					Volunteer v = itr.next();
					if (v.getBreak() == time) {
						volsOnBreak.add(v);
						itr.remove();
					}
				}
				vols = new PriorityQueue<Volunteer>(volunteers);
//				System.out.println(vols);
//				System.out.println(toString());
			}
		}
//		while (!vols.isEmpty()) {
//			System.out.println(vols.poll());
//		}
	}
	
	// Simply inserts the volunteer into the station by looking up their id and putting into the schedule
	// Also updates the volunteer's personal schedule
	private void assign (Volunteer v, int timeSlot, int station) {
		//System.out.println("assigning " + v + " to " + timeSlot + "," + station);
		v.insertShift(timeSlot, station);
		schedule[timeSlot][station] = volToId.get(v);
	}
	
	private void assignPriority (Volunteer v, int timeSlot, int station) {
		//System.out.println("assigning " + v + " to " + timeSlot + "," + station);
		v.insertPriorityShift(timeSlot, station);
		schedule[timeSlot][station] = volToId.get(v);
	}
	
	// Assigns a break by using insertBreak
	private void assignBreak (Volunteer v, int timeSlot, int station) {
		v.insertBreak(timeSlot, station);
		schedule[timeSlot][station] = volToId.get(v);
	}
	
	// Clears the schedule, for restarting the process.
	// Maintains all other passed data, just empties the schedule array.
	public void clear() {
		int dimOne = schedule.length;
		int dimTwo = schedule[0].length;
		schedule = new int[dimOne][dimTwo];
	}
	
	// TODO: Add support for afternoon shift and early shift
	public String toString() {
		String res = "";
		int time = 9;
		boolean timeflip = false ;
		for (int i = 0; i < schedule.length; i++) {
			res += "[" + i + "] ";
			res += time;
			if (timeflip) {
				res += ":00-" + time + ":30 ";
			} else {
				res += ":30-" + (time + 1) + ":00 ";
				time = time + 1;
			}
			timeflip = !timeflip;
			res += schedule[i][0];
			for (int j = 1; j < schedule[i].length; j++) {
				res += ", " + schedule[i][j];
			}
			res += "\n";
		}
		return res;
	}
}
