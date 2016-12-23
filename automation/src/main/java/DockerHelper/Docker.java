package DockerHelper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import org.testng.Assert;

public class Docker {
    
    public static void checkDockerStatus(String dockerImageName, String publicIP) throws IOException, JSchException{
        
        System.out.println("Check status of instanse on Docker:");
        
        Session session = SSHConnect.getConnect("ubuntu", publicIP, 22);            
        InputStream in = SSHConnect.setCommand(session, Command.GET_CONTAINERS);    
        List<DockerContainer> dockerContainerList = SSHConnect.getdockerContainerList(in);
        
        DockerContainer dockerContainer = SSHConnect.getDockerContainer(dockerContainerList, dockerImageName);

        Assert.assertEquals(dockerContainer.getStatus().contains(Status.EXITED_0), true, "Status of container is not  Exited (0)");
        
        System.out.println("Docker image " + dockerImageName + " has status Exited (0)");
        }

}
