#!/usr/bin/python
import os
import base64
import json
from fabric.api import local


if __name__ == "__main__":
    log = local('cd /root; fab run')

    reply = dict()
    reply['request_id'] = os.environ['request_id']
    reply['status'] = 'ok'

    if len(log.stderr) > 0:
        reply['status'] = 'fail'
    reply['response'] = dict()

    try:
        with open("/root/result.json") as f:
            reply['response']['result'] = json.loads(f.read())
    except:
        pass

    try:
        with open("/root/runlog.log") as f:
            reply['response']['log'] = base64.b64encode(f.read())
    except:
        pass

    with open("/response/%s.json" % os.environ['request_id'], 'w') as response_file:
        response_file.write(json.dumps(reply))
