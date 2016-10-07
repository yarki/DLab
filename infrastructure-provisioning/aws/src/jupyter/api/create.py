#!/usr/bin/python
import os
import json
import sys
from fabric.api import local


if __name__ == "__main__":
    success = True
    try:
        local('cd /root; fab run')
    except:
        success = False

    reply = dict()
    reply['request_id'] = os.environ['request_id']
    if success:
        reply['status'] = 'ok'
    else:
        reply['status'] = 'err'

    reply['response'] = dict()

    try:
        with open("/root/result.json") as f:
            reply['response']['result'] = json.loads(f.read())
    except:
        reply['response']['result'] = {"error": "Failed to open result itself. Bad sign."}

    reply['response']['log'] = "/response/%s.log" % os.environ['request_id']

    with open("/response/%s.json" % os.environ['request_id'], 'w') as response_file:
        response_file.write(json.dumps(reply))

    if not success:
        sys.exit(1)