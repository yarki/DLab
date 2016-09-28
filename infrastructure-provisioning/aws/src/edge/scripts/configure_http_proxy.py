#!/usr/bin/python
from fabric.api import *
from fabric.contrib.files import exists
import argparse
import json
import random
import string
import crypt

parser = argparse.ArgumentParser()
parser.add_argument('--hostname', type=str, default='edge')
parser.add_argument('--keyfile', type=str, default='')
parser.add_argument('--additional_config', type=str, default='{"empty":"string"}')
args = parser.parse_args()


def configure_http_proxy_server(config):
    if not exists('/tmp/http_proxy_ensured'):
        sudo('apt-get -y install squid')
        template_file = config['template_file']
        proxy_subnet = config['exploratory_subnet']
        with open("/tmp/tmpsquid.conf", 'w') as out:
            with open(template_file) as tpl:
                for line in tpl:
                    out.write(line.replace('PROXY_SUBNET', proxy_subnet))
        put('/tmp/tmpsquid.conf', '/tmp/squid.conf')
        sudo('\cp /tmp/squid.conf /etc/squid/squid.conf')
        sudo('service squid reload')
        sudo('sysv-rc-conf squid on')
        sudo('touch /tmp/http_proxy_ensured')




##############
# Run script #
##############
if __name__ == "__main__":
    print "Configure connections"
    env['connection_attempts'] = 100
    env.key_filename = [args.keyfile]
    env.host_string = 'ubuntu@' + args.hostname
    deeper_config = json.loads(args.additional_config)

    print "Installing proxy for notebooks."
    configure_http_proxy_server(deeper_config)

