package AmazonHelper;

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
import org.testng.Assert;

public class Amazon {
    
    public static DescribeInstancesResult getInstanceResult(String instanceName) throws Exception {

            AWSCredentials _credentials = new BasicAWSCredentials(AmazonCredentials.ACCESS_KEY, AmazonCredentials.SECRET_KEY);
            AmazonEC2 _ec2 = new AmazonEC2Client(_credentials);
            _ec2.setRegion(com.amazonaws.regions.Region.getRegion(Regions.US_WEST_2));
     
            List<String> valuesT1 = new ArrayList<String>();
            valuesT1.add("*" + instanceName + "*");
            
            Filter filter = new Filter("tag:Name", valuesT1);
            
             
            DescribeInstancesRequest describeInstanceRequest = new DescribeInstancesRequest().withFilters(filter);
           
            DescribeInstancesResult describeInstanceResult = _ec2.describeInstances(describeInstanceRequest);
            
            if (describeInstanceResult.getReservations().size() == 0) {
            	throw new Exception("Instance "+ instanceName + " in Amazon not found");
            }
            
            return describeInstanceResult;
  
    }
    
    public static void checkAmazonStatus(String instanceName, String expAmazonState) throws Exception {
        
        System.out.println("Check status of instance " + instanceName + " on Amazon:");
        String instanceState;
        
        while ((instanceState = Amazon.getInstanceResult(instanceName)
            	.getReservations()
            	.get(0)
            	.getInstances()
            	.get(0)
            	.getState()
            	.getName()).equals("shutting-down")) {
            Thread.sleep(1000);
        };
        
        System.out.println("Amazon instance " + instanceName + " state is " + expAmazonState);           
        Assert.assertEquals(instanceState, expAmazonState, "Amazon instance " + instanceName + " state is not correct");
    }
}
