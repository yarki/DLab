# *****************************************************************************
#
# Copyright (c) 2016, EPAM SYSTEMS INC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# ******************************************************************************

import boto3
import botocore
import time
import sys
import os
import json
from fabric.api import *
import logging
from dlab.aws_meta import *
import traceback

local_log_filename = "%s.log" % os.environ['request_id']
local_log_filepath = "/response/" + local_log_filename
logging.basicConfig(format='%(levelname)-8s [%(asctime)s]  %(message)s',
                    level=logging.DEBUG,
                    filename=local_log_filepath)


def put_to_bucket(bucket_name, local_file, destination_file):
    try:
        s3 = boto3.client('s3')
        with open(local_file, 'rb') as data:
            s3.upload_fileobj(data, bucket_name, destination_file)
        return True
    except Exception as err:
        logging.info("Unable to upload files to S3 bucket: " + str(err) + "\n Traceback: " + traceback.print_exc(file=sys.stdout))
        with open("/root/result.json", 'w') as result:
            res = {"error": "Unable to upload files to S3 bucket", "error_message": str(err) + "\n Traceback: " + traceback.print_exc(file=sys.stdout)}
            print json.dumps(res)
            result.write(json.dumps(res))
        traceback.print_exc(file=sys.stdout)
        return False


def create_s3_bucket(bucket_name, tag, region):
    try:
        s3 = boto3.resource('s3')
        bucket = s3.create_bucket(Bucket=bucket_name,
                                  CreateBucketConfiguration={'LocationConstraint': region})
        tagging = bucket.Tagging()
        tagging.put(Tagging={'TagSet': [tag]})
        tagging.reload()
        return bucket.name
    except Exception as err:
        logging.info("Unable to create S3 bucket: " + str(err) + "\n Traceback: " + traceback.print_exc(file=sys.stdout))
        with open("/root/result.json", 'w') as result:
            res = {"error": "Unable to create S3 bucket", "error_message": str(err) + "\n Traceback: " + traceback.print_exc(file=sys.stdout)}
            print json.dumps(res)
            result.write(json.dumps(res))
        traceback.print_exc(file=sys.stdout)


def create_vpc(vpc_cidr, tag):
    try:
        ec2 = boto3.resource('ec2')
        vpc = ec2.create_vpc(CidrBlock=vpc_cidr)
        vpc.create_tags(Tags=[tag])
        return vpc.id
    except Exception as err:
        logging.info("Unable to create VPC: " + str(err) + "\n Traceback: " + traceback.print_exc(file=sys.stdout))
        with open("/root/result.json", 'w') as result:
            res = {"error": "Unable to create VPC", "error_message": str(err) + "\n Traceback: " + traceback.print_exc(file=sys.stdout)}
            print json.dumps(res)
            result.write(json.dumps(res))
        traceback.print_exc(file=sys.stdout)


def create_tag(resource, tag):
    try:
        ec2 = boto3.client('ec2')
        ec2.create_tags(
            Resources = resource,
            Tags = [
                json.loads(tag)
            ]
        )
    except Exception as err:
        logging.info("Unable to create Tag: " + str(err) + "\n Traceback: " + traceback.print_exc(file=sys.stdout))
        with open("/root/result.json", 'w') as result:
            res = {"error": "Unable to create Tag", "error_message": str(err) + "\n Traceback: " + traceback.print_exc(file=sys.stdout)}
            print json.dumps(res)
            result.write(json.dumps(res))
        traceback.print_exc(file=sys.stdout)


def create_subnet(vpc_id, subnet, tag):
    try:
        ec2 = boto3.resource('ec2')
        subnet = ec2.create_subnet(VpcId=vpc_id, CidrBlock=subnet)
        subnet.create_tags(Tags=[tag])
        subnet.reload()
        return subnet.id
    except Exception as err:
        logging.info("Unable to create Subnet: " + str(err) + "\n Traceback: " + traceback.print_exc(file=sys.stdout))
        with open("/root/result.json", 'w') as result:
            res = {"error": "Unable to create Subnet", "error_message": str(err) + "\n Traceback: " + traceback.print_exc(file=sys.stdout)}
            print json.dumps(res)
            result.write(json.dumps(res))
        traceback.print_exc(file=sys.stdout)


def create_instance(definitions, instance_tag):
    try:
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
        if definitions.instance_class == 'notebook':
            instances = ec2.create_instances(ImageId=definitions.ami_id, MinCount=1, MaxCount=1,
                                             BlockDeviceMappings=[
                                                 {
                                                     "DeviceName": "/dev/sdb",
                                                     "Ebs":
                                                         {
                                                             "VolumeSize": int(definitions.instance_disk_size)
                                                         }
                                                 }],
                                             KeyName=definitions.key_name,
                                             SecurityGroupIds=security_groups_ids,
                                             InstanceType=definitions.instance_type,
                                             SubnetId=definitions.subnet_id,
                                             IamInstanceProfile={'Name': definitions.iam_profile},
                                             UserData=user_data)
        else:
            get_iam_profile(definitions.iam_profile)
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
    except Exception as err:
        logging.info("Unable to create EC2: " + str(err) + "\n Traceback: " + traceback.print_exc(file=sys.stdout))
        with open("/root/result.json", 'w') as result:
            res = {"error": "Unable to create EC2", "error_message": str(err) + "\n Traceback: " + traceback.print_exc(file=sys.stdout)}
            print json.dumps(res)
            result.write(json.dumps(res))
        traceback.print_exc(file=sys.stdout)


def create_iam_role(role_name, role_profile):
    conn = boto3.client('iam')
    try:
        conn.create_role(RoleName=role_name, AssumeRolePolicyDocument='{"Version":"2012-10-17","Statement":[{"Effect":"Allow","Principal":{"Service":["ec2.amazonaws.com"]},"Action":["sts:AssumeRole"]}]}')
        conn.create_instance_profile(InstanceProfileName=role_profile)
    except botocore.exceptions.ClientError as e_role:
        if e_role.response['Error']['Code'] == 'EntityAlreadyExists':
            print "Instance profile already exists. Reusing..."
        else:
            logging.info("Unable to create Instance Profile: " + str(e_role.response['Error']['Message']) + "\n Traceback: " + traceback.print_exc(file=sys.stdout))
            with open("/root/result.json", 'w') as result:
                res = {"error": "Unable to create Instance Profile", "error_message": str(e_role.response['Error']['Message']) + "\n Traceback: " + traceback.print_exc(file=sys.stdout)}
                print json.dumps(res)
                result.write(json.dumps(res))
            traceback.print_exc(file=sys.stdout)
            return
    try:
        conn.add_role_to_instance_profile(InstanceProfileName=role_profile, RoleName=role_name)
        time.sleep(30)
    except botocore.exceptions.ClientError as err:
        logging.info("Unable to create IAM role: " + str(err.response['Error']['Message']) + "\n Traceback: " + traceback.print_exc(file=sys.stdout))
        with open("/root/result.json", 'w') as result:
            res = {"error": "Unable to create IAM role", "error_message": str(err.response['Error']['Message']) + "\n Traceback: " + traceback.print_exc(file=sys.stdout)}
            print json.dumps(res)
            result.write(json.dumps(res))
        traceback.print_exc(file=sys.stdout)


def attach_policy(policy_arn, role_name):
    try:
        conn = boto3.client('iam')
        conn.attach_role_policy(PolicyArn=policy_arn, RoleName=role_name)
        time.sleep(30)
    except botocore.exceptions.ClientError as err:
        logging.info("Unable to attach Policy: " + str(err.response['Error']['Message']) + "\n Traceback: " + traceback.print_exc(file=sys.stdout))
        with open("/root/result.json", 'w') as result:
            res = {"error": "Unable to attach Policy", "error_message": str(err.response['Error']['Message']) + "\n Traceback: " + traceback.print_exc(file=sys.stdout)}
            print json.dumps(res)
            result.write(json.dumps(res))
        traceback.print_exc(file=sys.stdout)


def create_attach_policy(policy_name, role_name, file_path):
    try:
        conn = boto3.client('iam')
        with open(file_path, 'r') as myfile:
            json_file = myfile.read()
        conn.put_role_policy(RoleName=role_name, PolicyName=policy_name, PolicyDocument=json_file)
    except Exception as err:
        logging.info("Unable to attach Policy: " + str(err) + "\n Traceback: " + traceback.print_exc(file=sys.stdout))
        with open("/root/result.json", 'w') as result:
            res = {"error": "Unable to attach Policy", "error_message": str(err) + "\n Traceback: " + traceback.print_exc(file=sys.stdout)}
            print json.dumps(res)
            result.write(json.dumps(res))
        traceback.print_exc(file=sys.stdout)


def remove_ec2(tag_name, tag_value):
    try:
        ec2 = boto3.resource('ec2')
        client = boto3.client('ec2')
        inst = ec2.instances.filter(
            Filters=[{'Name': 'instance-state-name', 'Values': ['running', 'stopped', 'pending', 'stopping']},
                     {'Name': 'tag:{}'.format(tag_name), 'Values': ['{}'.format(tag_value)]}])
        instances = list(inst)
        if instances:
            for instance in instances:
                client.terminate_instances(InstanceIds=[instance.id])
                waiter = client.get_waiter('instance_terminated')
                waiter.wait(InstanceIds=[instance.id])
                print "The instance " + instance.id + " has been terminated successfully"
        else:
            print "There are no instances with " + tag_value + " name to terminate"
    except Exception as err:
        logging.info("Unable to remove EC2: " + str(err) + "\n Traceback: " + traceback.print_exc(file=sys.stdout))
        with open("/root/result.json", 'w') as result:
            res = {"error": "Unable to EC2", "error_message": str(err) + "\n Traceback: " + traceback.print_exc(file=sys.stdout)}
            print json.dumps(res)
            result.write(json.dumps(res))
        traceback.print_exc(file=sys.stdout)


def stop_ec2(tag_name, tag_value):
    try:
        ec2 = boto3.resource('ec2')
        client = boto3.client('ec2')
        inst = ec2.instances.filter(
            Filters=[{'Name': 'instance-state-name', 'Values': ['running', 'pending']},
                     {'Name': 'tag:{}'.format(tag_name), 'Values': ['{}'.format(tag_value)]}])
        instances = list(inst)
        if instances:
            for instance in instances:
                client.stop_instances(InstanceIds=[instance.id])
                waiter = client.get_waiter('instance_stopped')
                waiter.wait(InstanceIds=[instance.id])
                print "The instance " + tag_value + " has been stopped successfully"
        else:
            print "There are no instances with " + tag_value + " name to stop"
    except Exception as err:
        logging.info("Unable to stop EC2: " + str(err) + "\n Traceback: " + traceback.print_exc(file=sys.stdout))
        with open("/root/result.json", 'w') as result:
            res = {"error": "Unable to stop EC2", "error_message": str(err) + "\n Traceback: " + traceback.print_exc(file=sys.stdout)}
            print json.dumps(res)
            result.write(json.dumps(res))
        traceback.print_exc(file=sys.stdout)


def start_ec2(tag_name, tag_value):
    try:
        ec2 = boto3.resource('ec2')
        client = boto3.client('ec2')
        inst = ec2.instances.filter(
            Filters=[{'Name': 'instance-state-name', 'Values': ['stopped']},
                     {'Name': 'tag:{}'.format(tag_name), 'Values': ['{}'.format(tag_value)]}])
        instances = list(inst)
        if instances:
            for instance in instances:
                client.start_instances(InstanceIds=[instance.id])
                waiter = client.get_waiter('instance_status_ok')
                waiter.wait(InstanceIds=[instance.id])
                print "The instance " + tag_value + " has been started successfully"
        else:
            print "There are no instances with " + tag_value + " name to start"
    except Exception as err:
        logging.info("Unable to start EC2: " + str(err) + "\n Traceback: " + traceback.print_exc(file=sys.stdout))
        with open("/root/result.json", 'w') as result:
            res = {"error": "Unable to start EC2", "error_message": str(err) + "\n Traceback: " + traceback.print_exc(file=sys.stdout)}
            print json.dumps(res)
            result.write(json.dumps(res))
        traceback.print_exc(file=sys.stdout)


def remove_role(instance_type='all', scientist=''):
    try:
        print "[Removing roles and instance profiles]"
        client = boto3.client('iam')
        roles_list = []
        if instance_type == "ssn":
            role_name = os.environ['conf_service_base_name'] + '-ssn-Role'
            role_profile_name = os.environ['conf_service_base_name'] + '-ssn-Profile'
            policy_name = os.environ['conf_service_base_name'] + '-ssn-Policy'
        elif instance_type == "edge":
            role_name = os.environ['conf_service_base_name'] + '-' + '{}'.format(scientist) + '-edge-Role'
            role_profile_name = os.environ['conf_service_base_name'] + '-' + '{}'.format(scientist) + '-edge-Profile'
        elif instance_type == "notebook":
            role_name = os.environ['conf_service_base_name'] + '-' + "{}".format(scientist) + '-nb-Role'
            role_profile_name = os.environ['conf_service_base_name'] + '-' + "{}".format(scientist) + '-nb-Profile'
        else:
            role_name = os.environ['conf_service_base_name']
            role_profile_name = os.environ['conf_service_base_name']
            policy_name = os.environ['conf_service_base_name']
        for item in client.list_roles().get("Role"):
            if role_name in item.get("RoleName"):
                roles_list.append(item.get('Name'))
        for iam_role in roles_list:
            if instance_type == "ssn":
                client.delete_role_policy(RoleName=iam_role, PolicyName=policy_name)
            if instance_type == "edge":
                policy_list = client.list_attached_role_policies(RoleName=iam_role).get('AttachedPolicies')
                for i in policy_list:
                    policy_arn = i.get('PolicyArn')
                    client.detach_role_policy(RoleName=iam_role, PolicyArn=policy_arn)
                    client.delete_policy(PolicyArn=policy_arn)
            elif instance_type == "notebook":
                policy_list = client.list_attached_role_policies(RoleName=iam_role).get('AttachedPolicies')
                for i in policy_list:
                    policy_arn = i.get('PolicyArn')
                    client.detach_role_policy(RoleName=iam_role, PolicyArn=policy_arn)
            client.remove_role_from_instance_profile(InstanceProfileName=role_profile_name, RoleName=iam_role)
            client.delete_instance_profile(InstanceProfileName=role_profile_name)
            client.delete_role(RoleName=iam_role)
            print "The IAM role " + iam_role + " has been deleted successfully"
    except Exception as err:
        logging.info("Unable to remove IAM role/profile/policy: " + str(err) + "\n Traceback: " + traceback.print_exc(file=sys.stdout))
        with open("/root/result.json", 'w') as result:
            res = {"error": "Unable to remove IAM role/profile/policy", "error_message": str(err) + "\n Traceback: " + traceback.print_exc(file=sys.stdout)}
            print json.dumps(res)
            result.write(json.dumps(res))
        traceback.print_exc(file=sys.stdout)


def s3_cleanup(bucket, cluster_name, user_name):
    try:
        s3_res = boto3.resource('s3')
        resource = s3_res.Bucket(bucket)
        prefix = user_name + '/' + cluster_name + "/"
        for i in resource.objects.filter(Prefix=prefix):
            s3_res.Object(resource.name, i.key).delete()
    except Exception as err:
        logging.info("Unable to clean S3 bucket: " + str(err) + "\n Traceback: " + traceback.print_exc(file=sys.stdout))
        with open("/root/result.json", 'w') as result:
            res = {"error": "Unable to clean S3 bucket", "error_message": str(err) + "\n Traceback: " + traceback.print_exc(file=sys.stdout)}
            print json.dumps(res)
            result.write(json.dumps(res))
        traceback.print_exc(file=sys.stdout)


def remove_s3(bucket_type='all', scientist=''):
    try:
        print "[Removing S3 buckets]"
        client = boto3.client('s3')
        bucket_list = []
        if bucket_type == 'ssn':
            bucket_name = (os.environ['conf_service_base_name'] + '-ssn-bucket').lower().replace('_', '-')
        elif bucket_type == 'edge':
            bucket_name = (os.environ['conf_service_base_name'] + '-' + "{}".format(scientist) + '-bucket').lower().replace('_', '-')
        else:
            bucket_name = (os.environ['conf_service_base_name']).lower().replace('_', '-')
        for item in client.list_buckets().get('Buckets'):
            if bucket_name in item.get('Name'):
                bucket_list.append(item.get('Name'))
        for s3bucket in bucket_list:
            list_obj = client.list_objects(Bucket=s3bucket)
            list_obj = list_obj.get('Contents')
            if list_obj is not None:
                for o in list_obj:
                    list_obj = o.get('Key')
                    print list_obj
                    client.delete_objects(
                        Bucket=s3bucket,
                        Delete={'Objects': [{'Key': list_obj}]}
                    )
                print "The S3 bucket " + s3bucket + " has been cleaned"
            client.delete_bucket(Bucket=s3bucket)
            print "The S3 bucket " + s3bucket + " has been deleted successfully"
        print "There are no more buckets to delete"
    except Exception as err:
        logging.info("Unable to remove S3 bucket: " + str(err) + "\n Traceback: " + traceback.print_exc(file=sys.stdout))
        with open("/root/result.json", 'w') as result:
            res = {"error": "Unable to remove S3 bucket", "error_message": str(err) + "\n Traceback: " + traceback.print_exc(file=sys.stdout)}
            print json.dumps(res)
            result.write(json.dumps(res))
        traceback.print_exc(file=sys.stdout)


def remove_subnets(tag_value):
    try:
        print "[Removing subnets]"
        ec2 = boto3.resource('ec2')
        client = boto3.client('ec2')
        tag_name = os.environ['conf_service_base_name'] + '-Tag'
        subnets = ec2.subnets.filter(
            Filters=[{'Name': 'tag:{}'.format(tag_name), 'Values': [tag_value]}])
        for subnet in subnets:
            print subnet.id
            client.delete_subnet(SubnetId=subnet.id)
            print "The subnet " + subnet.id + " has been deleted successfully"
    except Exception as err:
        logging.info("Unable to remove subnet: " + str(err) + "\n Traceback: " + traceback.print_exc(file=sys.stdout))
        with open("/root/result.json", 'w') as result:
            res = {"error": "Unable to remove subnet", "error_message": str(err) + "\n Traceback: " + traceback.print_exc(file=sys.stdout)}
            print json.dumps(res)
            result.write(json.dumps(res))
        traceback.print_exc(file=sys.stdout)


def remove_sgroups(tag_value):
    try:
        print "[Removing security groups]"
        ec2 = boto3.resource('ec2')
        client = boto3.client('ec2')
        tag_name = os.environ['conf_service_base_name']
        sgs = ec2.security_groups.filter(
            Filters=[{'Name': 'tag:{}'.format(tag_name), 'Values': [tag_value]}])
        for sg in sgs:
            print sg.id
            client.delete_security_group(GroupId=sg.id)
            print "The security group " + sg.id + " has been deleted successfully"
    except Exception as err:
        logging.info("Unable to remove SG: " + str(err) + "\n Traceback: " + traceback.print_exc(file=sys.stdout))
        with open("/root/result.json", 'w') as result:
            res = {"error": "Unable to remove SG", "error_message": str(err) + "\n Traceback: " + traceback.print_exc(file=sys.stdout)}
            print json.dumps(res)
            result.write(json.dumps(res))
        traceback.print_exc(file=sys.stdout)


def deregister_image(scientist):
    try:
        print "[De-registering images]"
        client = boto3.client('ec2')
        response = client.describe_images(
            Filters=[{'Name': 'name', 'Values': ['{}-{}-notebook-image'.format(os.environ['conf_service_base_name'], scientist)]}])
        images_list = response.get('Images')
        for i in images_list:
            client.deregister_image(ImageId=i.get('ImageId'))
            print "Notebook AMI " + i + " has been deregistered successfully"
    except Exception as err:
        logging.info("Unable to de-register image: " + str(err) + "\n Traceback: " + traceback.print_exc(file=sys.stdout))
        with open("/root/result.json", 'w') as result:
            res = {"error": "Unable to de-register image", "error_message": str(err) + "\n Traceback: " + traceback.print_exc(file=sys.stdout)}
            print json.dumps(res)
            result.write(json.dumps(res))
        traceback.print_exc(file=sys.stdout)


def terminate_emr(id):
    try:
        emr = boto3.client('emr')
        emr.terminate_job_flows(
            JobFlowIds=[id]
        )
        waiter = emr.get_waiter('cluster_terminated')
        waiter.wait(ClusterId=id)
    except Exception as err:
        logging.info("Unable to remove EMR: " + str(err) + "\n Traceback: " + traceback.print_exc(file=sys.stdout))
        with open("/root/result.json", 'w') as result:
            res = {"error": "Unable to remove EMR", "error_message": str(err) + "\n Traceback: " + traceback.print_exc(file=sys.stdout)}
            print json.dumps(res)
            result.write(json.dumps(res))
        traceback.print_exc(file=sys.stdout)


def remove_kernels(emr_name, tag_name, nb_tag_value, ssh_user, key_path, emr_version):
    try:
        ec2 = boto3.resource('ec2')
        inst = ec2.instances.filter(
            Filters=[{'Name': 'instance-state-name', 'Values': ['running']},
                     {'Name': 'tag:{}'.format(tag_name), 'Values': ['{}'.format(nb_tag_value)]}])
        instances = list(inst)
        if instances:
            for instance in instances:
                private = getattr(instance, 'private_dns_name')
                env.hosts = "{}".format(private)
                env.user = "{}".format(ssh_user)
                env.key_filename = "{}".format(key_path)
                env.host_string = env.user + "@" + env.hosts
                sudo('rm -rf  /opt/' + emr_version + '/' + emr_name + '/')
                sudo('rm -rf /home/{}/.local/share/jupyter/kernels/*_{}'.format(ssh_user, emr_name))
                print "Notebook's " + env.hosts + " kernels were removed"
        else:
            print "There are no notebooks to clean kernels."
    except Exception as err:
        logging.info("Unable to remove kernels on Notebook: " + str(err) + "\n Traceback: " + traceback.print_exc(file=sys.stdout))
        with open("/root/result.json", 'w') as result:
            res = {"error": "Unable to remove kernels on Notebook", "error_message": str(err) + "\n Traceback: " + traceback.print_exc(file=sys.stdout)}
            print json.dumps(res)
            result.write(json.dumps(res))
        traceback.print_exc(file=sys.stdout)


def remove_route_tables(tag_name):
    try:
        client = boto3.client('ec2')
        rtables = client.describe_route_tables(Filters=[{'Name': 'tag-key', 'Values': tag_name}])
        for rtable in rtables:
            rtable.get('RouteTables').get('RouteTableId')
            client.delete_route_table(RouteTableId=rtable)
            print "Route table " + rtable + " was removed"
    except Exception as err:
        logging.info("Unable to remove route table: " + str(err) + "\n Traceback: " + traceback.print_exc(
            file=sys.stdout))
        with open("/root/result.json", 'w') as result:
            res = {"error": "Unable to remove route table",
                   "error_message": str(err) + "\n Traceback: " + traceback.print_exc(file=sys.stdout)}
            print json.dumps(res)
            result.write(json.dumps(res))
        traceback.print_exc(file=sys.stdout)
