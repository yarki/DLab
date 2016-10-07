#!/usr/bin/python
import argparse
from dlab.fab import *
from dlab.aws_meta import *
import json
import sys


parser = argparse.ArgumentParser()
parser.add_argument('--config_dir', type=str, default='/root/conf/')
args = parser.parse_args()


def run():
    local_log_filename = "%s.log" % os.environ['request_id']
    local_log_filepath = "/response/" + local_log_filename
    logging.basicConfig(format='%(levelname)-8s [%(asctime)s]  %(message)s',
                        level=logging.DEBUG,
                        filename=local_log_filepath)

    try:
        if not run_routine('install_prerequisites', "--aaa --bbb"):
            logging.info('Failed')
            with open("/root/result.json", 'w') as result:
                res = {"error": "Failed", "conf": "failconf"}
                print json.dumps(res)
                result.write(json.dumps(res))
            sys.exit(1)
    except:
        sys.exit(1)

    with open("/root/result.json", 'w') as result:
        res = {"hostname": "skel"}
        print json.dumps(res)
        result.write(json.dumps(res))

    sys.exit(0)
