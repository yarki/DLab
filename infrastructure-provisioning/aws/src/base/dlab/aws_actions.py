import boto3, boto
import time


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