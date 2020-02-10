#!/bin/bash
#get parameter
para=""
current_path=$(pwd)

while [[ $# -ge 2 ]] ; do
    case "$1" in
    --out_path) para="$1 = $2;";out_path="$2";shift 2;;
    --gateway_port) para="$1 = $2;";gateway_port="$2";shift 2;;
    *) echo "unknown parameter $1." ; exit 1 ; break;;
    esac
done

echo "param out_path: ${out_path}"
echo "param listen_port: ${gateway_port}"

#copy file
function copy_file(){
    mkdir -p ${out_path}
    cp -r ./* ${out_path}/
    rm -f ${out_path}/install-gateway.sh
}

copy_file

cd ${current_path}

if [[ ${gateway_port} -gt 0 ]]; then
    sed -i "/server.port=/cserver.port=${gateway_port}" ${out_path}/conf/application-prod.properties
else
    echo "gateway_port is error"
    exit 1
fi
echo "set gateway_port success"

echo "gateway module install success"
