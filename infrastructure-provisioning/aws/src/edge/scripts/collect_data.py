#!/usr/bin/python
import argparse
import json
from fabric.api import *
from dlab.aws_actions import *
from dlab.aws_meta import *


parser = argparse.ArgumentParser()
parser.add_argument('--service_base_name', type=str, default='')
parser.add_argument('--user_name', type=str, default='')
parser.add_argument('--hostname', type=str, default='')
parser.add_argument('--keyfile', type=str, default='')
args = parser.parse_args()


if __name__ == "__main__":
    data = []
    edge = {}
    edge['environment_tag'] = args.service_base_name

    # Get EDGE id
    edge['id'] = get_instance_by_name('{}-{}-edge'.format(args.service_base_name, args.user_name))

    # Get Notebook List
    notebooks = []
    nbs_list = get_ec2_list(args.service_base_name)
    for i in nbs_list:
        user = {}
        user['Id'] = i.id
        for tag in i.tags:
            if tag['Key'] == 'Name':
                user['Name'] = tag['Value']
        user['Shape'] = i.instance_type
        user['Status'] = i.state['Name']
        emr_list = get_emr_list(user['Name'], 'Value')
        resources = []
        for j in emr_list:
            emr = {}
            emr['id'] = j
            emr['status'] =  get_emr_info(j, 'Status')['State']
            counter = 0
            for instance in get_ec2_list('Notebook', user['Name']):
                counter +=1
                emr['shape'] = instance.instance_type
            emr['nodes_count'] = counter
            emr['type'] =  get_emr_info(j, 'ReleaseLabel')
            resources.append(emr)
        user['computeresources'] = resources
        notebooks.append(user)

    edge['Notebooks'] = notebooks
    data.append(edge)

    filename = '{}_data.json'.format(args.user_name)
    with open('/root/' + filename, 'w') as outfile:
        json.dump(data, outfile)

    env['connection_attempts'] = 100
    env.key_filename = [args.keyfile]
    env.host_string = 'ubuntu@' + args.hostname
    put('/root/' + filename, '/tmp/' + filename, mode=0644)
    sudo('mv /tmp/' + filename + ' /home/ubuntu/' + filename)