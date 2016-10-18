#!/usr/bin/python

from fabric.api import *
import argparse
import json
import sys

parser = argparse.ArgumentParser()
parser.add_argument('--hostname', type=str, default='edge')
parser.add_argument('--keyfile', type=str, default='')
parser.add_argument('--additional_config', type=str, default='{"empty":"string"}')
args = parser.parse_args()


def copy_key(config):
    key = open('{}/{}.pub'.format(config['user_keydir'], config['user_keyname'])).read()
    if sudo('echo "{}" >> /home/ubuntu/.ssh/authorized_keys'.format(key)).succeeded:
        return True
    else:
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

    print "Installing users key..."
    if copy_key(deeper_config):
        sys.exit(0)
    else:
        print "Users keyfile {0}.pub could not be found at {1}/{0}".format(args.keyfile, deeper_config['user_keydir'])
        sys.exit(1)