FROM docker.epmc-bdcc.projects.epam.com/dlab-aws-base:latest

MAINTAINER Volodymyr Shvachko (volodymyr_shvachko@epam.com)

COPY . /root/
RUN chmod 755 /root/fabfile.py; \
    chmod 755 /root/scripts/*; \
    chmod a+x /root/entrypoint.py; \
    chmod a+x /root/api/*; \
    mv /root/api/*.py /bin/

