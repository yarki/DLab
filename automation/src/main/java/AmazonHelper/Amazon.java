package AmazonHelper;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;

import org.testng.Assert;

public class Amazon {
	
	private static final long AWS_REQUEST_TIMEOUT = 10000;
	private static final Duration CHECK_TIMEOUT = Duration.parse("PT10m");
	
    public static Instance getInstance(String instanceName) throws Exception {

            AWSCredentials _credentials = new BasicAWSCredentials(AmazonCredentials.ACCESS_KEY, AmazonCredentials.SECRET_KEY);
            AmazonEC2 _ec2 = new AmazonEC2Client(_credentials);
            _ec2.setRegion(com.amazonaws.regions.Region.getRegion(Regions.US_WEST_2));
     
            List<String> valuesT1 = new ArrayList<String>();
            valuesT1.add(instanceName + "*");
            Filter filter = new Filter("tag:Name", valuesT1);
            
            DescribeInstancesRequest describeInstanceRequest = new DescribeInstancesRequest().withFilters(filter);
            DescribeInstancesResult describeInstanceResult = _ec2.describeInstances(describeInstanceRequest);
            
            List<Reservation> reservations = describeInstanceResult.getReservations();
            
            if (reservations.size() == 0) {
            	throw new Exception("Instance "+ instanceName + " in Amazon not found");
            } else if (reservations.size() > 1) {
            	throw new Exception("Found many instances in Amazon for instance filter "+ instanceName);
            }
            
            List<Instance> instances = reservations.get(0).getInstances();
            if (instances.size() == 0) {
            	throw new Exception("Instance "+ instanceName + " in Amazon not found");
            } else if (instances.size() > 1) {
            	throw new Exception("Found many instances in Amazon for instance filter "+ instanceName);
            }
            
            return instances.get(0);
  
    }
    
    public static void checkAmazonStatus(String instanceName, String expAmazonState) throws Exception {
        System.out.println("Check status of instance " + instanceName + " on Amazon:");
        String instanceState;
        long timeout = CHECK_TIMEOUT.toMillis();
        long expiredTime = System.currentTimeMillis() + timeout;

        // TODOD: Add timeout
        while ((instanceState = Amazon.getInstance(instanceName)
            	.getState()
            	.getName()).equals("shutting-down")) {
        	if (timeout != 0 && expiredTime < System.currentTimeMillis()) {
                System.out.println("Amazon instance " + instanceName + " state is " + instanceState);           
        		throw new Exception("Timeout has been expired for check amazon instance " + instanceState);
            }
            Thread.sleep(AWS_REQUEST_TIMEOUT);
        };
        
        System.out.println("Amazon instance " + instanceName + " state is " + instanceState);           
        Assert.assertEquals(instanceState, expAmazonState, "Amazon instance " + instanceName + " state is not correct");
    }
}
