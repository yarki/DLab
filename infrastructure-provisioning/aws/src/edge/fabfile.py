#!/usr/bin/python
import argparse
from dlab.fab import *
from dlab.aws_meta import *


def run():
    local_log_filename = "%s.log" % os.environ['request_id']
    local_log_filepath = "/response/" + local_log_filename
    logging.basicConfig(format='%(levelname)-8s [%(asctime)s]  %(message)s',
                        level=logging.DEBUG,
                        filename=local_log_filepath)

    env.warn_only = True
    run_routine('install_prerequisites', "--aaa --bbb")


