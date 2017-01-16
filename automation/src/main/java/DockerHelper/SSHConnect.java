package DockerHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import AutomationTest.PropertyValue;

public class SSHConnect {
    
    public static Session getConnect(String username, String host, int port) throws JSchException {
        Session session;
        JSch jsch = new JSch();
        Properties config = new Properties(); 
        config.put("StrictHostKeyChecking", "no");
        
        jsch.addIdentity(PropertyValue.getAccessKeyPrivFileName());
        session = jsch.getSession(username, host, port);
        session.setConfig(config);
        session.connect();

        System.out.println(String.format("Getting connected to %s:%d", host, port));
        return session;
    }

    public static Session getForwardedConnect(String username, String hostAlias, int port) throws JSchException {
        Session session;
        JSch jsch = new JSch();
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");

        jsch.addIdentity(PropertyValue.getAccessKeyPrivFileName());
        session = jsch.getSession(username, "127.0.0.1", port);
        session.setConfig(config);
        session.setHostKeyAlias(hostAlias);
        session.connect();
        System.out.println(String.format("Getting connected to %s through 127.0.0.1:%d", hostAlias, port));
        return session;
    }
    
    public static ChannelExec setCommand(Session session, String command)
            throws JSchException, IOException, InterruptedException {
        System.out.println(String.format("Setting command: %s", command));

        ChannelExec channelExec = (ChannelExec)session.openChannel("exec");
        channelExec.setCommand(command);
        channelExec.connect();

        return channelExec;
    }

    public static List<DockerContainer> getdockerContainerList(InputStream in) throws JsonParseException, JsonMappingException, IOException{
        
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

}
