#!/usr/bin/python
import os
from ConfigParser import SafeConfigParser

for filename in os.listdir('/root/conf'):
    if filename.endswith('.ini'):
        config = SafeConfigParser()
        config.read(os.path.join('/root/conf', filename))
        for section in config.sections():
            for option in config.options(section):
                varname = "%s_%s" % (section, option)
                if varname not in os.environ:
                    with open('/root/.bashrc', 'a') as bashrc:
                        bashrc.write("export %s=%s\n" % (varname, config.get(section, option)))

# Enforcing overwrite
for filename in os.listdir('/root/conf'):
    if filename.endswith('overwrite.ini'):
        config = SafeConfigParser()
        config.read(os.path.join('/root/conf', filename))
        for section in config.sections():
            for option in config.options(section):
                varname = "%s_%s" % (section, option)
                with open('/root/.bashrc', 'a') as bashrc:
                    bashrc.write("export %s=%s\n" % (varname, config.get(section, option)))
