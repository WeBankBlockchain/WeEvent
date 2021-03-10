#!/bin/bash
#get parameter
para=""
current_path=$(pwd)

while [[ $# -ge 2 ]] ; do
    case "$1" in
    --out_path) para="$1 = $2;";out_path="$2";shift 2;;
    --listen_port) para="$1 = $2;";listen_port="$2";shift 2;;
    --block_chain_node_path) para="$1 = $2;";block_chain_node_path="$2";shift 2;;
    --version) para="$1 = $2;";version="$2";shift 2;;
    --zookeeper_connect_string) para="$1 = $2;";zookeeper_connect_string="$2";shift 2;;
    --fisco_config_file) para="$1 = $2;";fisco_config_file="$2";shift 2;;
    *) echo "unknown parameter $1." ; exit 1 ; break;;
    esac
done

echo "param out_path: ${out_path}"
echo "param listen_port: ${listen_port}"
echo "param version: ${version}"
echo "param block_chain_node_path: ${block_chain_node_path}"
echo "param zookeeper_connect_string: ${zookeeper_connect_string}"
echo "param fisco_config_file: ${fisco_config_file}"


#copy file
function copy_file(){
    mkdir -p ${out_path}
    cp -r ./* ${out_path}/
    cp ${fisco_config_file} ${out_path}/conf
    rm -f ${out_path}/install-broker.sh
}

copy_file


echo "set channel_info success"
if [[ -d ${block_chain_node_path} ]]; then
    rm -rf ${out_path}/conf/conf
    mkdir -p ${out_path}/conf/conf
    cp -rf ${block_chain_node_path}/* ${out_path}/conf/conf
else
    echo "ca.crt or sdk.crt or sdk.key is not exist."
    exit 1
fi

if [[ -f ${fisco_config_file} ]]; then
    cp -rf ${fisco_config_file} ${out_path}/conf/
else
    echo "fisco_config_file is not exist."
    exit 1
fi

#deploy contract
cd ${out_path}
./deploy-topic-control.sh
if [[ $? -eq 0 ]];then
    echo "deploy topic control contract success"
else
    echo "deploy topic control contract failed"
    exit 1
fi

cd ${current_path}

if [[ ${listen_port} -gt 0 ]]; then
    sed -i "/server.port=/cserver.port=${listen_port}" ${out_path}/conf/application-prod.properties
else
    echo "listen_port is err"
    exit 1
fi
echo "set lister_port success"

if [[ ${database_type} != "h2" ]];then
    switch_database_to_mysql "${application_properties}"

    if [[ -z ${mysql_ip} ]];then
        echo "mysql_ip is empty."
        echo "set mysql_ip failed"
        exit 1
    else
       sed -i "s/127.0.0.1:3306/${mysql_ip}:3306/" ${application_properties}
    fi
    echo "set mysql_ip success"

    if [[ -z ${mysql_port} ]];then
        echo "mysql_port is empty."
        echo "set mysql_port failed"
        exit 1
    else
        sed -i "s/3306/${mysql_port}/" ${application_properties}
    fi
    echo "set mysql_port success"

    if [[ -z ${mysql_user} ]];then
        echo "mysql_user is empty."
        echo "set mysql_user failed"
        exit 1
    else
        sed -i "s/xxxx/${mysql_user}/" ${application_properties}
    fi
    echo "set mysql_user success"

    if [[ -z ${mysql_pwd} ]];then
        echo "mysql_pwd is empty"
        echo "set mysql_pwd failed"
        exit 1
    else
        sed -i "s/yyyy/${mysql_pwd}/" ${application_properties}
    fi
    echo "set mysql_pwd success"
fi

if [[ -n ${zookeeper_connect_string} ]];then
  sed -i "/spring.cloud.zookeeper.connect-string=/cspring.cloud.zookeeper.connect-string=${zookeeper_connect_string}" ${out_path}/conf/application-prod.properties
else
    echo "zookeeper_connect_string is err"
    exit 1
fi
echo "set zookeeper_connect_string success"

# init db, create database and tables
cd ${out_path}
./init-broker.sh
if [[ $? -ne 0 ]];then

    echo "Error,init mysql fail"
    exit 1
fi
echo "init db success"

function switch_database_to_mysql() {
    mysql_config_line=$(cat -n $1|grep 'spring.jpa.database=mysql'|awk '{print $1}'|head -1)
    sed -i ''$mysql_config_line','$((mysql_config_line+4))'s/^#//' $1

    h2_config_line=$(cat -n $1|grep 'spring.jpa.database=h2'|awk '{print $1}'|head -1)
    sed -i ''$h2_config_line','$((h2_config_line+4))'s/^/#/' $1
}

echo "broker module install success"
