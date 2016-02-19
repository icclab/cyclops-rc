#!/bin/bash
# Copyright (c) 2015. Zuercher Hochschule fuer Angewandte Wissenschaften
# All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may
# not use this file except in compliance with the License. You may obtain
# a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations
# under the License.
#
# Author: Piyush Harsh, Martin Skoviera

echo "Configuring logging folder structure"
touch /var/log/cyclops/rc.log
sudo chmod 777 /var/log/cyclops/rc.log

echo "Preparing configuration file"
cd ../src/main/webapp/WEB-INF/
mv configuration_cloudstack.txt configuration.txt
vi configuration.txt

sudo -k