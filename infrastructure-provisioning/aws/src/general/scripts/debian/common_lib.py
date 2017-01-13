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
from fabric.contrib.files import exists

def ensure_pkg(requisites, user):
    try:
        if not exists('/home/{}/.ensure_dir/pkg_upgraded'.format(user)):
            sudo('apt-get update')
            sudo('apt-get -y install ' + requisites)
            sudo('unattended-upgrades -v')
            sudo('export LC_ALL=C')
            sudo('mkdir /home/{}/.ensure_dir'.format(user))
            sudo('touch /home/{}/.ensure_dir/pkg_upgraded'.format(user))
        return True
    except:
        return False