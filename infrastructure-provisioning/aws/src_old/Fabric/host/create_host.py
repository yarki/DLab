#!/usr/bin/python
import os
import logging
import time
import boto3
import ConfigParser
import paramiko
from fabric.api import *
from fabric.operations import put
from time import strftime, gmtime

def check_ssh_readyness(host_name, user_name, key_file):
    paramiko.util.log_to_file('paramiko.log')
    ECHO_TEXT = 'TEST'
    retvalue = False

    with paramiko.SSHClient() as ssh:
        ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
        try:
            ssh.connect(host_name, username=user_name, key_filename=key_file, look_for_keys=False, allow_agent=False)
            stdin, stdout, stderr = ssh.exec_command('echo "' + ECHO_TEXT + '"')
            response = stdout.readlines()
            if (not response == []) and (str(response[0]).rstrip() == ECHO_TEXT):
                retvalue = True
        except Exception as e:
            print(e)
    ssh.close()
    return retvalue

@task
def create_instance(params):
    logging.info("calling ../../scripts/create_instances.py " + params)
    shell_out = local("../../scripts/create_instances.py " + params, capture=True)
    print shell_out
    logging.info(shell_out)
    print shell_out.stderr
    logging.error(shell_out.stderr)
    
@task
def copy_keys(priv_key, pub_key, instance_name, username, keyfile):
    logging.info("Copying SSH keys to instance...")
    REMOTE_DESTINATION_DIR = '/home/ubuntu/.ssh'
    
    ec2 = boto3.resource('ec2')
    instances = ec2.instances.filter(
        Filters=[{'Name': 'tag:Name', 'Values': [instance_name]},
                 {'Name': 'instance-state-name', 'Values': ['running']}])
    instance = ''
    for ins in instances:
        instance = ins
    if instance == '':
        logging.error("'%s' instance not found", instance_name)
        return
    
    instancePublicDNSName = getattr(instance, 'public_dns_name')
    while (not check_ssh_readyness(instancePublicDNSName, username, keyfile)):
        logging.info("Waiting for SSH in instance...")
        time.sleep(30)
        
    env.host_string = username + '@' + instancePublicDNSName
    #env.host = instancePublicDNSName
    #env.user = 'ubuntu'
    #env.use_ssh_config = True
    put(priv_key, REMOTE_DESTINATION_DIR, mode=0600)
    put(pub_key, REMOTE_DESTINATION_DIR, mode=0600)
    logging.info("Copying SSH keys to instance...done")

@task
def run():
    local_log_filename = "run-%s.log" % str(strftime("%Y-%m-%d-%H-%M-%S", gmtime()))
    local_log_filepath = "%s/%s" % (os.environ['PWD'], local_log_filename)
    logging.basicConfig(format='%(levelname)-8s [%(asctime)s]  %(message)s',
                        level=logging.DEBUG,
                        filename=local_log_filepath)

    config = ConfigParser.SafeConfigParser()
    config.read('instance.ini')
        
    logging.info('[CREATE INSTANCE]')
    params = "--node_name %s --ami_id %s --instance_type %s --key_name %s --security_group_id %s " \
             "--subnet_id %s --user_data_file %s --dry_run false " % \
             (config.get('instance', 'instance_name'),
              config.get('instance', 'ami_id'),
              config.get('instance', 'instance_type'),
              config.get('instance', 'key_name'),
              config.get('instance', 'security_groups_ids'),
              config.get('instance', 'subnet_id'),
              config.get('instance', 'cloud_config'))

    create_instance(params)
    
    logging.info('[COPYING KEYPAIR TO INSTANCE]')
    copy_keys(config.get('git-keys', 'priv'),
              config.get('git-keys', 'pub'),
              config.get('instance', 'instance_name'),
              config.get('instance', 'user_name'),
              config.get('instance', 'key_file'))
