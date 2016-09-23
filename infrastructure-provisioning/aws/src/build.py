#!/usr/bin/python
from fabric.api import *
import argparse
import os

parser = argparse.ArgumentParser()
parser.add_argument('--target', type=str, default='everything')
parser.add_argument('--tag', type=str, default='')
parser.add_argument('--builddir', action='')
parser.add_argument('--push', action='store_true')
args = parser.parse_args()


if args.builddir == '':
    args.builddir = os.path.dirname(os.path.realpath(__file__))

