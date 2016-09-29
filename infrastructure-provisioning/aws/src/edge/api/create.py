#!/usr/bin/python
import os
import json
from fabric.api import local


if __name__ == "__main__":
    local('cd /root; fab run')

    reply = dict()
    reply['request_id'] = os.environ['request_id']
    reply['status'] = 'ok'

    reply['response'] = dict()

    try:
        with open("/root/result.json") as f:
            reply['response']['result'] = json.loads(f.read())
    except:
        pass

    reply['response']['log'] = "/response/%s.log" % os.environ['request_id']

    with open("/response/%s.json" % os.environ['request_id'], 'w') as response_file:
        response_file.write(json.dumps(reply))
