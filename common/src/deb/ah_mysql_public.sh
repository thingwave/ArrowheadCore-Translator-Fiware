#!/bin/sh -e

. /usr/share/debconf/confmodule
. /usr/share/arrowhead/conf/ahconf.sh

if [ $(mysql -u root -sse "SELECT EXISTS(SELECT 1 FROM mysql.user WHERE user = 'arrowhead' AND host = '%')") != 1 ]; then
    mysql -e "CREATE USER arrowhead@'%' IDENTIFIED BY '${AH_PASS_DB}';"
    mysql -e "GRANT ALL PRIVILEGES ON arrowhead.* TO arrowhead@'%';"
    mysql -e "FLUSH PRIVILEGES;"
fi
sed -i 's/^\(bind-address[ \t]*=[ \t]*\).*$/\10.0.0.0/' /etc/mysql/mysql.conf.d/mysqld.cnf
systemctl restart mysql

echo >&2
echo "Use MySQL user 'arrowhead' with password '${AH_PASS_DB}'" >&2
echo >&2
