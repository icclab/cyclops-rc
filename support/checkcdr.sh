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
# Author: Piyush Harsh,
# URL: piyush-harsh.info

# After installing gatekeeper, simply run this script to test if all API endpoints 
# are working properly or not. Make sure that Gatekeeper has been srated before
# runnig this test script. Change APIPATH to appropriate value.

APIPATH="http://localhost:8080/rc/generate/cdr"
response=$(curl --write-out %{http_code} --silent --output /dev/null $APIPATH)

if [ "$response" == "200" ]; then
    echo "CDR Generation Successful"
    exit 0
else
    echo "RC-Error: Internal Server Error"
    exit 2
fi