#!/bin/bash
echo "---------------------------------------------------------------------------"
echo "| Setting up the database "
echo "---------------------------------------------------------------------------"
curl -X POST 'http://localhost:8086/db?u=root&p=root' -d '{"name": "rc_service"}'
curl -X POST 'http://localhost:8086/db/rc_service/users?u=root&p=root' -d '{"name": "admin", "password": "Yh9hvmhGev"}'
echo "---------------------------------------------------------------------------"
echo "| Installation process is complete "
echo "---------------------------------------------------------------------------"
echo "---------------------------------------------------------------------------"
echo "| if(all_Installations_Were_Successful_then){"
echo "|         Ready to Rock 'n Roll ! "
echo "|  } "
echo "---------------------------------------------------------------------------"