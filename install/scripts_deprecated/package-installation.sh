#!/bin/bash
echo "deb http://www.rabbitmq.com/debian/ testing main" | tee -a /etc/apt/sources.list.d/rabbitmq.list
curl -L -o ~/rabbitmq-signing-key-public.asc http://www.rabbitmq.com/rabbitmq-signing-key-public.asc
apt-key add ~/rabbitmq-signing-key-public.asc
wget -q http://repos.sensuapp.org/apt/pubkey.gpg -O- | apt-key add -
echo "deb  http://repos.sensuapp.org/apt sensu main" | tee -a /etc/apt/sources.list.d/sensu.list
apt-get update
echo "---------------------------------------------------------------------------"
echo "| Installing the Java openjdk-7-jre"
echo "| Java 7 is the baseline Java version"
echo "---------------------------------------------------------------------------"
apt-get install -y openjdk-7-jre
apt-get install -y openjdk-7-jdk
echo "---------------------------------------------------------------------------"
echo "| Installing Ruby, ruby-dev and Ruby build-essential"
echo "---------------------------------------------------------------------------"
apt-get install -y ruby ruby-dev build-essential
echo "---------------------------------------------------------------------------"
echo "| Installing Sensu plugin to support the check scricpts ¦ sensu-plugin & rest_client"
echo "---------------------------------------------------------------------------"
gem install sensu-plugin
gem install rest_client
echo "---------------------------------------------------------------------------"
echo "| Installing Maven and Git"
echo "---------------------------------------------------------------------------"
sudo add-apt-repository ppa:natecarlson/maven3
sudo add-apt-repository ppa:andrei-pozolotin/maven3
sudo add-apt-repository ppa:webupd8team/java
sudo apt-get update
apt-get install -y maven3
apt-get install -y git-core
echo "---------------------------------------------------------------------------"
echo "| Installing curl"
echo "| Dependent packages - libc6 libcurl3 zlib1g "
echo "---------------------------------------------------------------------------"
apt-get install -y curl
echo "---------------------------------------------------------------------------"
echo "| Installing Apache Server"
echo "---------------------------------------------------------------------------"
apt-get install -y apache2
echo "---------------------------------------------------------------------------"
echo "| Installing Redis"
echo "---------------------------------------------------------------------------"
apt-get -y install redis-server
echo "---------------------------------------------------------------------------"
echo "| Installing tomcat7 and tomcat7-admin"
echo "---------------------------------------------------------------------------"
apt-get install -y tomcat7
apt-get install -y tomcat7-admin

cat << EOF | sudo tee -a /etc/default/tomcat7
JAVA_OPTS="-Djava.awt.headless=true -Xmx128m -XX:+UseConcMarkSweepGC -Djava.security.egd=file:/dev/./urandom"
EOF

sudo service tomcat7 restart

echo "---------------------------------------------------------------------------"
echo "| Installing the v0.9.4.1 release of InfluxDB"
echo "---------------------------------------------------------------------------"
mkdir -p /tmp/rcservice
wget http://influxdb.s3.amazonaws.com/influxdb_0.9.4.1_amd64.deb -P /tmp/rcservice
echo "---------------------------------------------------------------------------"
echo "| Decompressing the InfluxDB package"
echo "---------------------------------------------------------------------------"
dpkg -i /tmp/rcservice/influxdb_0.9.4.1_amd64.deb
echo "---------------------------------------------------------------------------"
echo "| Starting InfluxDB"
echo "---------------------------------------------------------------------------"
/etc/init.d/influxdb restart
echo "---------------------------------------------------------------------------"
echo "| Starting the process of installing Sensu"
echo "---------------------------------------------------------------------------"
echo "---------------------------------------------------------------------------"
echo "| Installing RabbitMQ"
echo "---------------------------------------------------------------------------"
apt-get install -y rabbitmq-server erlang-nox
echo "---------------------------------------------------------------------------"
echo "| Installing SSL certificates"
echo "---------------------------------------------------------------------------"
wget http://sensuapp.org/docs/0.13/tools/ssl_certs.tar -P /tmp/rcservice
tar -xvf /tmp/rcservice/ssl_certs.tar -C /tmp/rcservice
CURRDIR=`pwd`
cd /tmp/rcservice/ssl_certs && ./ssl_certs.sh generate
mkdir -p /etc/rabbitmq/ssl && cp /tmp/rcservice/ssl_certs/sensu_ca/cacert.pem /tmp/rcservice/ssl_certs/server/cert.pem /tmp/rcservice/ssl_certs/server/key.pem /etc/rabbitmq/ssl
echo "---------------------------------------------------------------------------"
echo "| Creating RabbitMQ Config file"
echo "---------------------------------------------------------------------------"
cat > /etc/rabbitmq/rabbitmq.config << EOF
[
    {rabbit, [
    {ssl_listeners, [5671]},
    {ssl_options, [{cacertfile,"/etc/rabbitmq/ssl/cacert.pem"},
                   {certfile,"/etc/rabbitmq/ssl/cert.pem"},
                   {keyfile,"/etc/rabbitmq/ssl/key.pem"},
                   {verify,verify_peer},
                   {fail_if_no_peer_cert,true}]}
  ]}
].
EOF
echo "---------------------------------------------------------------------------"
echo "| Restarting RabbitMQ"
echo "---------------------------------------------------------------------------"
service rabbitmq-server restart
echo "---------------------------------------------------------------------------"
echo "| Adding virtual hosts to RabbitMQ"
echo "---------------------------------------------------------------------------"
rabbitmqctl add_vhost /sensu
rabbitmqctl add_user sensu rcservice
rabbitmqctl set_permissions -p /sensu sensu ".*" ".*" ".*"
echo "---------------------------------------------------------------------------"
echo "| Installing Sensu and Uchiwa, copying the ssl certificates to /etc/sensu/ssl"
echo "---------------------------------------------------------------------------"
apt-get install -y sensu uchiwa
mkdir -p /etc/sensu/ssl && cp /tmp/rcservice/ssl_certs/client/cert.pem /tmp/rcservice/ssl_certs/client/key.pem /etc/sensu/ssl
echo "---------------------------------------------------------------------------"
echo "| Sensu configuration begins"
echo "| Creating the RabbitMQ config file - Port 5671"
echo "---------------------------------------------------------------------------"
cat > /etc/sensu/conf.d/rabbitmq.json << EOF
{
  "rabbitmq": {
    "ssl": {
      "cert_chain_file": "/etc/sensu/ssl/cert.pem",
      "private_key_file": "/etc/sensu/ssl/key.pem"
    },
    "host": "localhost",
    "port": 5671,
    "vhost": "/sensu",
    "user": "sensu",
    "password": "rcservice"
  }
}
EOF
echo "---------------------------------------------------------------------------"
echo "| Creating the Redis config file - Port 6379"
echo "---------------------------------------------------------------------------"
cat > /etc/sensu/conf.d/redis.json << EOF
{
  "redis": {
    "host": "localhost",
    "port": 6379
  }
}
EOF
echo "---------------------------------------------------------------------------"
echo "| Creating the sensu API config file - Port 4567"
echo "---------------------------------------------------------------------------"
cat > /etc/sensu/conf.d/api.json << EOF
{
  "api": {
    "host": "localhost",
    "port": 4567
  }
}
EOF
echo "---------------------------------------------------------------------------"
echo "| Creating the Uchiwa config file - Port 4567"
echo "---------------------------------------------------------------------------"
cat > /etc/sensu/conf.d/uchiwa.json << EOF
{
    "sensu": [
        {
            "name": "Sensu",
            "host": "localhost",
            "ssl": false,
            "port": 4567,
            "path": "",
            "timeout": 5000
        }
    ],
    "uchiwa": {
        "port": 3000,
        "stats": 10,
        "refresh": 10000
    }
}
EOF
echo "---------------------------------------------------------------------------"
echo "| [Temporary Fix] Creating the Uchiwa config file - Port 4567"
echo "---------------------------------------------------------------------------"
cat > /etc/sensu/uchiwa.json << EOF
{
    "sensu": [
        {
            "name": "Sensu",
            "host": "localhost",
            "ssl": false,
            "port": 4567,
            "path": "",
            "timeout": 5000
        }
    ],
    "uchiwa": {
        "port": 3000,
        "stats": 10,
        "refresh": 10000
    }
}
EOF
echo "---------------------------------------------------------------------------"
echo "| Creating the Sensu client config file"
echo "---------------------------------------------------------------------------"
cat > /etc/sensu/conf.d/client.json << EOF
{
  "client": {
    "name": "server",
    "address": "localhost",
    "subscriptions": [ "ALL" ]
  }
}
EOF
echo "---------------------------------------------------------------------------"
echo "| Creating the generate-rate-rcservice check config file - Interval 10 mins"
echo "---------------------------------------------------------------------------"
cat > /etc/sensu/conf.d/generate_rate_rcservice.json << EOF
{
  "checks": {
    "generate_rate_rcservice": {
      "command": "/etc/sensu/plugins/generate-rate-rcservice.rb",
      "interval": 600,
      "subscribers": [ "ALL" ]
    }
  }
}
EOF
echo "---------------------------------------------------------------------------"
echo "| Creating the generate-rate-rcservice ping script"
echo "---------------------------------------------------------------------------"
cat > /etc/sensu/plugins/generate-rate-rcservice.rb << EOF
#!/usr/bin/env ruby
#
# Checks etcd node self stats
# ===
#
# DESCRIPTION:
#   This script pings the UDR service to trigger the data collection API
#
# OUTPUT:
#   plain-text
#
# PLATFORMS:
#   all
#
# DEPENDENCIES:
#   sensu-plugin Ruby gem
#   rest_client Ruby gem
#

require 'rubygems' if RUBY_VERSION < '1.9.0'
require 'sensu-plugin/check/cli'
require 'rest-client'
require 'json'

class PingUDRService < Sensu::Plugin::Check::CLI
  def run
    begin
      r = RestClient::Resource.new("http://localhost:8080/rc/generate/rate", :timeout => 60 ).get
      if r.code == 200
        ok "UDR service ping was successfull"
      else
        critical "Oops!"
      end
    end
  end
end
EOF
echo "---------------------------------------------------------------------------"
echo "| Setting the chmod status to 755 on check-udrservice.rb"
echo "---------------------------------------------------------------------------"
sudo chmod 755 /etc/sensu/plugins/generate-rate-rcservice.rb
echo "---------------------------------------------------------------------------"
echo "| Creating the generate-cdr-rcservice check config file - Interval 10 mins"
echo "---------------------------------------------------------------------------"
cat > /etc/sensu/conf.d/generate_cdr_rcservice.json << EOF
{
  "checks": {
    "generate_cdr_rcservice": {
      "command": "/etc/sensu/plugins/generate-cdr-rcservice.rb",
      "interval": 600,
      "subscribers": [ "ALL" ]
    }
  }
}
EOF
echo "---------------------------------------------------------------------------"
echo "| Creating the generate-cdr-rcservice ping script"
echo "---------------------------------------------------------------------------"
cat > /etc/sensu/plugins/generate-cdr-rcservice.rb << EOF
#!/usr/bin/env ruby
#
# Checks etcd node self stats
# ===
#
# DESCRIPTION:
#   This script pings the UDR service to trigger the data collection API
#
# OUTPUT:
#   plain-text
#
# PLATFORMS:
#   all
#
# DEPENDENCIES:
#   sensu-plugin Ruby gem
#   rest_client Ruby gem
#

require 'rubygems' if RUBY_VERSION < '1.9.0'
require 'sensu-plugin/check/cli'
require 'rest-client'
require 'json'

class PingUDRService < Sensu::Plugin::Check::CLI
  def run
    begin
      r = RestClient::Resource.new("http://localhost:8080/rc/generate/cdr", :timeout => 60 ).get
      if r.code == 200
        ok "UDR service ping was successfull"
      else
        critical "Oops!"
      end
    end
  end
end
EOF
echo "---------------------------------------------------------------------------"
echo "| Setting the chmod status to 755 on check-udrservice.rb"
echo "---------------------------------------------------------------------------"
sudo chmod 755 /etc/sensu/plugins/generate-cdr-rcservice.rb
echo "---------------------------------------------------------------------------"
echo "| Updating sensu-server sensu-client sensu-api uchiwa"
echo "---------------------------------------------------------------------------"
update-rc.d sensu-server defaults
update-rc.d sensu-client defaults
update-rc.d sensu-api defaults
update-rc.d uchiwa defaults
echo "---------------------------------------------------------------------------"
echo "| Starting sensu-server sensu-client sensu-api uchiwa"
echo "---------------------------------------------------------------------------"
service sensu-server restart
service sensu-client restart
service sensu-api restart
service uchiwa restart
echo "---------------------------------------------------------------------------"
echo "| Adding a manager for Tomcat "
echo "| user : admin ¦ password: Yh9hvmhGeBl"
echo "---------------------------------------------------------------------------"
cat > /etc/tomcat7/tomcat-users.xml << EOF
<?xml version='1.0' encoding='utf-8'?>
<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<!--
  NOTE:  By default, no user is included in the "manager-gui" role required
  to operate the "/manager/html" web application.  If you wish to use this app,
  you must define such a user - the username and password are arbitrary.
-->
<!--
  NOTE:  The sample user and role entries below are wrapped in a comment
  and thus are ignored when reading this file. Do not forget to remove
  <!.. ..> that surrounds them.
-->
<!--
  <role rolename="tomcat"/>
  <role rolename="role1"/>
  <user username="tomcat" password="tomcat" roles="tomcat"/>
  <user username="both" password="tomcat" roles="tomcat,role1"/>
  <user username="role1" password="tomcat" roles="role1"/>
-->
<tomcat-users>
    <user username="admin" password="Yh9hvmhGeBl" roles="manager-gui"/>
</tomcat-users>
EOF

sudo service tomcat7 restart

echo "---------------------------------------------------------------------------"
echo "| Starting Redis"
echo "---------------------------------------------------------------------------"
service redis-server restart
echo "---------------------------------------------------------------------------"
echo "| Removing the temp/udrservice folder"
echo "---------------------------------------------------------------------------"
rm -fR /tmp/rcservice