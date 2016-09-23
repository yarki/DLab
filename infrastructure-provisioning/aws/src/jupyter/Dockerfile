FROM docker.epmc-bdcc.projects.epam.com/dlab-aws-base:latest

MAINTAINER Victor Andreev (victor_andreev@epam.com)

COPY jupyter/ /root/
RUN chmod 755 /root/fabfile.py; \
    chmod 400 /root/keys/*; \
    chmod 755 /root/scripts/*; \
    ln -s /root /usr/share/notebook_automation; \
    chmod a+x /root/entrypoint.py; \
    chmod a+x /root/api/*; \
    mv /root/api/*.py /bin/

