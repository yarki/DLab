#!/usr/bin/python
import os
import argparse
from fabric.api import local, hide
import json
from ConfigParser import SafeConfigParser

parser = argparse.ArgumentParser()
parser.add_argument('--action', type=str, default='describe')
args = parser.parse_args()


def create_shadow_config():
    sections = ['creds', 'conf', 'ssn', 'notebook']
    shadow_overwrite_config = SafeConfigParser()
    shadow_config_file = os.environ['PROVISION_CONFIG_DIR'] + 'shadow_overwrite.ini'
    for key in os.environ:
        transitional_key = key.lower()
        for section in sections:
            if transitional_key.startswith(section):
                if not shadow_overwrite_config.has_section(section):
                    shadow_overwrite_config.add_section(section)
                shadow_overwrite_config.set(section, transitional_key[len(section) + 1:], os.environ[key])

                print transitional_key[len(section) + 1:] + ' : ' + os.environ[key]
    with open(shadow_config_file, 'w') as shadow_config:
        shadow_overwrite_config.write(shadow_config)


if __name__ == "__main__":
    create_shadow_config()

    request_id = 'generic'
    try:
        request_id = os.environ['request_id']
    except:
        pass

    if args.action == 'create':
        with hide('running'):
            local("/bin/create.py")

    elif args.action == 'status':
        with hide('running'):
            local("/bin/status.py")

    elif args.action == 'describe':
        with open('/root/description.json') as json_file:
            description = json.load(json_file)
            description['request_id'] = request_id
            with open("/response/%s.json" % request_id, 'w') as response_file:
                response_file.write(json.dumps(description))

    elif args.action == 'stop':
        with hide('running'):
            local("/bin/stop.py")

    elif args.action == 'start':
        with hide('running'):
            local("/bin/start.py")

    elif args.action == 'terminate':
        with hide('running'):
            local("/bin/terminate.py")
