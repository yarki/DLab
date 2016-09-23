FROM ubuntu:16.04

MAINTAINER Victor Andreev (victor_andreev@epam.com)

# Install any .deb dependecies
RUN	apt-get update && \
    apt-get -y upgrade && \
    apt-get -y install python-pip python-dev groff vim less git wget nano && \
    apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# Install any python dependencies
RUN pip install boto3 fabric awscli boto argparse ujson jupyter

# Configuring ssh for user
RUN mkdir -p /root/.ssh; echo "Host *" > /root/.ssh/config; \
    echo "StrictHostKeyChecking no" >>  /root/.ssh/config; \
    echo "UserKnownHostsFile=/dev/null" >> /root/.ssh/config; \
    echo "GlobalKnownHostsFile=/dev/null" >> /root/.ssh/config; \
    echo "ConnectTimeout=30" >> /root/.ssh/config

RUN mkdir -p /response; chmod a+rwx /response

# Configuring entrypoint script
ENV PROVISION_CONFIG_DIR /root/conf/
ENV KEYFILE_DIR /root/keys/
RUN mkdir -p /root/conf
COPY base/ /root
RUN chmod a+x /root/entrypoint.py

# Entrypoint
ENTRYPOINT ["/root/entrypoint.py"]
