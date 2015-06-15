## Introduction
Rating & Charging micro service is responsible for creating the Charge Data Records (CDR) of a user and also the rate of a resource. The micro service supports different rating policies such as Static & Dynamic. Rating & Charging Service uses the Usage Data Records generated by the UDR Service to combine with the rate of a cloud resource and calculate the Charge Data Records. The generated CDRs are made available for external applications through REST APIs using which charge and rate reports can be generated. 

## Getting Started
#### Process
* Clone the repo at https://github.com/icclab/cyclops-rc.git
* Update the configuration file at src/main/webapp/WEB-INF/configuration.txt
* Change directory to /install, edit the file permission and run install.sh

#### Installation Steps
     $ git clone https://github.com/icclab/cyclops-rc.git
     $ cd cyclops-rc/install
     $ chmod +x ./*
     $ bash install.sh
     
### Architecture
#### * CYCLOPS Rating Charging & Billing Framework
<img align="middle" src="http://blog.zhaw.ch/icclab/files/2013/05/overall_architecture.png" alt="CYCLOPS Architecture" height="480" width="600"></img>

#### * Rating & Charging Micro Service
<img align="middle" src="http://blog.zhaw.ch/icclab/files/2015/06/RatingChargingService.png" alt="RC Service Architecture" height="400" width="700"></img>

#### Documentation
1. <a href="https://github.com/icclab/cyclops-rc/wiki/API-Documentation">API Documentation</a>
2. Rate calculation process
3. CDR generation process
4. Interaction with the rate engine
5. Changes to the rating policy
6. CDR creation process

#### Bugs & Issues
To report any bugs or issues, please use <a href="https://github.com/icclab/cyclops-udr/issues">Github Issues</a>

### Contact Us
  * Issues/Ideas/Suggestions : <a href="https://github.com/icclab/cyclops-udr/issues">GitHub Issue</a>
  * Email : <a href="http://blog.zhaw.ch/icclab/srikanta-patanjali/">Srikanta</a> (pata at zhaw[dot]ch) or <a href="http://blog.zhaw.ch/icclab/piyush_harsh/">Piyush</a> (harh at zhaw[dot]ch)
  * Website : http://blog.zhaw.ch/icclab/ 
  * Tweet us @<a href="https://twitter.com/ICC_Lab">ICC_Lab</a>
   
### Developed @
<a href="http://blog.zhaw.ch/icclab/"><img src="http://blog.zhaw.ch/icclab/files/2014/04/icclab_logo.png" alt="ICC Lab" height="180" width="620"></img></a>

### License
 
      Licensed under the Apache License, Version 2.0 (the "License"); you may
      not use this file except in compliance with the License. You may obtain
      a copy of the License at
 
           http://www.apache.org/licenses/LICENSE-2.0
 
      Unless required by applicable law or agreed to in writing, software
      distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
      WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
      License for the specific language governing permissions and limitations
      under the License.


