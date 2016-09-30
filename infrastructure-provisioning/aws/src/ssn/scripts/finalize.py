#!/usr/bin/python
import boto3
import argparse
import sys

parser = argparse.ArgumentParser()
parser.add_argument('--key_id', type=str, default='')
args = parser.parse_args()


def cleanup(key_id):
    try:
        iam = boto3.resource('iam')
        current_user = iam.CurrentUser()
        for user_key in current_user.access_keys.all():
            if user_key.id == key_id:
                print "Deleted key " + user_key.id
                user_key.delete()
        return True
    except:
        return False

##############
# Run script #
##############

if __name__ == "__main__":
    if not cleanup(args.key_id):
        sys.exit(1)
    else:
        sys.exit(0)
