import boto3


def get_instance_hostname(instance_name):
    ec2 = boto3.resource('ec2')
    instances = ec2.instances.filter(
        Filters=[{'Name': 'tag:Name', 'Values': [instance_name]},
                 {'Name': 'instance-state-name', 'Values': ['running']}])
    for instance in instances:
        public = getattr(instance, 'public_dns_name')
        private = getattr(instance, 'private_dns_name')
        if public:
            return public
        else:
            return private


def get_bucket_by_name(bucket_name):
    s3 = boto3.resource('s3')
    for bucket in s3.buckets.all():
        if bucket.name == bucket_name:
            return bucket.name
    return ''


def get_instance_ip_address(instance_name):
    ec2 = boto3.resource('ec2')
    instances = ec2.instances.filter(
        Filters=[{'Name': 'tag:Name', 'Values': [instance_name]},
                 {'Name': 'instance-state-name', 'Values': ['running']}])
    for instance in instances:
        public = getattr(instance, 'public_ip_address')
        private = getattr(instance, 'private_ip_address')
        if public:
            return public
        else:
            return private


def get_ami_id_by_name(ami_name):
    ec2 = boto3.resource('ec2')
    try:
        for image in ec2.images.filter(Filters=[{'Name': 'name', 'Values': [ami_name]}]):
            return image.id
    except:
        return ''
    return ''


def get_security_group_by_name(security_group_name):
    ec2 = boto3.resource('ec2')
    try:
        for security_group in ec2.security_groups.filter(GroupNames=[security_group_name]):
            return security_group.id
    except:
        return ''
    return ''


def get_instance_attr(instance_id, attribute_name):
    ec2 = boto3.resource('ec2')
    instances = ec2.instances.filter(
        Filters=[{'Name': 'instance-id', 'Values': [instance_id]},
                 {'Name': 'instance-state-name', 'Values': ['running']}])
    for instance in instances:
        return getattr(instance, attribute_name)
    return ''


def get_instance_by_name(instance_name):
    ec2 = boto3.resource('ec2')
    instances = ec2.instances.filter(
        Filters=[{'Name': 'tag:Name', 'Values': [instance_name]},
                 {'Name': 'instance-state-name', 'Values': ['running']}])
    for instance in instances:
        return instance.id
    return ''


def get_role_by_name(role_name):
    iam = boto3.resource('iam')
    for role in iam.roles.all():
        if role.name == role_name:
            return role.name
    return ''


def get_subnet_by_cidr(cidr):
    ec2 = boto3.resource('ec2')
    for subnet in ec2.subnets.filter(Filters=[{'Name': 'cidrBlock', 'Values': [cidr]}]):
        return subnet.id
    return ''


def get_vpc_by_cidr(cidr):
    ec2 = boto3.resource('ec2')
    for vpc in ec2.vpcs.filter(Filters=[{'Name': 'cidr', 'Values': [cidr]}]):
        return vpc.id
    return ''


def get_emr_list(tag_name, type='Key'):
    emr = boto3.client('emr')
    clusters = emr.list_clusters(
        ClusterStates=['RUNNING', 'WAITING', 'STARTING', 'BOOTSTRAPPING']
    )
    clusters = clusters.get('Clusters')
    clusters_list = []
    for i in clusters:
        response = emr.describe_cluster(ClusterId=i.get('Id'))
        tag = response.get('Cluster').get('Tags')
        for j in tag:
            if j.get(type) == tag_name:
                clusters_list.append(i.get('Id'))
    return clusters_list


def get_ec2_list(tag_name):
    ec2 = boto3.resource('ec2')
    notebook_instances = ec2.instances.filter(
        Filters=[{'Name': 'instance-state-name', 'Values': ['running', 'stopped']},
                 {'Name': 'tag:{}'.format(tag_name), 'Values': ['*nb*']}])
    return notebook_instances


def resource_count(resource_type, tag_name):
    if resource_type == 'EC2':
        notebooks = get_ec2_list(tag_name)
        count = 0
        for i in notebooks:
            count += 1
        return count
    elif resource_type == 'EMR':
        count = len(get_emr_list(tag_name))
        return count
    else:
        print "Incorrect resource type!"


def provide_index(resource_type, tag_name):
    ids = []
    if resource_type == 'EMR':
        list = get_emr_list(tag_name)
        emr = boto3.client('emr')
        for i in list:
            response = emr.describe_cluster(ClusterId=i)
            number = response.get('Cluster').get('Name').split('-')[-1]
            if number not in ids:
                ids.append(int(number))
    elif resource_type == 'EC2':
        list = get_ec2_list(tag_name)
        for i in list:
            for tag in i.tags:
                if tag['Key'] == 'Name':
                    ids.append(int(tag['Value'].split('-')[-1]))
    else:
        print "Incorrect resource type!"
    index = 1
    while True:
        if index not in ids:
            break
        else:
            index += 1
    return index
