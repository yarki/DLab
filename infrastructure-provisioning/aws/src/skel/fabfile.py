#!/usr/bin/python
from fabric.api import *
from ConfigParser import SafeConfigParser
import os
import logging
import argparse

parser = argparse.ArgumentParser()
parser.add_argument('--config_dir', type=str, default='/root/conf/')
args = parser.parse_args()


def get_configuration(configuration_dir):
    merged_config = SafeConfigParser()

    notebook_config = SafeConfigParser()
    notebook_config.read(configuration_dir + 'skel.ini')
    section = 'notebook'
    for option, value in notebook_config.items(section):
        if not merged_config.has_section(section):
            merged_config.add_section(section)
        merged_config.set(section, option, value)

    overwrite_config = SafeConfigParser()
    overwrite_config.read(configuration_dir + 'overwrite.ini')
    for section in ['creds', 'conf', 'ssn', 'notebook']:
        if overwrite_config.has_section(section):
            if not merged_config.has_section(section):
                merged_config.add_section(section)
            for option, value in overwrite_config.items(section):
                merged_config.set(section, option, value)

    shadow_overwrite_config = SafeConfigParser()
    shadow_overwrite_config.read(configuration_dir + 'shadow_overwrite.ini')
    for section in ['creds', 'conf', 'ssn', 'notebook']:
        if shadow_overwrite_config.has_section(section):
            if not merged_config.has_section(section):
                merged_config.add_section(section)
            for option, value in shadow_overwrite_config.items(section):
                merged_config.set(section, option, value)

    return merged_config


def create_aws_config_files(config):
    aws_user_dir = local('echo ~', capture=True) + "/.aws"
    logging.info(local("rm -rf " + aws_user_dir+" 2>&1", capture=True))
    logging.info(local("mkdir -p " + aws_user_dir+" 2>&1", capture=True))
    with open(aws_user_dir + '/config', 'w') as aws_file:
        aws_file.write("[default]\n")
        aws_file.write("region = %s\n" % config.get('creds', 'region'))

    logging.info(local("chmod 600 " + aws_user_dir + "/*"+" 2>&1", capture=True))
    logging.info(local("chmod 550 " + aws_user_dir+" 2>&1", capture=True))


def run_routine(routine_name, params):
    logging.info("~/scripts/%s.py %s" % (routine_name, params))
    shell_out = local("~/scripts/%s.py %s" % (routine_name, params))
    print shell_out
    logging.info(shell_out)
    print shell_out.stderr
    logging.error(shell_out.stderr)


def run(config):
    run_routine('install_prerequisites', "--aaa --bbb")


if __name__ == "__main__":
    config = get_configuration(args.config_dir)

    local_log_filename = "%s.log" % os.environ['request_id']
    local_log_filepath = "/response/" + local_log_filename
    logging.basicConfig(format='%(levelname)-8s [%(asctime)s]  %(message)s',
                        level=logging.DEBUG,
                        filename=local_log_filepath)

    env.warn_only = True

    run(config)
