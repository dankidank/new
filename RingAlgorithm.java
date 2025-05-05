import java.util.Scanner;
import java.util.ArrayList;

// create process class for creating a process having id and status
class Process {
    public int id;
    public String status;
    public int electedLeaderId; // Added to store the elected leader

    public Process(int id) {
        this.id = id;
        this.status = "active";
        this.electedLeaderId = -1; // Initialize with -1, meaning no leader yet
    }
}

public class RingAlgorithm {

    Scanner sc;
    Process[] processes;
    int n; 
    int initiatorId;

    // initialize Scanner class object in constructor
    public RingAlgorithm() {
        sc = new Scanner(System.in);
    }

    // create ring() method for initializing the ring
    public void ring() {
        // get input from the user for processes
        System.out.print("Enter total number of processes: ");
        n = sc.nextInt();

        // initialize processes array
        processes = new Process[n];
        for (int i = 0; i < n; i++) {
            processes[i] = new Process(i);
        }
    }

    // create election() method for electing process
    public void performElection() {
        if (n <= 0) {
            System.out.println("No processes to perform election.");
            return;
        }

        // Get the ID of the process initiating the election.
        System.out.print("Enter the ID of the process initiating the election: ");
        initiatorId = sc.nextInt();

        if (initiatorId < 0 || initiatorId >= n) {
            System.out.println("Invalid process ID.  Please enter a valid ID (0 to " + (n - 1) + ").");
            return;
        }
        if (processes[initiatorId].status.equals("inactive"))
        {
            System.out.println("Process " + initiatorId + " is inactive.  Election cannot be started from this process.");
            return;
        }

        // Create a list to hold the IDs of participating processes.
        ArrayList<Integer> participatingProcesses = new ArrayList<>();
        int currentId = initiatorId;
        do {
            if (processes[currentId].status.equals("active")) {
                participatingProcesses.add(currentId);
            }
            currentId = (currentId + 1) % n; // Move to the next process in the ring
        } while (currentId != initiatorId);

        //If no active processes other than the initiator
        if(participatingProcesses.size() == 1){
            processes[initiatorId].electedLeaderId = initiatorId;
            System.out.println("Process " + initiatorId + " declares itself as the leader.");
            return;
        }
        // Print the processes participating in the election
        System.out.println("Processes participating in the election: " + participatingProcesses);


        int nextId = initiatorId;
        int highestId = initiatorId; // Start with the initiator's ID

        // Phase 1: Pass the ID around the ring
        System.out.println("\nPhase 1: Passing IDs:");
        do {
            nextId = (nextId + 1) % n;
            if (processes[nextId].status.equals("active")) {
                System.out.println("Process " + processes[nextId].id + " receives ID " + highestId + " from Process " + processes[(nextId - 1 + n) % n].id);
                if (processes[nextId].id > highestId) {
                    highestId = processes[nextId].id;
                }
            }
        } while (nextId != initiatorId);

        // Phase 2: Announce the leader
        System.out.println("\nPhase 2: Announcing the leader:");
        int leaderId = highestId;
        nextId = initiatorId;
        do {
            nextId = (nextId + 1) % n;
            if (processes[nextId].status.equals("active")) {
                System.out.println("Process " + processes[nextId].id + " receives Leader(" + leaderId + ") from Process " + processes[(nextId - 1 + n) % n].id);
                processes[nextId].electedLeaderId = leaderId; //store the leader
            }
        } while (nextId != initiatorId);

        System.out.println("\nElection Complete.");
        System.out.println("The elected leader is Process " + leaderId);
        processes[leaderId].electedLeaderId = leaderId;
        }

    public static void main(String[] args) {
        RingAlgorithm ring = new RingAlgorithm();
        ring.ring();
        ring.performElection();
    }
}
