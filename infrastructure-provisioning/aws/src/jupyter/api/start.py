#!/usr/bin/python
from ConfigParser import SafeConfigParser
import os
from fabric.api import local, hide

if __name__ == "__main__":
#    with hide('stderr', 'running'):
#        local("cd /root; fab run")
    print '{"message": "justatest"}'