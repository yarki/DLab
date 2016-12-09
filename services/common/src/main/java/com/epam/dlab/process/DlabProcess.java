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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DlabProcess {

    private final static DlabProcess INSTANCE = new DlabProcess();

    private ExecutorService executorService = Executors.newFixedThreadPool(32);

    public static DlabProcess getInstance() {
        return INSTANCE;
    }

    private final ProcessConveyor processConveyor;

    private DlabProcess() {
        this.processConveyor = new ProcessConveyor();
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService.shutdown();
        this.executorService = executorService;
    }

    public CompletableFuture<ProcessInfo> start(ProcessId id, String command){
        processConveyor.add(id,command,ProcessStep.START);
        CompletableFuture<ProcessInfo> future = processConveyor.getFuture(id);
        return future;
    }

    public CompletableFuture<Boolean> stop(ProcessId id){
        return processConveyor.add(id,"STOP",ProcessStep.STOP);
    }

    public CompletableFuture<Boolean> kill(ProcessId id){
        return processConveyor.add(id,"KILL",ProcessStep.KILL);
    }

    public CompletableFuture<Boolean> finish(ProcessId id, Integer exitStatus){
        return processConveyor.add(id,exitStatus,ProcessStep.FINISH);
    }

    public CompletableFuture<Boolean> toStdOut(ProcessId id, String msg){
        return processConveyor.add(id,msg,ProcessStep.STD_OUT);
    }

    public CompletableFuture<Boolean> toStdErr(ProcessId id, String msg){
        return processConveyor.add(id,msg,ProcessStep.STD_ERR);
    }

    public CompletableFuture<Boolean> toStdErr(ProcessId id, String msg, Throwable err){
        StringWriter sw = new StringWriter();
        sw.append(msg);
        sw.append("\n");
        PrintWriter pw = new PrintWriter(sw);
        err.printStackTrace(pw);
        return processConveyor.add(id,sw.toString(),ProcessStep.STD_ERR);
    }

    public Collection<ProcessId> getActiveProcesses() {
        Collection<ProcessId> pList = new ArrayList<>();
        processConveyor.forEachKeyAndBuilder( (k,b)-> pList.add(k) );
        return pList;
    }

}
