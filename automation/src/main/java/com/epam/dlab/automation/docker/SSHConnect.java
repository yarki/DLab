package com.epam.dlab.automation.docker;

import com.epam.dlab.automation.helper.ConfigPropertyValue;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class SSHConnect {
    private final static Logger LOGGER = LogManager.getLogger(SSHConnect.class);
    public static final String LOCALHOST_IP = "127.0.0.1";

    public static Session getConnect(String username, String host, int port) throws JSchException {
        Session session;
        JSch jsch = new JSch();

        Properties config = new Properties(); 
        config.put("StrictHostKeyChecking", "no");
        
        jsch.addIdentity(ConfigPropertyValue.getAccessKeyPrivFileName());
        session = jsch.getSession(username, host, port);
        session.setConfig(config);
        session.connect();

        LOGGER.info("Getting connected to {}:{}", host, port);
        return session;
    }

    public static Session getSession(String username, String host, int port) throws JSchException {
        Session session;
        JSch jsch = new JSch();

        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");

        jsch.addIdentity(ConfigPropertyValue.getAccessKeyPrivFileName());
        session = jsch.getSession(username, host, port);
        session.setConfig(config);
        session.connect();


        LOGGER.info("Getting connected to {}:{}", host, port);
        return session;
    }

    public static ChannelSftp getChannelSftp(Session session) throws JSchException {
        Channel channel = session.openChannel("sftp");
        channel.connect();
        ChannelSftp channelSftp =(ChannelSftp)channel;

        return channelSftp;
    }

    public static Session getForwardedConnect(String username, String hostAlias, int port) throws JSchException {
        Session session;
        JSch jsch = new JSch();
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");

        jsch.addIdentity(ConfigPropertyValue.getAccessKeyPrivFileName());
        //TODO: figure out what is 127.0.0.1 and why it is hardcoded
        session = jsch.getSession(username, LOCALHOST_IP, port);
        session.setConfig(config);
        session.setHostKeyAlias(hostAlias);
        session.connect();
        LOGGER.info("Getting connected to {} through {}:{}", hostAlias, LOCALHOST_IP, port);
        return session;
    }
    
    public static ChannelExec setCommand(Session session, String command)
            throws JSchException, IOException, InterruptedException {
        LOGGER.info("Setting command: {}", command);

        ChannelExec channelExec = (ChannelExec)session.openChannel("exec");
        channelExec.setCommand(command);
        channelExec.connect();

        return channelExec;
    }

    public static List<DockerContainer> getDockerContainerList(InputStream in) throws IOException {
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));         
        String line;
        List<DockerContainer> dockerContainerList = null;

        TypeReference<List<DockerContainer>> typeRef = new TypeReference<List<DockerContainer>>() { };
        ObjectMapper mapper = new ObjectMapper();
        
        List<String> result = new ArrayList<String>();
        while ((line = reader.readLine()) != null)       
        {      
             result.add(line); 
             if (line.contains("Id"))
             {
                 dockerContainerList = mapper.readValue(line, typeRef);
             }       
             
        }
        
        return dockerContainerList;
    }
    
    public static DockerContainer getDockerContainer(List<DockerContainer> dockerContainerList, String name) {
        DockerContainer dockerContainer = null;
        String containerName;
   
        for(Iterator<DockerContainer> i = dockerContainerList.iterator(); i.hasNext(); )
        {
            dockerContainer = i.next();
            containerName = dockerContainer.getNames().get(0);
            if(containerName.contains(name)){
                break;
            }
         
        }
        return dockerContainer;
    }

    public static AckStatus checkAck(ChannelExec channel) throws IOException, InterruptedException {
        channel.setOutputStream(System.out, true);
    	channel.setErrStream(System.err, true);

        int status;
        while(channel.getExitStatus() == -1) {
            Thread.sleep(1000);
        }
        status = channel.getExitStatus();

        return new AckStatus(status, "");
    }

    public static AckStatus checkAck(ChannelSftp channel) throws IOException, InterruptedException {
        channel.setOutputStream(System.out, true);
//        channel.setErrStream(System.err, true);

        int status;
        while(channel.getExitStatus() == -1) {
            Thread.sleep(1000);
        }
        status = channel.getExitStatus();

        return new AckStatus(status, "");
    }

}
