#!/usr/bin/python

from fabric.api import *
import argparse
import json
import os

parser = argparse.ArgumentParser()
parser.add_argument('--hostname', type=str, default='edge')
parser.add_argument('--keyfile', type=str, default='')
parser.add_argument('--additional_config', type=str, default='{"empty":"string"}')
args = parser.parse_args()


def copy_key(config):
    try:
        if not exists('/tmp/userkey_installed'):
            key = open('{}/{}.pub'.format(config['user_keydir'], config['user_keyname'])).read()
            sudo('echo "{}" >> /home/ubuntu/.ssh/authorized_keys'.format(key))
    except:
        return False
    return True


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
        sys.exit(1)