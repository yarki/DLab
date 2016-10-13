import boto3, boto
import time
import sys
import os


def put_to_bucket(bucket_name, local_file, destination_file):
    try:
        s3 = boto3.client('s3')
        with open(local_file, 'rb') as data:
            s3.upload_fileobj(data, bucket_name, destination_file)
        return True
    except:
        return False


def create_s3_bucket(bucket_name, tag):
    s3 = boto3.resource('s3')
    bucket = s3.create_bucket(Bucket=bucket_name)
    tagging = bucket.Tagging()
    tagging.put(Tagging={'TagSet': [tag]})
    tagging.reload()
    return bucket.name


def create_vpc(vpc_cidr, tag):
    ec2 = boto3.resource('ec2')
    vpc = ec2.create_vpc(CidrBlock=vpc_cidr)
    vpc.create_tags(Tags=[tag])
    return vpc.id


def create_subnet(vpc_id, subnet, tag):
    ec2 = boto3.resource('ec2')
    subnet = ec2.create_subnet(VpcId=vpc_id, CidrBlock=subnet)
    subnet.create_tags(Tags=[tag])
    subnet.reload()
    return subnet.id


def create_instance(definitions, instance_tag):
    ec2 = boto3.resource('ec2')
    security_groups_ids = []
    for chunk in definitions.security_group_ids.split(','):
        security_groups_ids.append(chunk.strip())
    user_data = ''
    if definitions.user_data_file != '':
        try:
            with open(definitions.user_data_file, 'r') as f:
                for line in f:
                    user_data = user_data + line
            f.close()
        except:
            print("Error reading user-data file")
    instances = ec2.create_instances(ImageId=definitions.ami_id, MinCount=1, MaxCount=1,
                                     KeyName=definitions.key_name,
                                     SecurityGroupIds=security_groups_ids,
                                     InstanceType=definitions.instance_type,
                                     SubnetId=definitions.subnet_id,
                                     IamInstanceProfile={'Name': definitions.iam_profile},
                                     UserData=user_data)
    for instance in instances:
        print "Waiting for instance " + instance.id + " become running."
        instance.wait_until_running()
        instance.create_tags(Tags=[{'Key': 'Name', 'Value': definitions.node_name}, instance_tag])
        return instance.id
    return ''


def create_iam_role(role_name, role_profile):
    conn = boto.connect_iam()
    conn.create_role(role_name)
    conn.create_instance_profile(role_profile)
    conn.add_role_to_instance_profile(role_profile, role_name)
    time.sleep(10)


def attach_policy(policy_arn, role_name):
    conn = boto.connect_iam()
    conn.attach_role_policy(policy_arn, role_name)


def create_attach_policy(policy_name, role_name, file_path):
    conn = boto.connect_iam()
    with open(file_path, 'r') as myfile:
        json = myfile.read()
    conn.put_role_policy(role_name, policy_name, json)


def remove_role(instance_type, scientist=''):
    print "[Removing roles and instance profiles]"
    client = boto3.client('iam')
    if instance_type == "ssn":
        role_name = os.environ['conf_service_base_name'] + '-ssn-Role'
        role_profile_name = os.environ['conf_service_base_name'] + '-ssn-Profile'
    if instance_type == "edge":
        role_name = os.environ['conf_service_base_name'] + '-edge-Role'
        role_profile_name = os.environ['conf_service_base_name'] + '-edge-Profile'
    elif instance_type == "notebook":
        role_name = os.environ['conf_service_base_name'] + '-' + "{}".format(scientist) + '-nb-Role'
        role_profile_name = os.environ['conf_service_base_name'] + '-' + "{}".format(scientist) + '-nb-Profile'
    try:
        role = client.get_role(RoleName="{}".format(role_name)).get("Role").get("RoleName")
    except:
        print "Wasn't able to get role!"
        sys.exit(1)
    policy_list = client.list_attached_role_policies(RoleName=role).get('AttachedPolicies')
    for i in policy_list:
        policy_arn = i.get('PolicyArn')
        client.detach_role_policy(RoleName=role, PolicyArn=policy_arn)
    try:
        profile = client.get_instance_profile(InstanceProfileName="{}".format(role_profile_name)).get(
            "InstanceProfile").get("InstanceProfileName")
    except:
        print "Wasn't able to get instance profile!"
        sys.exit(1)
    try:
        client.remove_role_from_instance_profile(InstanceProfileName=profile, RoleName=role)
    except:
        print "\nWasn't able to remove role from instance profile!"
        sys.exit(1)
    try:
        client.delete_instance_profile(InstanceProfileName=profile)
    except:
        print "\nWasn't able to remove instance profile!"
        sys.exit(1)
    try:
        client.delete_role(RoleName=role)
    except:
        print "\nWasn't able to remove role!"
        sys.exit(1)
    print "The IAM role " + role + " has been deleted successfully"


def remove_s3(bucket_type, scientist=''):
    print "[Removing S3 buckets]"
    s3 = boto3.resource('s3')
    client = boto3.client('s3')
    if bucket_type == 'ssn':
        bucket_name = (os.environ['conf_service_base_name'] + '-ssn-bucket').lower().replace('_', '-')
    elif bucket_type == 'edge':
        bucket_name = (os.environ['conf_service_base_name'] + '-' + "{}".format(scientist) + '-edge-bucket').lower().replace('_', '-')
    bucket = s3.Bucket("{}".format(bucket_name))
    try:
        list_obj = client.list_objects(Bucket=bucket.name)
    except:
        print "Wasn't able to get S3!"
    try:
        list_obj = list_obj.get('Contents')
    except:
        print "Wasn't able to get S3!"
    if list_obj is not None:
        for o in list_obj:
            list_obj = o.get('Key')
            client.delete_objects(
                Bucket=bucket_name,
                Delete={'Objects': [{'Key': list_obj}]}
            )
            print "The S3 bucket " + bucket.name + " has been cleaned"
    try:
        client.delete_bucket(Bucket=bucket.name)
    except:
        print "Wasn't able to remove S3!"
    print "The S3 bucket " + bucket.name + " has been deleted successfully"


def remove_subnets():
    print "[Removing subnets]"
    ec2 = boto3.resource('ec2')
    client = boto3.client('ec2')
    tag_name = os.environ['conf_service_base_name'] + '-tag'
    subnets = ec2.subnets.filter(
        Filters=[{'Name': 'tag:{}'.format(tag_name), 'Values': ['*']}])
    for subnet in subnets:
        print subnet.id
        client.delete_subnet(SubnetId=subnet.id)
        print "The subnet " + subnet.id + " has been deleted successfully"


def remove_sgroups():
    print "[Removing security groups]"
    ec2 = boto3.resource('ec2')
    client = boto3.client('ec2')
    tag_name = os.environ['conf_service_base_name']
    sgs = ec2.security_groups.filter(
        Filters=[{'Name': 'tag:{}'.format(tag_name), 'Values': ['*']}])
    for sg in sgs:
        print sg.id
        client.delete_security_group(GroupId=sg.id)
        print "The security group " + sg.id + " has been deleted successfully"


def deregister_image(scientist):
    print "[De-registering images]"
    client = boto3.client('ec2')
    response = client.describe_images(
        Filters=[{'Name': 'name', 'Values': ['{}-{}-notebook-image'.format(os.environ['conf_service_base_name'], scientist)]}])
    images_list = response.get('Images')
    for i in images_list:
        client.deregister_image(ImageId=i.get('ImageId'))


def remove_ec2(tag_name, tag_value):
    print "[Removing EC2]"
    ec2 = boto3.resource('ec2')
    client = boto3.client('ec2')
    try:
        instances = ec2.instances.filter(
            Filters=[{'Name': 'instance-state-name', 'Values': ['running', 'stopped']},
                     {'Name': 'tag:{}'.format(tag_name), 'Values': ['{}'.format(tag_value)]}])
        for instance in instances:
            client.terminate_instances(InstanceIds=[instance.id])
            waiter = client.get_waiter('instance_terminated')
            waiter.wait(InstanceIds=[instance.id])
            print "The instance " + instance.id + " has been deleted successfully"
    except:
        sys.exit(1)


def terminate_emr(cluster_id, bucket_name):
    client = boto3.client('emr')
    try:
        cluster = client.describe_cluster(ClusterId=cluster_id)
        cluster = cluster.get("Cluster")
        emr_name = cluster.get('Name')
        client.terminate_job_flows(JobFlowIds=[cluster_id])
        print "The EMR cluster " + emr_name + " has been deleted successfully"
    except:
        sys.exit(1)
    clean_s3(bucket_name, emr_name)


def clean_s3(bucket_name, emr_name):
    s3 = boto3.resource('s3')
    try:
        s3_bucket = s3.Bucket(bucket_name)
        s3_dir = "config/" + emr_name + "/"
        for i in s3_bucket.objects.filter(Prefix=s3_dir):
            s3.Object(s3_bucket.name, i.key).delete()
        print "The bucket " + bucket_name + " has been cleaned successfully"
    except:
        sys.exit(1)