from fabric.api import *
from fabric.contrib.files import exists
import logging
import os


def ensure_apt(requisites):
    if not exists('/tmp/apt_upgraded'):
        sudo('apt-get update')
        sudo('apt-get -y upgrade')
        sudo('touch /tmp/apt_upgraded')
    sudo('apt-get -y install ' + requisites)


def ensure_pip(requisites):
    if not exists('/tmp/pip_path_added'):
        sudo('echo PATH=$PATH:/usr/local/bin/:/opt/spark/bin/ >> /etc/profile')
        sudo('echo export PATH >> /etc/profile')
        sudo('touch /tmp/pip_path_added')
    sudo('pip install -U ' + requisites)


def run_routine(routine_name, params):
    logging.info("~/scripts/%s.py %s" % (routine_name, params))
    shell_out = local("~/scripts/%s.py %s" % (routine_name, params))
    print shell_out
    logging.info(shell_out)
    print shell_out.stderr
    logging.error(shell_out.stderr)


def create_aws_config_files(generate_full_config=False):
    aws_user_dir = os.environ['AWS_DIR']
    logging.info(local("rm -rf " + aws_user_dir+" 2>&1", capture=True))
    logging.info(local("mkdir -p " + aws_user_dir+" 2>&1", capture=True))

    with open(aws_user_dir + '/config', 'w') as aws_file:
        aws_file.write("[default]\n")
        aws_file.write("region = %s\n" % os.environ['creds_region'])

    if generate_full_config:
        with open(aws_user_dir + '/credentials', 'w') as aws_file:
            aws_file.write("[default]\n")
            aws_file.write("aws_access_key_id = %s\n" % os.environ['creds_access_key'])
            aws_file.write("aws_secret_access_key = %s\n" % os.environ['creds_secret_access_key'])

    logging.info(local("chmod 600 " + aws_user_dir + "/*"+" 2>&1", capture=True))
    logging.info(local("chmod 550 " + aws_user_dir+" 2>&1", capture=True))
