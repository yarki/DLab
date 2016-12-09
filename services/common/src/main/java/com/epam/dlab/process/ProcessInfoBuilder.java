package com.epam.dlab.process;/*
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

import java.util.function.Supplier;

public class ProcessInfoBuilder implements Supplier<ProcessInfo>, Testing {

    private final long startTimeStamp = System.currentTimeMillis();
    private ProcessStatus status = ProcessStatus.CREATED;
    private final StringBuilder stdOut = new StringBuilder();
    private final StringBuilder stdErr = new StringBuilder();
    private int exitCode = -1;
    private String command = "N/A";

    private boolean finished = false;

    public static void start(ProcessInfoBuilder b, String command) {
        if( b.status == ProcessStatus.CREATED ) {
            b.status   = ProcessStatus.RUNNING;
        } else {

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


    @Override
    public ProcessInfo get() {
        return new ProcessInfo(
                status,
                command,
                stdOut.toString(),
                stdErr.toString(),
                exitCode,
                startTimeStamp, System.currentTimeMillis());
    }

    @Override
    public boolean test() {
        return finished;
    }
}
