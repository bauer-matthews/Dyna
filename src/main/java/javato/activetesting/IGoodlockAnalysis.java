package javato.activetesting;

import javato.activetesting.activechecker.ActiveChecker;
import javato.activetesting.analysis.AnalysisImpl;
import javato.activetesting.analysis.Observer;
import javato.activetesting.common.Parameters;
import javato.activetesting.igoodlock.GoodlockDS;
import javato.activetesting.reentrant.IgnoreRentrantLock;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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
public class IGoodlockAnalysis extends AnalysisImpl {

    private static final String PROGRAM_NAME;
    private static final String WORKING_DIR;
    private static final boolean PRINT_TRACE;
    private static final Map<Integer, String> IID_MAP;

    private static Writer WRITER;

    static {

        Properties sysProps = System.getProperties();
        PROGRAM_NAME = sysProps.getProperty("programName");
        WORKING_DIR = sysProps.getProperty("mydir");

        if (sysProps.getProperty("printTrace").equalsIgnoreCase("yes")) {
            PRINT_TRACE = true;
        } else {
            PRINT_TRACE = false;
        }

        IID_MAP = new HashMap<Integer, String>();

        try {
            WRITER = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(WORKING_DIR + "/" + PROGRAM_NAME + ".trace"), "utf-8"));
        } catch (IOException ex) {
        }
    }

    private GoodlockDS gl;
    private IgnoreRentrantLock ignoreRentrantLock;

    public void initialize() {
        synchronized (ActiveChecker.lock) {

            IID_MAP.clear();

            ignoreRentrantLock = new IgnoreRentrantLock();
            gl = new GoodlockDS();
        }
    }

    public void lockBefore(Integer iid, Integer thread, Integer lock, Object actualLock) {
        synchronized (ActiveChecker.lock) {
            if (ignoreRentrantLock.lockBefore(thread, lock)) {
                gl.lock(iid, thread, lock);

                if (PRINT_TRACE) {
                    try {
                        WRITER.write("T" + thread + "|" + "acq(L" + lock + ")|" + iid + "\n");
                        IID_MAP.put(iid, Observer.getIidToLine(iid));
                    } catch (IOException ex) {
                    }
                }
            }
        }
    }

    public void unlockAfter(Integer iid, Integer thread, Integer lock) {
        synchronized (ActiveChecker.lock) {
            if (ignoreRentrantLock.unlockAfter(thread, lock)) {
                gl.unlock(iid, thread, lock);

                if (PRINT_TRACE) {
                    try {
                        WRITER.write("T" + thread + "|" + "rel(L" + lock + ")|" + iid + "\n");
                        IID_MAP.put(iid, Observer.getIidToLine(iid));
                    } catch (IOException ex) {
                    }
                }
            }
        }
    }

    public void newExprAfter(Integer iid, Integer object, Integer objOnWhichMethodIsInvoked) {
    }

    public void methodEnterBefore(Integer iid, Integer thread) {
    }

    public void methodExitAfter(Integer iid, Integer thread) {
    }

    public void startBefore(Integer iid, Integer parent, Integer child) {

        if (PRINT_TRACE) {
            synchronized (ActiveChecker.lock) {
                try {
                    WRITER.write("T" + parent + "|" + "fork(T" + child + ")|" + iid + "\n");
                } catch (IOException ex) {

                }
            }
        }
    }

    public void waitAfter(Integer iid, Integer thread, Integer lock) {

        if (PRINT_TRACE) {
            synchronized (ActiveChecker.lock) {
                try {
                    WRITER.write("T" + thread + "|" + "wait(L" + lock + ")|" + iid + "\n");
                } catch (IOException ex) {

                }
            }
        }
    }

    public void notifyBefore(Integer iid, Integer thread, Integer lock) {

        if (PRINT_TRACE) {
            synchronized (ActiveChecker.lock) {
                try {
                    WRITER.write("T" + thread + "|" + "notify(L" + lock + ")|" + iid + "\n");
                } catch (IOException ex) {

                }
            }
        }
    }

    public void notifyAllBefore(Integer iid, Integer thread, Integer lock) {

        if (PRINT_TRACE) {
            synchronized (ActiveChecker.lock) {
                try {
                    WRITER.write("T" + thread + "|" + "notifyAll(L" + lock + ")|" + iid + "\n");
                } catch (IOException ex) {

                }
            }
        }
    }

    public void joinAfter(Integer iid, Integer parent, Integer child) {

        if (PRINT_TRACE) {
            synchronized (ActiveChecker.lock) {
                try {
                    WRITER.write("T" + parent + "|" + "join(T" + child + ")|" + iid + "\n");
                } catch (IOException ex) {

                }
            }
        }
    }

    public void readBefore(Integer iid, Integer thread, Long memory, boolean isVolatile) {

        if (PRINT_TRACE) {
            synchronized (ActiveChecker.lock) {
                try {
                    WRITER.write("T" + thread + "|" + "r(V" + memory + ")|" + iid + "\n");
                } catch (IOException ex) {

                }
            }
        }
    }

    public void writeBefore(Integer iid, Integer thread, Long memory, boolean isVolatile) {

        if (PRINT_TRACE) {
            synchronized (ActiveChecker.lock) {
                try {
                    WRITER.write("T" + thread + "|" + "w(V" + memory + ")|" + iid + "\n");
                } catch (IOException ex) {

                }
            }
        }
    }

    public void finish() {
        synchronized (ActiveChecker.lock) {
            int nDeadlocks;
            nDeadlocks = gl.dumpDeadlocks();
            Observer.writeIntegerList(Parameters.ERROR_LIST_FILE, nDeadlocks);

            try {
                WRITER.flush();
                WRITER.close();
            } catch (Exception ex) {

            }


            try {
                Writer iidWriter = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(WORKING_DIR + "/" + PROGRAM_NAME + ".linemap"), "utf-8"));

                for (Integer i : IID_MAP.keySet()) {
                    iidWriter.write(i + " -> " + Observer.getIidToLine(i) + "\n");
                }

                iidWriter.flush();
                iidWriter.close();

            } catch (Exception ex) {
            }

            IID_MAP.clear();
        }
    }
}
