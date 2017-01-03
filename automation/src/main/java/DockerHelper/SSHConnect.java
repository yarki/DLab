package DockerHelper;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import AutomationTest.PropertyValue;

public class SSHConnect {
    
    public static Session getConnect(String username, String host, int port) throws JSchException {
        Session session = null;
        Channel channel = null;
        JSch jsch = new JSch();
        Properties config = new Properties(); 
        config.put("StrictHostKeyChecking", "no");
        
        jsch.addIdentity(PropertyValue.getAccessKeyPrivFileName());
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

    public static AckStatus copyLocalFileToRemote(Session session, String localPath, String remotePath)
            throws JSchException, IOException {

        AckStatus status;
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        try {
            String command = "scp -t " + remotePath;
            channel.setCommand(command);

            // get I/O streams for remote scp
            OutputStream out = channel.getOutputStream();
            try {
                InputStream in = channel.getInputStream();
                channel.connect();

                status = checkAck(in);
                if (!status.isOk()) {
                    return status;
                }

                File localFile = new File(localPath);

                // send "C0644 filesize filename", where filename should not include '/'
                long fileSize = localFile.length();
                command = "C0644 " + fileSize + " ";
                command += (localPath.lastIndexOf('/') > 0 ?
                     localPath.substring(localPath.lastIndexOf('/') + 1) : localPath);
                command += "\n";
                out.write(command.getBytes());
                out.flush();

                status = checkAck(in);
                if (!status.isOk()) {
                    return status;
                }

                // send a content of localPath
                FileInputStream fis = new FileInputStream(localPath);
                byte[] buf = new byte[1024];
                try {
                    while (true) {
                        int len = fis.read(buf, 0, buf.length);
                        if (len <= 0) break;
                        out.write(buf, 0, len); //out.flush();
                    }
                }
                finally {
                    fis.close();
                }

                // send '\0'
                buf[0] = 0;
                out.write(buf, 0, 1);
                out.flush();

                status = checkAck(in);
            }
            finally {
                out.close();
            }
        }
        finally {
            channel.disconnect();
        }
        return status;
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

    static AckStatus checkAck(InputStream in) throws IOException{
        int status = in.read();
        String message = "";

        // b may be 0 for success,
        //          1 for error,
        //          2 for fatal error,
        //          -1
        if(status == 1 || status == 2){
            StringBuffer sb=new StringBuffer();
            int c;

            do {
                c=in.read();
                sb.append((char)c);
            }
            while(c!='\n');

            message = sb.toString();
            System.out.println(message);
        }
        return new AckStatus(status, message);
    }

}
