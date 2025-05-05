import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TokenRing {

    private static class Token {
        public AtomicInteger totalRequests = new AtomicInteger(0);
    }

    private static class Process extends Thread {
        private final int id;
        private Process nextProcess;
        private Token token;
        private AtomicBoolean hasToken = new AtomicBoolean(false);
        private volatile boolean running = true;
        private AtomicBoolean hasRequest = new AtomicBoolean(false);
        private static AtomicInteger lastExecutedProcess = new AtomicInteger(-1);

        public Process(int id, Token token) {
            this.id = id;
            this.token = token;
        }

        public void setNextProcess(Process nextProcess) {
            this.nextProcess = nextProcess;
        }

        public void receiveToken() {
            hasToken.set(true);
        }

        public void stopProcess() {
            running = false;
        }

        public boolean addRequest() {
            if (!hasRequest.get()) {
                hasRequest.set(true);
                token.totalRequests.incrementAndGet();
                System.out.println("Process " + id + " added a request. Total requests: " + token.totalRequests.get());
                return true;
            }
            return false;
        }

        @Override
        public void run() {
            while (running) {
                if (hasToken.get()) {
                    if (token.totalRequests.get() > 0) {
                        // Enter critical section
                        lastExecutedProcess.set(id);
                        System.out.println("\nProcess " + id + " ENTERS critical section (Requests: " + 
                                        token.totalRequests.get() + ")");

                        // Process request if this process has one
                        if (hasRequest.get()) {
                            hasRequest.set(false);
                            token.totalRequests.decrementAndGet();
                            System.out.println("Process " + id + " processed its request. Remaining: " + 
                                            token.totalRequests.get() + " total");
                        }

                        // Simulate work in critical section
                        try {
                            Thread.sleep(1500);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }

                        System.out.println("Process " + id + " EXITS critical section");

                        // Generate new request (from any process except current)
                        generateNewRequest();

                        // Pass the token
                        passToken();
                    } else {
                        // No requests, initiate shutdown
                        System.out.println("No more requests. Process " + id + " initiating shutdown...");
                        stopAllProcesses();
                        break;
                    }
                }

                // Small delay to prevent busy waiting
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        private void generateNewRequest() {
            if (token.totalRequests.get() > 0) {
                Random rand = new Random();
                int newRequester;
                do {
                    newRequester = rand.nextInt(5) + 1;
                } while (newRequester == id);

                // Find the process and try to add request
                for (Process p : allProcesses) {
                    if (p.id == newRequester) {
                        p.addRequest();
                        break;
                    }
                }
            }
        }

        private void passToken() {
            hasToken.set(false);
            nextProcess.receiveToken();
            System.out.println("Token passed to Process " + nextProcess.id);
        }
    }

    private static Process[] allProcesses;
    private static Token sharedToken = new Token();

    private static void stopAllProcesses() {
        for (Process p : allProcesses) {
            p.stopProcess();
        }
    }

    private static void printInitialStatus() {
        System.out.println("\nInitial Process Status:");
        System.out.println("-----------------------");
        for (Process p : allProcesses) {
            System.out.println("Process " + p.id + " - Has request: " + p.hasRequest.get());
        }
        System.out.println("Total requests: " + sharedToken.totalRequests.get() + "\n");
    }

    public static void main(String[] args) throws InterruptedException {
        final int NUM_PROCESSES = 5;
        allProcesses = new Process[NUM_PROCESSES];

        // Create processes with shared token
        for (int i = 0; i < NUM_PROCESSES; i++) {
            allProcesses[i] = new Process(i + 1, sharedToken); // IDs 1-5
        }

        // Set up the ring structure
        for (int i = 0; i < NUM_PROCESSES; i++) {
            allProcesses[i].setNextProcess(allProcesses[(i + 1) % NUM_PROCESSES]);
        }

        // Generate random initial requests (1-5)
        Random rand = new Random();
        int initialRequests = rand.nextInt(5) + 1; // 1-5
        System.out.println("Initial number of requests: " + initialRequests);

        // Assign requests to random processes (each can have only one)
        for (int i = 0; i < initialRequests; i++) {
            boolean requestAdded = false;
            while (!requestAdded) {
                int processId = rand.nextInt(5) + 1;
                for (Process p : allProcesses) {
                    if (p.id == processId && !p.hasRequest.get()) {
                        requestAdded = p.addRequest();
                        break;
                    }
                }
            }
        }

        // Start all processes
        for (Process p : allProcesses) {
            p.start();
        }

        printInitialStatus();

        // Initialize the token by giving it to the first process
        allProcesses[0].receiveToken();

        // Wait for all processes to finish
        for (Process p : allProcesses) {
            p.join();
        }

        System.out.println("\nAll processes stopped. Final request count: " + sharedToken.totalRequests.get());
    }
}