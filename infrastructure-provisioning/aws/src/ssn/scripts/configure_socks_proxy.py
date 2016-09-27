#!/usr/bin/python
from fabric.api import *
from fabric.contrib.files import exists
import argparse
import json

parser = argparse.ArgumentParser()
parser.add_argument('--hostname', type=str, default='edge')
parser.add_argument('--keyfile', type=str, default='')
parser.add_argument('--additional_config', type=str, default='{"empty":"string"}')
args = parser.parse_args()


def ensure_proxy():
    if not exists('/tmp/socks_proxy_ensured'):
        sudo('apt-get install dante-server')
        sudo('service danted start')
        sudo('sysv-rc-conf danted on')
        sudo('touch /tmp/socks_proxy_ensured')


def configure_proxy():
    if not exists("/lib/systemd/system/mongod.service"):
        local('scp -i {} /usr/share/notebook_automation/templates/mongod.service_template {}:/tmp/mongod.service'.format(args.keyfile, env.host_string))
        sudo('mv /tmp/mongod.service /lib/systemd/system/mongod.service')
    local('scp -i {} /usr/share/notebook_automation/templates/configure_mongo.py {}:/tmp/configure_mongo.py'.format(args.keyfile, env.host_string))
    sudo('python /tmp/configure_mongo.py')


##############
# Run script #
##############
if __name__ == "__main__":
    print "Configure connections"
    env['connection_attempts'] = 100
    env.key_filename = [args.keyfile]
    env.host_string = 'ubuntu@' + args.hostname
    deeper_config = json.loads(args.additional_config)

    print "Installing dante server"
    ensure_proxy()

    print "Configuring dante server"
    configure_proxy()