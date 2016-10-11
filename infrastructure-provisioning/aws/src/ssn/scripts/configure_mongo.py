#!/usr/bin/python
from pymongo import MongoClient
import random
import string
import yaml
import subprocess
import time
import sys

path = "/etc/mongod.conf"
outfile = "/etc/mongo_params.yml"


def id_generator(size=10, chars=string.digits + string.ascii_letters):
    return ''.join(random.choice(chars) for _ in range(size))


def read_yml_conf(path, section, param):
    try:
        with open(path, 'r') as config_yml:
            config = yaml.load(config_yml)
        result = config[section][param]
        return result
    except:
        print "File does not exist"
        return ''


def add_2_yml_config(path,section,param,value):
    try:
        try:
            with open(path, 'r') as config_yml_r:
                config_orig = yaml.load(config_yml_r)
        except:
            config_orig = {}
        sections = []
        for i in config_orig:
            sections.append(i)
        if section in sections:
            config_orig[section].update({param:value})
        else:
            config_orig.update({section:{param:value}})
        with open(path, 'w') as outfile_yml_w:
            yaml.dump(config_orig, outfile_yml_w, default_flow_style=True)
        return True
    except:
        print "Could not write the target file"
        return False

if __name__ == "__main__":
    # mongo_passwd = id_generator()
    mongo_passwd = "XS3ms9R3tP"
    mongo_ip = read_yml_conf(path,'net','bindIp')
    mongo_port = read_yml_conf(path,'net','port')

    # Setting up admin's password and enabling security
    client = MongoClient(mongo_ip + ':' + str(mongo_port))
    pass_upd = True
    try:
        command = ['service', 'mongod', 'start']
        subprocess.call(command, shell=False)
        time.sleep(5)
        client.dlabdb.add_user('admin', mongo_passwd, roles=[{'role':'userAdminAnyDatabase','db':'admin'}])
        client.dlabdb.command('grantRolesToUser', "admin", roles=["readWrite"])
        #client.dlabdb.grantRolesToUser("admin",["readWrite"])
        if add_2_yml_config(path,'security','authorization','enabled'):
            command = ['service', 'mongod', 'restart']
            subprocess.call(command, shell=False)
    except:
        print "Looks like MongoDB have already been secured"
        pass_upd = False

    # Generating output config
    add_2_yml_config(outfile,'network','ip',mongo_ip)
    add_2_yml_config(outfile,'network','port',mongo_port)
    add_2_yml_config(outfile,'account','user','admin')
    if pass_upd:
        add_2_yml_config(outfile,'account','pass',mongo_passwd)

