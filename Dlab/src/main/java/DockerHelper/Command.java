package DockerHelper;

public class Command {
    
    public static final String GET_CONTAINERS = "echo -e \"GET /containers/json?all=1 HTTP/1.0\r\n\" | nc -U /var/run/docker.sock";

}
