package eaf.process;

import java.util.ArrayList;

public abstract class ProcessInfoWorker {


    public ProcessInfoWorker() {
        ProcessTracker.workers.add(this);
    }


    public abstract void processInfo(ProcessInfo info);
}
