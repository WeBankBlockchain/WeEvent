#!/bin/bash

outpath=""
blockchain_rpc=
web3sdk_conf_path=

broker_port=8081

nginx_port=8080

governance=
governance_port=

mysql_ip=
mysql_port=
mysql_user=
mysql_password=

installPWD=$PWD


function yellow_echo () {
    local what=$*
    if true;then
        echo -e "\e[1;33m${what} \e[0m"
    fi
}


function error_message()
{
    local message=$1
    echo "ERROR - ${message}"
    exit 1
}

function ini_get()
{
    local file="config.ini"
    local section=$1
    local param=$2
    local no_exit=$3
    local value=$($installPWD/build/crudini-0.9/crudini --get $file $section $param 2> /dev/null)
    if [ $? -ne 0 ];then
        if [ "${no_exit}" = "true" ];then
            #{ echo >&2 "ERROR - ini config get failed, section is $section param is $param."; exit 1; }
            echo "no_exit"
        else
            echo "error_message"
            error_message "ERROR - ini config get failed, section is $section param is $param."
        fi
    fi

    echo "$value"
}



function setgparam(){
    blockchain_rpc=$(ini_get "fisco-bcos" "channel")
    web3sdk_conf_path=$(ini_get  "fisco-bcos" "web3sdk_conf_path")
    if [[ $web3sdk_conf_path == .* ]];then
       web3sdk_conf_path=`cd $web3sdk_conf_path; pwd`
    fi

    nginx_port=$(ini_get "nginx" "port")
    
    broker_port=$(ini_get  "broker" "port")
    
    governance_enable=$(ini_get  "governance" "enable")
    governance_port=$(ini_get "governance" "port")

    mysql_ip=$(ini_get "governance" "mysql_ip")
    mysql_port=$(ini_get  "governance" "mysql_port")
    mysql_user=$(ini_get "governance" "mysql_user")
    mysql_password=$(ini_get "governance" "mysql_password")
}

function checkIp(){
    if [[ $ip =~ ^[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
      exit 0
    else
      echo "fail"
      exit 1
    fi
}

function checkPort(){
    local port="$1"
    local -i port_num=$(to_int "${port}" 2>/dev/null)

    if (( $port_num < 1 || $port_num > 65535 )) ; then
        echo "*** ${port} is not a valid port" 1>&2
        exit 1
    fi
}

function isPortFree(){ 
    lsof -i:${1} &>> $installPWD/install.log
    
    if [ $? -eq 1 ]
    then 
        echo "$1 port is okay"  &>> $installPWD/install.log
    else 
        echo "$1 port is use" 
        exit 1
    fi
}


function check-param(){
    if [ -d $web3sdk_conf_path ]; then
        isPortFree $broker_port
        isPortFree $nginx_port
        echo "param ok" &>> $installPWD/install.log
    else
        echo "path not exist, $web3sdk_conf_path"
        exit 1;
    fi
}

### check the result and print it
function getReturn(){    
    if [[ $1 -eq 0 ]];then
        yellow_echo "$2 install success"
    else
        exit 1;     
   fi
}


### set the module and params
function setModule(){
    
    params="--out_path $outpath --listen_port $broker_port --web3sdk_certpath $web3sdk_conf_path --channel_info $blockchain_rpc"       
    
    yellow_echo $params &>> $installPWD/install.log
    cd $installPWD/modules/broker
    ./install-broker.sh $params 
    getReturn $? broker


    nginxpath=$outpath"/nginx"
    params="--nginx_path $nginxpath --nginx_port $nginx_port --broker_port $broker_port --governance_port $governance_port"

    cd $installPWD/modules/nginx
    ./install-nginx.sh $params
    getReturn $? nginx

    if $governance_enable;then 
        governancepath=$outpath"/governance"
        params="--out_path $governancepath --server_port $governance_port --broker_port $broker_port --mysql_ip $mysql_ip --mysql_port $mysql_port  --mysql_user $mysql_user --mysql_pwd $mysql_password"

        yellow_echo "$params" &>> $installPWD/install.log
        cd $installPWD/modules/governance
        ./install-governance.sh $params
        getReturn $? governance
    fi
    
    cd $installPWD 
}

function install_crudini(){    
       
    echo "install crudini" &>> $installPWD/install.log
    mkdir -p $installPWD/build/
    tar -zxf $installPWD/third-packages/crudini-0.9.tar.gz -C $installPWD/build/  
       
    $installPWD/build/crudini-0.9/crudini --version &>> $installPWD/install.log
 
    sleep 1
    if [ $? -ne 0 ];then
        echo "install crudini failed, skip"
        exit 1
    fi
}

function update-check-server(){
    sed -i "s/8080\/weevent/$broker_port\/weevent/" check-service.sh
    sed -i "s/8082\/weevent-governance/$governance_port\/weevent-governance/" check-service.sh
}
function main(){  
    
    #crudini
    install_crudini

    # set the params
    setgparam
    # check the dir is exist or not
    check-param

    # confirm
    if [ -d $2 ]; then
        read -p "$2 already exist, continue? [Y/N]" cmd_input
        if [ "Y" != "$cmd_input" ]; then
            echo "input $cmd_input, install skipped"
            exit 1
        fi
    fi
    mkdir -p $2
    if [ $? -ne 0 ];then
        echo "create dir $2 fail !!! "
        exit 1
    fi
    outpath=$2
    
    if [[ $2 == .* ]];then
       outpath=`cd $2; pwd`
    fi
    
    # set module and the params
    setModule
    # set the check service port
    update-check-server

    cp start-all.sh $outpath/
    cp stop-all.sh $outpath/
    cp check-service.sh $outpath/
    cp uninstall-all.sh $outpath/

}


# Usage message
if [ $# -lt 2 ]; then
    echo "Usage:"
    echo "    ./install-all.sh -p install_path"
    exit 1
fi

main $1 $2