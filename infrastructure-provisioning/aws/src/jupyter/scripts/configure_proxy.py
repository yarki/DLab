#!/usr/bin/python
from fabric.api import *
from fabric.contrib.files import exists
import argparse
import json
from notebook.auth import passwd as jupyter_passwd
import random
import string

parser = argparse.ArgumentParser()
parser.add_argument('--hostname', type=str, default='')
parser.add_argument('--instance_name', type=str, default='')
parser.add_argument('--keyfile', type=str, default='')
parser.add_argument('--additional_config', type=str, default='{"empty":"string"}')
args = parser.parse_args()


def enable_proxy(proxy_host, proxy_port):
    if not exists('/tmp/proxy_enabled'):
        proxy_string = "http://%s:%s" % (proxy_host, proxy_port)
        sudo('echo export http_proxy=' + proxy_string + ' >> /etc/profile')
        sudo('echo export https_proxy=' + proxy_string + ' >> /etc/profile')
        sudo("echo 'Acquire::http::Proxy \"" + proxy_string + "\";' >> /etc/apt/apt.conf")
        sudo('touch /tmp/proxy_enabled ')


##############
# Run script #
##############
if __name__ == "__main__":
    print "Configure connections"
    env['connection_attempts'] = 100
    env.key_filename = [args.keyfile]
    env.host_string = 'ubuntu@' + args.hostname
    deeper_config = json.loads(args.additional_config)

    print "Enabling proxy for notebook server for repositories access."
    enable_proxy(deeper_config['proxy_host'], deeper_config['proxy_port'])
