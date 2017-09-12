package scheduler;
import java.util.Scanner;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;

// Currently a testing class for volunteers

public class Main {
	public static void main (String[] args) {
		int numVolunteers = 8; // Total volunteers
		int numShifts = 7; // Number of shifts in the day (standard morning shift = 7)
		
		PriorityQueue<Volunteer> volunteers = new PriorityQueue<Volunteer>(); 
		for (int i = 0; i < numVolunteers; i++) {
			volunteers.add(new Volunteer(numShifts));
		}
		
		// Asks for all the stations and capacities, produces the schedule header based on that
		Pair<Pair<String[], HashSet<Integer>>, Pair<Integer, Integer>> header = quickSchedule(); //makeScheduleArray();
		// Prints the contents, and then the indices of the unique stations
		printHeader(header);
		// Prints volunteer names
		System.out.println(volunteers);
		// Verify the schedule can be reasonably filled
		verifyScheduleValid(header.left().left(), header.right().left(), header.right().right(), numVolunteers);
		
		// Now all the information is there, start filling out a schedule.
		// TODO: Construct the schedule
		
		// First assign each volunteer to a break, updating the volunteer's fields and the overall schedule.
		// Header data reminder: ((scheduleHeader, necessaryStations), (uniqueStations, breakShifts))
		// TODO: Assign to break
		
		// Now, go through the 2-D array one timeslot at a time, filling in volunteers according to the priority
		// queue. Assign them to important stations first, then to unimportant. Use volunteer methods:
		// 
		
	}
	
	// Verify that the number of volunteers can likely fit the whole schedule
	// based on number of volunteers vs. number of unique stations minus break total
	private static void verifyScheduleValid(String[] scheduleHeader, int uniqueStations, int breakNum, int volunteerNum) {
		int minVolunteers = uniqueStations + breakNum;
		if (volunteerNum > minVolunteers) {
			System.out.println(volunteerNum + " volunteers can fill the required " 
							+ (uniqueStations + breakNum) + " shifts");
		} else  {
			System.out.println("Not enough volunteers");
		}
	}
	
	// Code that prompts the user for all the stations and capacities,
	// And produces an appropriately sized array to contain them.
	// Returns the following data: ((scheduleHeader, necessaryStations), (uniqueStations, breakShifts)).
	private static Pair<Pair<String[], HashSet<Integer>>, Pair<Integer, Integer>> makeScheduleArray() {
		int uniqueStations = 0;
		int totalStations = 0;
		LinkedList<String> stations = new LinkedList<String>();
		HashSet<Integer> necessaryStations = new HashSet<Integer>();
		
		Scanner input = new Scanner(System.in);
		String station = null;
		while (true) {
			System.out.print("Type a station name [press ENTER to stop]: ");
			station = input.nextLine();
			if (station.equals("")) {
				break;
			}
			necessaryStations.add(totalStations);
			stations.addLast(station);
			totalStations++;
			uniqueStations++;
			System.out.print("Max volunteers at station? ");
			int max = Integer.parseInt(input.nextLine());
			for (int i = 1; i < max; i++) {
				stations.add(station);
				totalStations++;
			}
		}
		// Add contents of queue into a new schedule array, then print both uniquestations and totalstations
		System.out.println("There were " + uniqueStations + " stations inputted with " + totalStations + " slots.");
		
		// Get number of break shifts
		System.out.print("Max volunteers on break? ");
		int brek = Integer.parseInt(input.nextLine());
		for (int i = 0; i < brek; i++) {
			stations.add("break");
			totalStations++;
		}
		
		String[] result = new String[totalStations];
		for (int i = 0; i < totalStations; i++) {
			result[i] = stations.removeFirst();
		}
		// Verify queue is empty
		if (!stations.isEmpty()) {
			System.out.println("Something went wrong");
		}
		
		return new Pair(new Pair(result, necessaryStations), new Pair(uniqueStations, brek));
	}
	
	// An alternative to makeScheduleArray that simply returns a sample array with
	// 3 unique stations and 9 total, plus 2 break 
	// Pair layout: ((schedule, importantIndices), (uniqueStations, breakCount))
	private static Pair<Pair<String[], HashSet<Integer>>, Pair<Integer, Integer>> quickSchedule() {
		String[] result = new String[11];
		HashSet<Integer> important = new HashSet<Integer>();
		result[0] = "s1";
		result[1] = "s1";
		result[2] = "s1";
		result[3] = "s1";
		result[4] = "s2";
		result[5] = "s2";
		result[6] = "s2";
		result[7] = "s3";
		result[8] = "s3";
		result[9] = "br";
		result[10] = "br";
		important.add(0);
		important.add(4);
		important.add(7);
		return new Pair(new Pair(result, important), new Pair(3, 2));
	}
	
	private static void printHeader(Pair<Pair<String[], HashSet<Integer>>, Pair<Integer, Integer>> header) {
		System.out.println();
		for (int i = 0; i < header.left().left().length; i++) {
			System.out.print(header.left().left()[i] + " ");
		}
		System.out.println();
		System.out.print("Important indices: ");
		for (int index : header.left().right()) {
			System.out.print(index + " ");
		}
		System.out.println();
	}
}
