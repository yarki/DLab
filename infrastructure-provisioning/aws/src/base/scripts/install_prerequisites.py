#!/usr/bin/python
from fabric.api import *
import argparse
import json
from dlab.fab import *
import sys


parser = argparse.ArgumentParser()
parser.add_argument('--hostname', type=str, default='edge')
parser.add_argument('--keyfile', type=str, default='')
parser.add_argument('--apt_packages', type=str, default='python-pip python-dev groff vim less git wget sysv-rc-conf')
parser.add_argument('--pip_packages', type=str, default='boto3 boto argparse fabric jupyter awscli')
parser.add_argument('--additional_config', type=str, default='{"empty":"string"}')
args = parser.parse_args()


if __name__ == "__main__":
    print "Configure connections"
    env['connection_attempts'] = 100
    env.key_filename = [args.keyfile]
    env.host_string = 'ubuntu@' + args.hostname
    deeper_config = json.loads(args.additional_config)

    print "Updating repositories and installing requested tools: " + args.apt_packages
    if not ensure_apt(args.apt_packages):
        sys.exit(1)

    print "Installing python packages: " + args.pip_packages
    if not ensure_pip(args.pip_packages):
        sys.exit(1)

    sys.exit(0)

