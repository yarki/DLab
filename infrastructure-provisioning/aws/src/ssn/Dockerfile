FROM docker.epmc-bdcc.projects.epam.com/dlab-aws-base:latest

MAINTAINER Victor Andreev (victor_andreev@epam.com)

COPY ssn/ /root/
RUN chmod 755 /root/fabfile.py; \
    chmod 755 /root/scripts/*; \
    chmod a+x /root/entrypoint.py; \
    chmod a+x /root/api/*; \
    mv /root/api/*.py /bin/

RUN mkdir /project_tree
COPY . /project_tree

