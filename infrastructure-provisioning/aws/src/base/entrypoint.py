#!/usr/bin/python
import os
from ConfigParser import SafeConfigParser
import argparse
from fabric.api import *
import json

parser = argparse.ArgumentParser()
parser.add_argument('--action', type=str, default='describe')
args = parser.parse_args()

if __name__ == "__main__":
    for filename in os.listdir('/root/conf'):
        if filename.endswith('.ini'):
            config = SafeConfigParser()
            config.read(os.path.join('/root/conf', filename))
            for section in config.sections():
                for option in config.options(section):
                    varname = "%s_%s" % (section, option)
                    if varname not in os.environ:
                        os.environ[varname] = config.get(section, option)

    # Enforcing overwrite
    for filename in os.listdir('/root/conf'):
        if filename.endswith('overwrite.ini'):
            config = SafeConfigParser()
            config.read(os.path.join('/root/conf', filename))
            for section in config.sections():
                for option in config.options(section):
                    varname = "%s_%s" % (section, option)
                    os.environ[varname] = config.get(section, option)

    request_id = 'generic'
    try:
        request_id = os.environ['request_id']
    except:
        os.environ['request_id'] = 'generic'

    dry_run = False
    try:
        if os.environ['dry_run'] == 'true':
            dry_run = True
    except:
        pass

    if dry_run:
        with open("/response/%s.json" % request_id, 'w') as response_file:
            response = {"request_id": request_id, "action": args.action, "dry_run": "true"}
            response_file.write(json.dumps(response))

    elif args.action == 'create':
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
