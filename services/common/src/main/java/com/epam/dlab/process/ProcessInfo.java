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

public class ProcessInfo {

    private final String command;
    private final ProcessStatus status;
    private final String stdOut;
    private final String stdErr;
    private final int exitCode;
    private final long startTimeStamp;
    private final long infoTimeStamp;


    ProcessInfo(ProcessStatus status, String command, String stdOut, String stdErr, int exitCode, long startTimeStamp, long infoTimeStamp) {
        this.status         = status;
        this.command        = command;
        this.stdOut         = stdOut;
        this.stdErr         = stdErr;
        this.exitCode       = exitCode;
        this.startTimeStamp = startTimeStamp;
        this.infoTimeStamp  = infoTimeStamp;
    }

    public String getCommand() {
        return command;
    }

    public ProcessStatus getStatus() {
        return status;
    }

    public String getStdOut() {
        return stdOut;
    }

    public String getStdErr() {
        return stdErr;
    }

    public int getExitCode() {
        return exitCode;
    }

    public long getStartTimeStamp() {
        return startTimeStamp;
    }

    public long getInfoTimeStamp() {
        return infoTimeStamp;
    }

    @Override
    public String toString() {
        return "ProcessInfo{" +
                "command='" + command + '\'' +
                ", status=" + status +
                ", stdOut='" + stdOut + '\'' +
                ", stdErr='" + stdErr + '\'' +
                ", exitCode=" + exitCode +
                ", startTimeStamp=" + startTimeStamp +
                ", infoTimeStamp=" + infoTimeStamp +
                '}';
    }
}
