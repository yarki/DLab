#!/usr/bin/python
from ConfigParser import SafeConfigParser
import os
from fabric.api import local, hide


def create_shadow_config():
    sections = ['creds', 'conf', 'ssn', 'user']
    shadow_overwrite_config = SafeConfigParser()
    shadow_config_file = os.environ['PROVISION_CONFIG_DIR'] + 'shadow_overwrite.ini'
    for key in os.environ:
        transitional_key = key.lower()
        for section in sections:
            if transitional_key.startswith(section):
                if not shadow_overwrite_config.has_section(section):
                    shadow_overwrite_config.add_section(section)
                shadow_overwrite_config.set(section, transitional_key[len(section) + 1:], os.environ[key])

                print transitional_key[len(section) + 1:] + ' : ' + os.environ[key]
    with open(shadow_config_file, 'w') as shadow_config:
        shadow_overwrite_config.write(shadow_config)


if __name__ == "__main__":
    create_shadow_config()
#    with hide('stderr', 'running'):
#        local("cd /root; fab run")
    print '{"message": "justatest"}'