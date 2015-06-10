## Introduction
Rating & Charging micro service is responsible for creating the Charge Data Records (CDR) of a user and also the rate of a resource. The micro service supports different rating policies such as Static & Dynamic. Rating & Charging Service uses the Usage Data Records generated by the UDR Service to combine with the rate of a cloud resource and calculate the Charge Data Records. The generated CDRs are made available for external applications through REST APIs using which charge and rate reports can be generated. 

#### Rate Calculation
The rate of a resource 

#### 

## Getting Started
#### Installation
* Clone the repo at https://github.com/icclab/cyclops-rc.git
* Update the configuration file at src/main/webapp/WEB-INF/configuration.txt
* Change directory to /install, edit the file permission and run install.sh
     $ git clone https://github.com/icclab/cyclops-rc.git
     $ cd cyclops-rc/install
     $ chmod +x ./*
     $ bash install.sh


#### 

