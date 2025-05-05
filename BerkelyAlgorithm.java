import java.sql.Time;
import java.text.*;
import java.util.*;

public class BerkelyAlgorithm 
{
    public static void main(String[] args) throws ParseException 
    {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter number of clients in your network: ");
        int clientCount = sc.nextInt();
        sc.nextLine();
        String[] timeString = new String[1 + clientCount];
        for (int i = 0; i < timeString.length; i++) 
        {
            if (i == 0) 
            {
                System.out.print("Enter time displayed in Server (HH:mm): ");
            } 
            else 
            {
                System.out.print("Enter time displayed in Client " + i + " (HH:mm): ");
            }
            String time = sc.nextLine();
            timeString[i] = time;
        }
        System.out.println("\nBefore Synchronization");
        displayTime(timeString,"");
        berkeleyAlgorithm(timeString);
        System.out.println("\nAfter Synchronization");
        displayTime(timeString, "Synchronized ");
        sc.close();
    }

    public static void berkeleyAlgorithm(String[] timeString) throws ParseException 
    {
        int n = timeString.length;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        long[] timeInMilliseconds = new long[n];

        System.out.println("Time in Milliseconds: ");
        for (int i = 0; i < n; i++) 
        {
            timeInMilliseconds[i] = simpleDateFormat.parse(timeString[i]).getTime();
            System.out.print(timeInMilliseconds + " ");
        }
        System.out.println("\n\n");



        long serverTime = timeInMilliseconds[0];
        long[] differenceInTimeWithServer = new long[n];

        System.out.println("Difference in Time with Server: ");
        for (int i = 0; i < n; i++) 
        {
            differenceInTimeWithServer[i] = timeInMilliseconds[i]- serverTime;
            System.out.print(differenceInTimeWithServer[i] / (1000*60) + " ");
        }
        System.out.println("\n\n");

        long avg = 0;
        for (int i = 0; i < n; i++) 
        {
            avg += differenceInTimeWithServer[i];
        }
        avg /= n;
        System.out.println("Fault tolerant average = " + avg + "\t{" + avg / (1000 * 60) + "} "); 

        for (int i = 0; i < n; i++) 
        {
            long offset = avg- differenceInTimeWithServer[i];
            timeInMilliseconds[i] += offset;
            if (i == 0) 
            {
                continue;
            }
            System.out.println("Clock " + i + " adjustment = " + offset / (1000 * 60));
        }
        for (int i = 0; i < n; i++) 
        {
            timeString[i] = simpleDateFormat.format(new Time(timeInMilliseconds[i]));
        }
    }

    private static void displayTime(String[] time, String prefix) 
    {
        System.out.println(prefix + "Server Clock:\t" + time[0]);
        for (int i = 1; i < time.length; i++) 
        {
            System.out.println(prefix + "Client " + i + " Clock:\t" + time[i]);
        }
        System.out.println();
    }
}