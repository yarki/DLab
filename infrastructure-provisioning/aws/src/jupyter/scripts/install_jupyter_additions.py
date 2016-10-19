#!/usr/bin/python
from fabric.api import *
from fabric.contrib.files import exists
import argparse
import json
import sys

parser = argparse.ArgumentParser()
parser.add_argument('--hostname', type=str, default='edge')
parser.add_argument('--keyfile', type=str, default='')
parser.add_argument('--additional_config', type=str, default='{"empty":"string"}')
args = parser.parse_args()


def ensure_matplot():
    if not exists('/home/ubuntu/matplot_ensured'):
        try:
            sudo('apt-get build-dep -y python-matplotlib')
            sudo('pip install matplotlib')
            sudo('pip3 install matplotlib')
            sudo('touch /home/ubuntu/matplot_ensured')
        except:
            sys.exit(1)


def ensure_sbt():
    if not exists('/home/ubuntu/sbt_ensured'):
        try:
            sudo('apt-get install -y apt-transport-https')
            sudo('echo "deb https://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list')
            sudo('apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 642AC823')
            sudo('apt-get update')
            sudo('apt-get install -y sbt')
            sudo('touch /home/ubuntu/sbt_ensured')
        except:
            sys.exit(1)


def ensure_libraries_py2():
    if not exists('/home/ubuntu/ensure_libraries_py2_installed'):
        try:
            sudo('export LC_ALL=C')
            sudo('apt-get install zlib1g-dev')
            sudo('pip2 install boto boto3')
            sudo('pip2 install NumPy SciPy Matplotlib pandas Sympy Pillow sklearn')
            sudo('touch /home/ubuntu/ensure_libraries_py2_installed')
        except:
            sys.exit(1)


def ensure_libraries_py3():
    if not exists('/home/ubuntu/ensure_libraries_py3_installed'):
        try:
            sudo('pip3 install boto boto3')
            sudo('pip3 install NumPy SciPy Matplotlib pandas Sympy Pillow sklearn')
            sudo('touch /home/ubuntu/ensure_libraries_py3_installed')
        except:
            sys.exit(1)

##############
# Run script #
##############
if __name__ == "__main__":
    print "Configure connections"
    env['connection_attempts'] = 100
    env.key_filename = [args.keyfile]
    env.host_string = 'ubuntu@' + args.hostname
    deeper_config = json.loads(args.additional_config)

    print "Installing required libraries for Python 2.7"
    ensure_libraries_py2()

    print "Installing required libraries for Python 3"
    ensure_libraries_py3()

    print "Installing notebook additions: matplotlib."
    ensure_matplot()

    print "Installing notebook additions: sbt."
    ensure_sbt()

