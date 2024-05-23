#!/bin/bash
mkdir /root/.ssh
cat docker-key.pub >> /root/.ssh/authorized_keys
echo "AuthorizedKeysFile /root/.ssh/authorized_keys" >> /etc/ssh/sshd_config
service ssh restart