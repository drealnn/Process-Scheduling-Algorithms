import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.*;

/**
 * Created by Daniel Sledd on 2/15/2016.
 * COP 4600
 */
public class main {

    public static void main(String[] args)
    {
        Scheduler.start("processes.in");
    }
}

class Scheduler {

    public static void start(String filename)
    {
        Scanner in;

        try{
            System.setOut(new PrintStream(new File("processes.out")));

            //System.out.println("hello world");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }



        try {
            in = new Scanner(new File(filename));
        }catch (Exception e)
        {
            e.printStackTrace();
            return;
        }

        int processcount = -1, runfor = -1, quantum = -1;
        String use = "null";
        List<process> processes = new LinkedList<process>();
        ArrayList<process> lkj = new ArrayList<>();
        HashMap<Integer, process> processHash = new HashMap<>();
        boolean exit = false;

        while (in.hasNextLine())
        {
            String line = in.nextLine();
            String newLine = "null";
            if (line.contains("#"))
                line = line.substring(0 ,line.indexOf("#"));

            //System.out.println(line);

            String[] inputs = line.split(" ");


            switch (inputs[0])
            {
                case ("processcount"):
                    processcount = Integer.parseInt(inputs[1]);
                    break;
                case ("runfor"):
                    runfor = Integer.parseInt(inputs[1]);
                    break;
                case ("use"):
                    use = inputs[1];
                    break;
                case ("quantum"):
                    quantum = Integer.parseInt(inputs[1]);
                    break;
                case ("process"):
                    process newprocess = new process();
                    newprocess.name = inputs[2];
                    newprocess.arrivalTime = Integer.parseInt(inputs[4]);
                    newprocess.burstTime = Integer.parseInt(inputs[6]);
                    newprocess.runTime = 0;

                    // hash the arrival time to the process
                    processHash.put(newprocess.arrivalTime, newprocess);
                    processes.add(newprocess);

                    break;
                case ("end"):
                    exit = true;
                    break;
            }

            if (exit)
                break;
        }


        //System.out.println(String.format("%s processes\n Using %s, %s, %s, %s", processcount, runfor, use, quantum, Arrays.toString(processes.toArray())));
        Collections.sort(processes);

        //firstComeFirstServe(processcount, runfor, processHash);
        //shortestJobFirst(processcount, runfor, processHash);
        //roundRobin(processcount, runfor, quantum, processHash);

        switch (use)
        {
            case("fcfs"):
                firstComeFirstServe(processcount, runfor, processHash);
                break;
            case("rr"):
                roundRobin(processcount, runfor, quantum, processHash);
                break;
            case("sjf"):
                shortestJobFirst(processcount, runfor, processHash);
                break;
        }

    } // end function

    public static void firstComeFirstServe(int processcount, int runfor, HashMap<Integer, process> hash)
    {
        int processed = 0;
        process currentproc = null;
        process arrivedProc;
        Queue<process> procQueue = new LinkedList<>();
        int i = 0, arrival, burst;

        System.out.printf("%d processes\nUsing First Come First Serve\n\n", processcount);


        for (; i <= runfor; i++)
        {

            // if a new process has arrived
            if ( (arrivedProc = hash.get(i)) != null)
            {
                // add the process to the queue
                procQueue.add(arrivedProc);
                process lastProc = (process) procQueue.toArray()[procQueue.size()-1];

                System.out.printf("Time %d: process %s arrives\n", i, lastProc.name);
                lastProc.turnaroundTime = i;
                // if we have no current process, dequeue
                if (currentproc == null)
                {
                    currentproc = procQueue.remove();


                    System.out.printf("Time %d: %s selected (burst %d)\n", i, currentproc.name, currentproc.burstTime);
                }

            }

            if (currentproc != null) {

                if (  currentproc.burstTime - currentproc.runTime > 0) {
                    {

                        currentproc.runTime++;
                        continue;
                    }
                } else {

                    // process is finished
                    // calculate turnaround time and wait
                    currentproc.turnaroundTime = i - currentproc.turnaroundTime;
                    currentproc.waitTime = currentproc.turnaroundTime - currentproc.burstTime;

                    System.out.printf("Time %d: %s finished\n", i, currentproc.name);

                    // dequeue to the next process
                    if (procQueue.size() > 0) {


                        // dequeue to the next process
                        // set init turnaround time
                        currentproc = procQueue.remove();
                        //currentproc.turnaroundTime = i;
                        System.out.printf("Time %d: %s selected (burst %d)\n", i, currentproc.name, currentproc.burstTime);
                        currentproc.runTime++;
                    }
                    else {

                        currentproc = null;
                        System.out.printf("Finished at time %d\n\n", i);

                    }

                }
            }
            else
                System.out.printf("Time %d: Idle\n", i);



        } // end for

        // loop through hash and print wait/turnaround values
        for (process p : hash.values())
        {
            System.out.printf("%s wait %d turnaround %d\n", p.name, p.waitTime, p.turnaroundTime);
        }
    }

    public static void shortestJobFirst(int processcount, int runfor, HashMap<Integer, process> hash)
    {

        process currentproc = null;
        process arrivedProc;

        // should be using a priority queue
        ArrayList<process> procQueue = new ArrayList<>();
        int i = 0, arrival, burst;

        System.out.printf("%d processes\nUsing Shortest Job First\n\n", processcount);


        for (; i <= runfor; i++)
        {

            // if a new process has arrived
            if ( (arrivedProc = hash.get(i)) != null)
            {
                /**
                 * check to see its burst time is less than the remaining runtime for the current process
                 * if it is, load in the process as the current process and put the running process in a sorted array
                 * if not, put the newly arrived process in an array sorted by burst time
                 **/
                arrivedProc.turnaroundTime = i;
                //procQueue.add(arrivedProc);
                // process lastProc = procQueue.get(procQueue.size()-1);

                System.out.printf("Time %d: process %s arrives\n", i, arrivedProc.name);


                // if we have no current process, load it in to currentproc
                if (currentproc == null)
                {
                    currentproc = arrivedProc;
                    System.out.printf("Time %d: %s selected (burst %d)\n", i, currentproc.name, currentproc.burstTime);
                }
                else
                {
                    // context switch
                    if (arrivedProc.burstTime < (currentproc.burstTime - currentproc.runTime))
                    {
                        procQueue.add(currentproc);
                        Collections.sort(procQueue);

                        currentproc = arrivedProc;
                        System.out.printf("Time %d: %s selected (burst %d)\n", i, currentproc.name, currentproc.burstTime);
                    }
                    // continue
                    else
                    {
                        procQueue.add(arrivedProc);
                        Collections.sort(procQueue);
                    }
                }

            }

            if (currentproc != null) {

                if (  currentproc.burstTime - currentproc.runTime > 0) {
                    {

                        currentproc.runTime++;
                        continue;
                    }
                } else {

                    // process is finished
                    // calculate turnaround time and wait
                    currentproc.turnaroundTime = i - currentproc.turnaroundTime;
                    currentproc.waitTime = currentproc.turnaroundTime - currentproc.burstTime;

                    System.out.printf("Time %d: %s finished\n", i, currentproc.name);

                    // dequeue to the next process
                    if (procQueue.size() > 0) {


                        // dequeue to the next process
                        currentproc = procQueue.get(0);
                        procQueue.remove(0);
                        //currentproc.turnaroundTime = i;
                        System.out.printf("Time %d: %s selected (burst %d)\n", i, currentproc.name, currentproc.burstTime - currentproc.runTime);
                        currentproc.runTime++;
                    }
                    else {

                        currentproc = null;
                        System.out.printf("Time %d: Idle\n", i);

                    }

                }
            }
            //else
            //    System.out.printf("Time %d: Idle\n", i);



        } // end for

        System.out.printf("Finished at time %d\n\n", --i);

        // loop through hash and print wait/turnaround values
        for (process p : hash.values())
        {
            System.out.printf("%s wait %d turnaround %d\n", p.name, p.waitTime, p.turnaroundTime);
        }
    }

    public static void roundRobin(int processcount, int runfor, int quantum, HashMap<Integer, process> hash)
    {
        process currentproc = null;
        process arrivedProc;

        // FIFO queue
        Queue<process> procQueue = new LinkedList<>();
        int i = 0, arrival, burst;

        System.out.printf("%d processes\nUsing Round Robin\nQuantum %d\n\n", processcount, quantum);


        for (; i <= runfor; i++)
        {

            // if a new process has arrived
            if ( (arrivedProc = hash.get(i)) != null)
            {
                /*
                    Once a new process arrives, put it at the end of the queue
                */
                arrivedProc.turnaroundTime = i;
                //procQueue.add(arrivedProc);
                // process lastProc = procQueue.get(procQueue.size()-1);

                System.out.printf("Time %d: process %s arrives\n", i, arrivedProc.name);


                // if we have no current process, load it in to currentproc
                if (currentproc == null)
                {
                    currentproc = arrivedProc;
                    System.out.printf("Time %d: %s selected (burst %d)\n", i, currentproc.name, currentproc.burstTime);
                }
                else
                    procQueue.add(arrivedProc);


            }

            if (currentproc != null) {

                /**
                 *  If we have fulfilled the time quantum, we must context switch
                 *      to the next process in the queue, putting the current process
                 *      at the end of the queue
                 */

                if ((i != 0) && (i % quantum == 0))
                {
                    if (procQueue.size() > 0)
                    {
                        process temp = currentproc;
                        currentproc = procQueue.remove();
                        procQueue.add(temp);
                    }

                    System.out.printf("Time %d: %s selected (burst %d)\n", i, currentproc.name, currentproc.burstTime - currentproc.runTime);
                }

                if (  currentproc.burstTime - currentproc.runTime > 0) {
                    {

                        currentproc.runTime++;
                        continue;
                    }
                } else {

                    // process is finished
                    // calculate turnaround time and wait
                    currentproc.turnaroundTime = i - currentproc.turnaroundTime;
                    currentproc.waitTime = currentproc.turnaroundTime - currentproc.burstTime;

                    System.out.printf("Time %d: %s finished\n", i, currentproc.name);

                    // dequeue to the next process
                    if (procQueue.size() > 0) {


                        // dequeue to the next process
                        currentproc = procQueue.remove();
                        //currentproc.turnaroundTime = i;
                        System.out.printf("Time %d: %s selected (burst %d)\n", i, currentproc.name, currentproc.burstTime - currentproc.runTime);
                        currentproc.runTime++;
                    }
                    else {

                        currentproc = null;
                        System.out.printf("Time %d: Idle\n", i);

                    }

                }
            }
            //else
            //    System.out.printf("Time %d: Idle\n", i);



        } // end for

        System.out.printf("Finished at time %d\n\n", --i);

        // loop through hash and print wait/turnaround values
        for (process p : hash.values())
        {
            System.out.printf("%s wait %d turnaround %d\n", p.name, p.waitTime, p.turnaroundTime);
        }
    }

    process getNextProcess(Queue<process> q)
    {
        int min = q.peek().arrivalTime;
        process currentproc = q.peek();
        for (process p : q)
        {
            if (p.arrivalTime < min) {
                min = p.arrivalTime;
                currentproc = p;
            }
        }

        return currentproc;
    }

}


class process implements Comparable<process> {
    public String name;
    public int arrivalTime;
    public int burstTime;
    public int waitTime;
    public int turnaroundTime;
    public int runTime;

    public String toString()
    {
        return String.format("%s, %s, %s", name, arrivalTime, burstTime);
    }


    @Override
    public int compareTo(process o) {
        return this.burstTime - o.burstTime;
    }
}
