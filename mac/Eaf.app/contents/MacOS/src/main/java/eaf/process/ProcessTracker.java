package eaf.process;

import java.util.ArrayList;

public class ProcessTracker {


    public static ArrayList<ProcessInfoWorker> workers = new ArrayList<>();


    public void newInfo(ArrayList<ProcessInfo> infos) {
        for (var info : infos) {
            for (var worker : workers) {
                worker.processInfo(info);
            }
        }


    }


}
