#!/usr/bin/python
from fabric.api import *
from fabric.contrib.files import exists
import argparse
import json

parser = argparse.ArgumentParser()
parser.add_argument('--hostname', type=str, default='edge')
parser.add_argument('--keyfile', type=str, default='')
parser.add_argument('--apt_packages', type=str, default='python-pip python-dev groff vim less git wget sysv-rc-conf')
parser.add_argument('--pip_packages', type=str, default='boto3 boto argparse fabric jupyter awscli')
parser.add_argument('--additional_config', type=str, default='{"empty":"string"}')
args = parser.parse_args()


def ensure_apt(requisites):
    if not exists('/tmp/apt_upgraded'):
        sudo('apt-get update')
        sudo('apt-get -y upgrade')
        sudo('touch /tmp/apt_upgraded')
    sudo('apt-get -y install ' + requisites)


def ensure_pip(requisites):
    if not exists('/tmp/pip_path_added'):
        sudo('echo PATH=$PATH:/usr/local/bin/:/opt/spark/bin/ >> /etc/profile')
        sudo('echo export PATH >> /etc/profile')
        sudo('touch /tmp/pip_path_added')
    sudo('pip install -U ' + requisites)



##############
# Run script #
##############
if __name__ == "__main__":
    print "Configure connections"
    env['connection_attempts'] = 100
    env.key_filename = [args.keyfile]
    env.host_string = 'ubuntu@' + args.hostname
    deeper_config = json.loads(args.additional_config)

    print "Updating repositories and installing requested tools: " + args.apt_packages
    ensure_apt(args.apt_packages)

    print "Installing python packages: " + args.pip_packages
    ensure_pip(args.pip_packages)
