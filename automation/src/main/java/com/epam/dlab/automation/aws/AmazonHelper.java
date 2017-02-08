package com.epam.dlab.automation.aws;

import com.epam.dlab.automation.helper.PropertyValue;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.Grant;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class AmazonHelper {

    private final static Logger LOGGER = LogManager.getLogger(AmazonHelper.class);
	
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
            	throw new Exception("Instance "+ instanceName + " in AmazonHelper not found");
            }
            
            List<Instance> instances = reservations.get(0).getInstances();
            if (instances.size() == 0) {
            	throw new Exception("Instance "+ instanceName + " in AmazonHelper not found");
            }
            
            return instances;
    }
    
    public static Instance getInstance(String instanceName) throws Exception {
        return getInstances(instanceName).get(0);
    }

    public static void checkAmazonStatus(String instanceName, String expAmazonState) throws Exception {
        LOGGER.info("Check status of instance {} on AmazonHelper:", instanceName);
        String instanceState;
        long requestTimeout = PropertyValue.getAwsRequestTimeout().toMillis();
    	long timeout = CHECK_TIMEOUT.toMillis();
        long expiredTime = System.currentTimeMillis() + timeout;

        // TODO: Add timeout
        while ((instanceState = AmazonHelper.getInstance(instanceName)
            	.getState()
            	.getName()).equals("shutting-down")) {
        	if (timeout != 0 && expiredTime < System.currentTimeMillis()) {
                LOGGER.info("AmazonHelper instance {} state is {}", instanceName, instanceState);
        		throw new Exception("Timeout has been expired for check amazon instance " + instanceState);
            }
            Thread.sleep(requestTimeout);
        }
        
        for (Instance instance : AmazonHelper.getInstances(instanceName)) {
            LOGGER.info("AmazonHelper instance {} with private IP {} state is {}", instanceName ,instance.getPrivateIpAddress(), instanceState);
		}
        Assert.assertEquals(instanceState, expAmazonState, "AmazonHelper instance " + instanceName + " state is not correct");
    }

    public static void printBucketGrants(String bucketName) throws Exception {
        LOGGER.info("Print grants for bucket {} on AmazonHelper:" , bucketName);
        AWSCredentials credentials = getCredentials();
        AmazonS3 s3 = new AmazonS3Client(credentials);
        
        s3.setRegion(getRegion());
        AccessControlList acl = s3.getBucketAcl(bucketName);
        for (Grant grant : acl.getGrants()) {
            LOGGER.info(grant);
		}
    }
}
