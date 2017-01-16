#!/usr/bin/python

# *****************************************************************************
#
# Copyright (c) 2016, EPAM SYSTEMS INC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# ******************************************************************************

from fabric.api import *
import argparse
import json
import sys
from dlab.ssn_lib import *

parser = argparse.ArgumentParser()
parser.add_argument('--hostname', type=str, default='')
parser.add_argument('--keyfile', type=str, default='')
parser.add_argument('--additional_config', type=str, default='{"empty":"string"}')
parser.add_argument('--os_user', type=str, default='')
parser.add_argument('--dlab_path', type=str, default='')
args = parser.parse_args()


##############
# Run script #
##############
if __name__ == "__main__":
    print "Configure connections"
    try:
        env['connection_attempts'] = 100
        env.key_filename = [args.keyfile]
        env.host_string = args.os_user + '@' + args.hostname
        deeper_config = json.loads(args.additional_config)
    except:
        sys.exit(2)

    print "Creating service directories."
    if not creating_service_directories(args.dlab_path, args.os_user):
        sys.exit(1)

    print "Installing nginx as frontend."
    if not ensure_nginx(args.dlab_path):
        sys.exit(1)

    print "Configuring nginx."
    if not configure_nginx(deeper_config, args.dlab_path):
        sys.exit(1)

    print "Installing jenkins."
    if not ensure_jenkins(args.dlab_path):
        sys.exit(1)

    print "Configuring jenkins."
    if not configure_jenkins(args.dlab_path, args.os_user):
        sys.exit(1)

    print "Copying key"
    if not cp_key(args.keyfile, env.host_string, args.os_user):
        sys.exit(1)

    sys.exit(0)
