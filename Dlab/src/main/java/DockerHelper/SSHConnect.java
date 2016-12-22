package DockerHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import AutomationTest.Dlab.HelperMethods;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SSHConnect {
    
    public static Session getConnect(String username, String host, int port) throws JSchException {
        Session session = null;
        Channel channel = null;
        JSch jsch = new JSch();
        Properties config = new Properties(); 
        config.put("StrictHostKeyChecking", "no");
        
        String prvkey = HelperMethods.getFilePath("BDCC-DSS-POC.ppk");
        jsch.addIdentity(prvkey);
        session = jsch.getSession(username, host, port);
        session.setConfig(config);
        session.connect();
        channel = session.openChannel("sftp");
        System.out.println("Getting connected");
        channel.connect();
        return session;
    }
    
    public static InputStream setCommand(Session session, String command) throws JSchException, IOException {
        ChannelExec channelExec = (ChannelExec)session.openChannel("exec");
        InputStream in = channelExec.getInputStream();
        channelExec.setCommand(command);
        channelExec.connect();
        return in;
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

}
