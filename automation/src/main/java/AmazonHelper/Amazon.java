package AmazonHelper;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.Grant;

import AutomationTest.PropertyValue;

import org.testng.Assert;

public class Amazon {
	
	private static final Duration CHECK_TIMEOUT = Duration.parse("PT10m");
	
	private static AWSCredentials getCredentials() {
		return new BasicAWSCredentials(PropertyValue.getAwsAccessKeyId(), PropertyValue.getAwsSecretAccessKey());
	}
	
	private static Region getRegion() {
		return Region.getRegion(Regions.fromName(PropertyValue.getAwsRegion()));
	}
	
    public static List<Instance> getInstances(String instanceName) throws Exception {
            AWSCredentials credentials = getCredentials();
            AmazonEC2 ec2 = new AmazonEC2Client(credentials);
            ec2.setRegion(getRegion());
     
            List<String> valuesT1 = new ArrayList<String>();
            valuesT1.add(instanceName + "*");
            Filter filter = new Filter("tag:Name", valuesT1);
            
            DescribeInstancesRequest describeInstanceRequest = new DescribeInstancesRequest().withFilters(filter);
            DescribeInstancesResult describeInstanceResult = ec2.describeInstances(describeInstanceRequest);
            
            List<Reservation> reservations = describeInstanceResult.getReservations();
            
            if (reservations.size() == 0) {
            	throw new Exception("Instance "+ instanceName + " in Amazon not found");
            }
            
            List<Instance> instances = reservations.get(0).getInstances();
            if (instances.size() == 0) {
            	throw new Exception("Instance "+ instanceName + " in Amazon not found");
            }
            
            return instances;
    }
    
    public static Instance getInstance(String instanceName) throws Exception {
        return getInstances(instanceName).get(0);
    }

    public static void checkAmazonStatus(String instanceName, String expAmazonState) throws Exception {
        System.out.println("Check status of instance " + instanceName + " on Amazon:");
        String instanceState;
        long requestTimeout = PropertyValue.getAwsRequestTimeout().toMillis();
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
            Thread.sleep(requestTimeout);
        };
        
        for (Instance instance : Amazon.getInstances(instanceName)) {
            System.out.println("Amazon instance " + instanceName + " with private IP " + instance.getPrivateIpAddress() + " state is " + instanceState);           
		}
        Assert.assertEquals(instanceState, expAmazonState, "Amazon instance " + instanceName + " state is not correct");
    }

    public static void printBucketGrants(String bucketName) throws Exception {
    	System.out.println("Print grants for bucket " + bucketName + " on Amazon:");
        AWSCredentials credentials = getCredentials();
        AmazonS3 s3 = new AmazonS3Client(credentials);
        
        s3.setRegion(getRegion());
        AccessControlList acl = s3.getBucketAcl(bucketName);
        for (Grant grant : acl.getGrants()) {
			System.out.println(grant);
		}
    }
}
