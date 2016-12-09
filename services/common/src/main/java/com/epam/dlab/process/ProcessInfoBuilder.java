package com.epam.dlab.process;
/*
Copyright 2016 EPAM Systems, Inc.
 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
    http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

import com.aegisql.conveyor.Testing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.LinkedList;
import java.util.function.Supplier;

public class ProcessInfoBuilder implements Supplier<ProcessInfo>, Testing {

    private final static Logger LOG = LoggerFactory.getLogger(ProcessInfoBuilder.class);

    private final ProcessId processId;
    private final long startTimeStamp = System.currentTimeMillis();
    private ProcessStatus status = ProcessStatus.CREATED;
    private final StringBuilder stdOut = new StringBuilder();
    private final StringBuilder stdErr = new StringBuilder();
    private int exitCode = -1;
    private String command = "N/A";
    private Collection<ProcessInfo> rejected = null;

    private boolean finished = false;

    public ProcessInfoBuilder(ProcessId processId) {
        this.processId = processId;
    }

    public static void start(ProcessInfoBuilder b, String command) {
        if( b.status == ProcessStatus.CREATED ) {
            b.status = ProcessStatus.LAUNCHING;
            b.command = command;
            b.launch();
            b.status = ProcessStatus.RUNNING;
        } else {
            if(b.rejected == null) {
                b.rejected = new LinkedList<>();
            }
            long timeStamp = System.currentTimeMillis();
            b.rejected.add(new ProcessInfo(
                    b.processId,
                    ProcessStatus.REJECTED,
                    command,
                    "",
                    "rejected duplicated command",
                    ProcessStatus.REJECTED.ordinal(),
                    timeStamp,
                    timeStamp,null));
        }
    }

    public static void stop(ProcessInfoBuilder b, Object dummy) {
        b.status   = ProcessStatus.STOPPED;
        b.finished = true;
    }

    public static void kill(ProcessInfoBuilder b, Object dummy) {
        b.status   = ProcessStatus.KILLED;
        b.finished = true;
    }

    public static void finish(ProcessInfoBuilder b, Integer exitCode) {
        b.status   = ProcessStatus.FINISHED;
        b.exitCode = exitCode;
        b.finished = true;
    }

    public static void stdOut(ProcessInfoBuilder b, Object msg) {
        b.stdOut.append(msg).append("\n");
    }

    public static void stdErr(ProcessInfoBuilder b, Object msg) {
        b.stdErr.append(msg).append("\n");
    }

    private void launch() {
        DlabProcess.getInstance().getExecutorService().submit(()->{
            try {
                Process p = new ProcessBuilder(command).start();
                InputStream stdOutStream = p.getInputStream();
                DlabProcess.getInstance().getExecutorService().submit(()->{
                    BufferedReader reader = new BufferedReader(new InputStreamReader(stdOutStream));
                    String line;
                    try {
                        while ((line = reader.readLine()) != null) {
                            DlabProcess.getInstance().toStdOut(processId,line);
                        }
                    } catch (IOException e) {
                        DlabProcess.getInstance().toStdErr(processId,"Failed process STDOUT reader",e);
                        DlabProcess.getInstance().stop(processId);
                    }
                });
                InputStream stdErrStream = p.getErrorStream();
                DlabProcess.getInstance().getExecutorService().submit(()->{
                    BufferedReader reader = new BufferedReader(new InputStreamReader(stdErrStream));
                    String line;
                    try {
                        while ((line = reader.readLine()) != null) {
                            DlabProcess.getInstance().toStdErr(processId,line);
                        }
                    } catch (IOException e) {
                        DlabProcess.getInstance().toStdErr(processId,"Failed process STDERR reader",e);
                        DlabProcess.getInstance().stop(processId);
                    }
                });
                int exit = p.waitFor();
                DlabProcess.getInstance().finish(processId,exit);
            } catch (IOException e) {
                DlabProcess.getInstance().toStdErr(processId,"Command launch failed. "+get(),e);
                DlabProcess.getInstance().stop(processId);
            } catch (InterruptedException e) {
                DlabProcess.getInstance().toStdErr(processId,"Command interrupted. "+get(),e);
                DlabProcess.getInstance().stop(processId);
            }
        });
    }

    @Override
    public ProcessInfo get() {
        return new ProcessInfo(
                processId,
                status,
                command,
                stdOut.toString(),
                stdErr.toString(),
                exitCode,
                startTimeStamp, System.currentTimeMillis(),rejected );
    }

    @Override
    public boolean test() {
        return finished;
    }
}
