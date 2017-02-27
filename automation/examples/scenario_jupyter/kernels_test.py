import boto3
from fabric.api import *
import uuid
import argparse

parser = argparse.ArgumentParser()
parser.add_argument('--bucket', type=str, default='')
parser.add_argument('--cluster_name', type=str, default='')
parser.add_argument('--region', type=str, default='')
parser.add_argument('--os_user', type=str, default='')
args = parser.parse_args()

s3 = boto3.resource('s3', endpoint_url='https://s3-' + args.region + '.amazonaws.com')
s3.meta.client.upload_file('/tmp/train.csv', args.bucket, 'train.csv')
python_kernel_name = ['pyspark_' + args.cluster_name, 'py3spark_' + args.cluster_name, 'pyspark_local', 'py3spark_local']
scala_kernel_name = ['apache_toree_scala', 'toree_' + args.cluster_name]
r_kernel_name = ['ir', 'r_' + args.cluster_name]

for i in python_kernel_name:
    number = str(uuid.uuid4())[:5]
    with open('/tmp/PYTHON.ipynb', 'r') as f:
        text = f.read()
    text = text.replace('S3_BUCKET', args.bucket)
    text = text.replace('NUMBER', number)
    text = text.replace('KERNEL_NAME', i)
    with open('/home/USER/PYTHON.ipynb'.replace('USER', args.os_user), 'w') as f:
        f.write(text)
    local('sudo jupyter nbconvert --execute /home/USER/PYTHON.ipynb'.replace('USER', args.os_user))
    result = local('sudo echo $?', capture=True)
    if result == "0":
        res = "SUCCESS"
    else:
        res = "FAIL"
    print "Kernel: " + i
    print "Result: " + res
for i in scala_kernel_name:
    with open('/tmp/SCALA.ipynb', 'r') as f:
        text = f.read()
    text = text.replace('S3_BUCKET', args.bucket)
    text = text.replace('KERNEL_NAME', i)
    with open('/home/USER/SCALA.ipynb'.replace('USER', args.os_user), 'w') as f:
        f.write(text)
    local('sudo jupyter nbconvert --execute /home/USER/SCALA.ipynb'.replace('USER', args.os_user))
    result = local('sudo echo $?', capture=True)
    if result == "0":
        res = "SUCCESS"
    else:
        res = "FAIL"
    print "Kernel: " + i
    print "Result: " + res
for i in r_kernel_name:
    with open('/tmp/R.ipynb', 'r') as f:
        text = f.read()
    text = text.replace('S3_BUCKET', args.bucket)
    text = text.replace('KERNEL_NAME', i)
    with open('/home/USER/R.ipynb'.replace('USER', args.os_user), 'w') as f:
        f.write(text)
    local('sudo jupyter nbconvert --execute /home/USER/R.ipynb'.replace('USER', args.os_user))
    result = local('sudo echo $?', capture=True)
    if result == "0":
        res = "SUCCESS"
    else:
        res = "FAIL"
    print "Kernel: " + i
    print "Result: " + res
