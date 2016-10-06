#!/usr/bin/python
from fabric.api import *
from fabric.contrib.files import exists
import argparse
import json
import random
import string
import crypt
import sys

parser = argparse.ArgumentParser()
parser.add_argument('--hostname', type=str, default='edge')
parser.add_argument('--keyfile', type=str, default='')
parser.add_argument('--additional_config', type=str, default='{"empty":"string"}')
args = parser.parse_args()


def ensure_mongo():
    try:
        if not exists('/tmp/mongo_ensured'):
            sudo('apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv EA312927')
            sudo('ver=`lsb_release -cs`; echo "deb http://repo.mongodb.org/apt/ubuntu $ver/mongodb-org/3.2 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-3.2.list; apt-get update')
            sudo('apt-get -y install mongodb-org')
            sudo('sysv-rc-conf mongod on')
            sudo('touch /tmp/mongo_ensured')
        return True
    except:
        return False


def configure_mongo():
    try:
        if not exists("/lib/systemd/system/mongod.service"):
            local('scp -i {} /root/templates/mongod.service_template {}:/tmp/mongod.service'.format(args.keyfile, env.host_string))
            sudo('mv /tmp/mongod.service /lib/systemd/system/mongod.service')
        local('scp -i {} /root/scripts/configure_mongo.py {}:/tmp/configure_mongo.py'.format(args.keyfile, env.host_string))
        sudo('python /tmp/configure_mongo.py')
        return True
    except:
        return False


def start_ss():
    try:
        if not exists('/tmp/ss_started'):
            local('scp -i {} /root/application.yml {}:/tmp/application.yml'.format(args.keyfile, env.host_string))
            local('scp -i {} /root/self-service-1.0.jar {}:/tmp/self-service-1.0.jar'.format(args.keyfile, env.host_string))
            sudo('nohup java -jar /tmp/self-service-1.0.jar server /tmp/application.yml > /dev/null 2>&1 &')
            sudo('touch /tmp/ss_started')
        return True
    except:
        return False

##############
# Run script #
##############
if __name__ == "__main__":
    print "Configure connections"
    try:
        env['connection_attempts'] = 100
        env.key_filename = [args.keyfile]
        env.host_string = 'ubuntu@' + args.hostname
        deeper_config = json.loads(args.additional_config)
    except:
        sys.exit(2)

    print "Installing MongoDB"
    if not ensure_mongo():
        sys.exit(1)

    print "Configuring MongoDB"
    if not configure_mongo():
        sys.exit(1)

    print "Starting Self-Service(UI)"
    if not start_ss():
        sys.exit(1)

    sys.exit(0)
