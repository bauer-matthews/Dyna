package javato.activetesting.lockset;


import javato.activetesting.analysis.Observer;
import javato.activetesting.igoodlock.Pair;

import java.io.*;
import java.util.*;

/**
 * Copyright (c) 2007-2008,
 * Koushik Sen    <ksen@cs.berkeley.edu>
 * All rights reserved.
 * <p/>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * <p/>
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * <p/>
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * <p/>
 * 3. The names of the contributors may not be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 * <p/>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
public class LockSetTracker {

    private static final String PROGRAM_NAME;
    private static final String WORKING_DIR;
    private static final String TRIAL_NUM;
    private static final String PATTERN_NUM;

    private static Writer WRITER;
    private static int deadlockCounter;

    // thread -> list of iids
    private Map<Integer, LinkedList<Integer>> threadsToIidStack = new TreeMap<Integer, LinkedList<Integer>>();
    private Map<Integer, LinkedList<Integer>> threadsToLockStack = new TreeMap<Integer, LinkedList<Integer>>();
    private Map<Integer, Integer> holdsLockToThread = new TreeMap<Integer, Integer>();
    boolean isDeadlock = false;
    //private ArrayList<String> iidToLineMap = Observer.getIidToLineMap(Parameters.iidToLineMapFile);

    static {
        Properties sysProps = System.getProperties();
        PROGRAM_NAME = sysProps.getProperty("programName");
        WORKING_DIR = sysProps.getProperty("mydir");
        TRIAL_NUM = (sysProps.getProperty("trialNum") != null ? sysProps.getProperty("trialNum") : "NULL" );
        PATTERN_NUM = (sysProps.getProperty("patternNum") != null ? sysProps.getProperty("patternNum") : "NULL" );
    }


    /**
     * updates lockset for thread t
     * returns true iff a deadlock is detected
     *
     * @param thread
     * @param iid
     * @param lockId
     * @return
     */
    public boolean lockBefore(Integer iid, Integer thread, Integer lockId) {
        LinkedList<Integer> iidStack = threadsToIidStack.get(thread);
        LinkedList<Integer> lockStack = threadsToLockStack.get(thread);
        if (iidStack == null) {
            iidStack = new LinkedList<Integer>();
            threadsToIidStack.put(thread, iidStack);
        }
        if (lockStack == null) {
            lockStack = new LinkedList<Integer>();
            threadsToLockStack.put(thread, lockStack);
        }
        iidStack.addLast(iid);
        if (!lockStack.isEmpty()) {
            holdsLockToThread.put(lockStack.getLast(), thread);
        }
        lockStack.addLast(lockId);

        if (!isDeadlock && isDeadlock(thread, lockId)) {

            try {

                System.out.println(WORKING_DIR + "/deadlocks/" + PROGRAM_NAME + ".p" + PATTERN_NUM + ".t" + TRIAL_NUM +
                         ".deadlock");

                WRITER = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(WORKING_DIR + "/deadlocks/" + PROGRAM_NAME + ".p" + PATTERN_NUM + ".t" + TRIAL_NUM +
                                ".deadlock"), "utf-8"));
            } catch (IOException ex) {
            }

            try {
                WRITER.write("##############################################################\n");
                WRITER.write("Real Deadlock Detected\n");
                WRITER.write("##############################################################\n");
                printDeadlock();
                WRITER.write("##############################################################\n");
            } catch (IOException ex) {

            }

            try {
                WRITER.flush();
                WRITER.close();
            } catch (IOException ex) {

            }

            deadlockCounter++;

            isDeadlock = true;
            System.err.println("##############################################################");
            System.err.println("Real Deadlock Detected");
            System.err.println("##############################################################");
            printDeadlock();
            System.err.println("##############################################################");
            return true;
        }
        return false;
    }

    private void printDeadlock() {

        try {
            //WRITER.write("Thread and lock sets:" + Thread.currentThread() + "\n");
            for (Integer tid : threadsToIidStack.keySet()) {
                WRITER.write("Thread:  (" + tid + ")\n");
                LinkedList<Integer> iids = threadsToIidStack.get(tid);
                LinkedList<Integer> locks = threadsToLockStack.get(tid);
                int i = 0;
                for (Integer lid : locks) {
                    Integer iid = iids.get(i);
                    WRITER.write("    Lock  (" + locks.get(i) + ") at " + Observer.getIidToLine(iid)+ "\n");
                    i++;
                }
            }
        } catch (IOException ex) {

        }

        System.out.println("Thread and lock sets:" + Thread.currentThread());
        for (Integer tid : threadsToIidStack.keySet()) {
            System.out.println("Thread:  (" + tid + ")");
            LinkedList<Integer> iids = threadsToIidStack.get(tid);
            LinkedList<Integer> locks = threadsToLockStack.get(tid);
            int i = 0;
            for (Integer lid : locks) {
                Integer iid = iids.get(i);
                System.out.println("    Lock  (" + locks.get(i) + ") at " + Observer.getIidToLine(iid));
                i++;
            }
        }
    }

    /**
     * unlocks the last acquired lock
     *
     * @param thread
     */
    public void unlockAfter(Integer thread) {
        LinkedList<Integer> iidStack = threadsToIidStack.get(thread);
        assert iidStack != null;
        iidStack.removeLast();
        LinkedList<Integer> lockStack = threadsToLockStack.get(thread);
        assert (lockStack != null);
        Integer lockId = lockStack.removeLast();
        if (thread.equals(holdsLockToThread.get(lockId)))
            holdsLockToThread.remove(lockId);
    }

    /**
     * returns true iff the current lockset of all threads implies a deadlock
     *
     * @param threadId
     * @param lockId
     * @return
     */
    private boolean isDeadlock(Integer threadId, Integer lockId) {
        Integer tmpThread = threadId;
        Integer tmpLock = lockId; // what tmpThread intends to acquire
        while (true) {
            Integer oldThread = tmpThread;
            tmpThread = holdsLockToThread.get(tmpLock);
            // no one else holds the lock
            if (tmpThread == null) {
                return false;
            }
            if (tmpThread.equals(oldThread)) {
                return false;
            }
            if (tmpThread.equals(threadId)) {
                return true;
            }
            tmpLock = threadsToLockStack.get(tmpThread).getLast();
        }
    }

    public Pair<Integer, Integer> locationsInvolvedInDeadlock(Integer threadId, Integer lockId) {
        Integer iid1 = threadsToIidStack.get(threadId).getLast();
        Integer iid2;
        Integer otherThread = holdsLockToThread.get(lockId);
        LinkedList<Integer> iids = threadsToIidStack.get(otherThread);
        return new Pair<Integer, Integer>(iid1, iids.getLast());
    }

    /**
     * returns a list of the locations at which locks in the current lockset of thread are acquired
     * first element in the list being the outermost lock
     *
     * @param thread
     * @return
     */
    public List<Integer> getLockSetIids(Integer thread) {
        LinkedList<Integer> ls = threadsToIidStack.get(thread);
        if (ls == null) {
            return (new LinkedList<Integer>());
        }
        return (new LinkedList<Integer>(ls));
    }

    /**
     * returns a list of locks currently held by the thread
     * first element in the list being the oldest acquired lock
     *
     * @param thread
     * @return
     */
    public List<Integer> getLockList(Integer thread) {
        LinkedList<Integer> ls = threadsToLockStack.get(thread);
        if (ls == null) {
            return (new LinkedList<Integer>());
        }
        return (new LinkedList<Integer>(ls));
    }

    /**
     * returns a list of locks currently held by the thread
     * first element in the list being the oldest acquired lock
     *
     * @param thread
     * @return
     */
    public LockSet getLockSet(Integer thread) {
        LinkedList<Integer> ls = threadsToLockStack.get(thread);
        if (ls == null) {
            return (new LockSet());
        }
        return (new LockSet(ls));
    }


    public Integer getLockAcquireIID(Integer thread, Integer lock) {
        LinkedList<Integer> ls = threadsToLockStack.get(thread);
        int index = ls.indexOf(lock);
        return threadsToIidStack.get(thread).get(index);
    }
}
