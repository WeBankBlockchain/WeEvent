#!/bin/bash

function governance_setup() { 
    echo "install governance into $out_path "
    
    #create governance list
    mkdir -p $out_path
    cp -r ./* $out_path/
    rm -f $out_path/install-governance.sh
    
      
    if [[ -z $server_port ]];then
        echo "server_port is empty."
        echo "set server_port failed"
        exit 1
    else
       eval sed -i "s/8082/${server_port}/" $out_path/conf/application-prod.yml
    fi
    echo "set server_port success"
 
    if [[ -z $mysql_ip ]];then
        echo "mysql_ip is empty."
        echo "set mysql_ip failed"
        exit 1
    else
       eval sed -i "s/127.0.0.1:3306/${mysql_ip}:3306/" $out_path/conf/application-prod.yml
    fi
    echo "set mysql_ip success"
     
    if [[ -z $mysql_port ]];then
        echo "mysql_port is empty."
        echo "set mysql_port failed"
        exit 1
    else
       eval sed -i "s/3306/${mysql_port}/" $out_path/conf/application-prod.yml
    fi
    echo "set mysql_port success"

    if [[ -z $mysql_user ]];then
        echo "mysql_user is empty."
        echo "set mysql_user failed"
        exit 1
    else
       eval sed -i "s/xxxx/${mysql_user}/" $out_path/conf/application-prod.yml
    fi
    echo "set mysql_user success"
  
    if [[ -z $mysql_pwd ]];then
        echo "mysql_pwd is empty"
        echo "set mysql_pwd failed"
        exit 1
    else
       eval sed -i "s/yyyy/${mysql_pwd}/" $out_path/conf/application-prod.yml
    fi
    echo "set mysql_pwd success"
       
    if [[ -z $broker_port ]];then
        echo "broker_port is empty"
        echo "set broker_port failed"
        exit 1
    else
       eval sed -i "s/8081/${broker_port}/" $out_path/conf/application-prod.yml
    fi
    echo "set broker_port success"

    
    # init db,create datebase and tables
    cd $out_path
    ./init-governance.sh
    if [ $? -ne 0 ];then
        echo "Error,init mysql fail"
        exit 1
    fi
    echo "init db success"
    
    echo "governance module install success"
}

#get parameter
para=""
conf_path="./conf"
apps_path="./apps"
installPWD=$(dirname $(dirname `pwd`))

# usage
if [ $# -lt 2 ]; then 
    echo "Usage:"
    echo "    $0 --out_path /data/app/weevent-install/governance "
    echo "    --broker_port  --mysql_port  --mysql_user  --mysql_pwd  --influxdb_ip --influxdb_port"
    exit 1
fi

ssl=""
server_port=""
mysql_ip=""
mysql_port=""
mysql_user=""
mysql_pwd=""
broker_port=""
influxdb_ip=""
influxdb_port=""
out_path=""
current_path=`pwd`
echo "current path $current_path"

while [ $# -ge 2 ] ; do
    case "$1" in
        --out_path) para="$1 = $2;";out_path="$2";shift 2;;
        --server_port) para="$1 = $2;";server_port="$2";shift 2;;
        --mysql_ip) para="$1 = $2;";mysql_ip="$2";shift 2;;
        --mysql_port) para="$1 = $2;";mysql_port="$2";shift 2;;
        --mysql_user) para="$1 = $2;";mysql_user="$2";shift 2;;
        --mysql_pwd) para="$1 = $2;";mysql_pwd="$2";shift 2;;
        --broker_port) para="$1 = $2;";broker_port="$2";shift 2;;
        --influxdb_ip) para="$1 = $2;";influxdb_ip="$2";shift 2;;
        --influxdb_port) para="$1 = $2;";influxdb_port="$2";shift 2;;        
        --grafana_enable) para="$1 = $2;";grafana_enable="$2";shift 2;;
        --grafana_ip) para="$1 = $2;";grafana_ip="$2";shift 2;;
        --grafana_port) para="$1 = $2;";grafana_port="$2";shift 2;;
        --bee_ip) para="$1 = $2;";bee_ip="$2";shift 2;;
        --bee_port) para="$1 = $2;";bee_port="$2";shift 2;;		
        *) echo "unknown parameter $1." ; exit 1 ; break;;
    esac
done

governance_setup

