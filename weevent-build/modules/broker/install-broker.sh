#!/bin/bash
#get parameter
para=""
conf_path="./conf"
lib_path="./lib"
apps_path="./apps"
installPWD=$(dirname $(dirname `pwd`))
current_path=`pwd`

while [ $# -ge 2 ] ; do
    case "$1" in
    --out_path) para="$1 = $2;";out_path="$2";shift 2;;
    --listen_port) para="$1 = $2;";listen_port="$2";shift 2;;
    --block_chain_node_path) para="$1 = $2;";block_chain_node_path="$2";shift 2;;
    --channel_info) para="$1 = $2;";channel_info="$2";shift 2;;
    --version) para="$1 = $2;";version="$2";shift 2;;
    *) echo "unknown parameter $1." ; exit 1 ; break;;
    esac
done

echo "param out_path: $out_path"
echo "param listen_port: $listen_port"
echo "param version: $version"
echo "param block_chain_node_path: $block_chain_node_path"
echo "param channel_info: $channel_info"

#copy file
function copy_file(){
    cp $apps_path/* $out_path/apps/
    cp -r $conf_path/* $out_path/conf/
    cp $conf_path/../deploy-topic-control.sh $out_path/
    cp $conf_path/../gen-cert-key.sh $out_path/
    cp $conf_path/../broker.sh $out_path/
    cp $conf_path/../check-service.sh $out_path/   
}

#mkdir file
function make_file(){
    mkdir -p $out_path/conf
    mkdir -p $out_path/apps
    mkdir -p $out_path/lib
    mkdir -p $out_path/logs
}

if [[ -z $channel_info ]];then
    echo "channel_info is empty."
    exit -1
fi

mkdir -p $out_path
make_file
copy_file

sed -i "s/^.*nodes=.*$/nodes=$channel_info/g" $out_path/conf/fisco.properties
echo "set channel_info success"
if [[ $version = "1.3" ]];then
    sed -i "/version=2.0/cversion=1.3" $out_path/conf/fisco.properties
    if [[ -f $block_chain_node_path/ca.crt ]] && [[ -f $block_chain_node_path/client.keystore ]]; then
        rm -rf $out_path/conf/ca.crt $out_path/conf/client.keystore
        cp $block_chain_node_path/ca.crt $block_chain_node_path/client.keystore $out_path/conf/
    else
        echo "ca.crt or client.keystore is not exist."
        exit -1
    fi

    #deploy contract
    cd $out_path
    ./deploy-topic-control.sh
    cd $current_path
    contract_address=`cat $out_path/address.txt | awk -F '=' '{print $2}'`
    if [[ -z $contract_address ]];then
        echo "deploy contract error"
        exit -1
    else
        echo "deploy contract success"
        echo "contract_address:"$contract_address
        sed -i "s/^.*topic-controller.address=.*$/topic-controller.address=${contract_address}/g" $out_path/conf/fisco.properties
    fi
else
    if [[ -f $block_chain_node_path/ca.crt ]] && [[ -f $block_chain_node_path/node.crt ]] && [[ -f $block_chain_node_path/node.key ]]; then
        rm -rf $out_path/conf/v2/ca.crt $out_path/conf/v2/node.crt $out_path/conf/v2/node.key
        cp $block_chain_node_path/ca.crt $block_chain_node_path/node.crt $block_chain_node_path/node.key $out_path/conf/v2/
    else
        echo "ca.crt or node.crt or node.key is not exist."
        exit -1
    fi

    #deploy contract
    cd $out_path
    ./deploy-topic-control.sh 1
    cd $current_path
    contract_address=`cat $out_path/address.txt | awk -F '=' '{print $2}'`
    if [[ -z $contract_address ]];then
        echo "deploy contract error"
        exit -1
    else
        echo "deploy contract success"
        echo "contract_address:"$contract_address
        sed -i "s/^.*topic-controller.address=.*$/topic-controller.address=1:${contract_address}/g" $out_path/conf/fisco.properties
    fi
fi

if [[ $listen_port -gt 0 ]]; then
    sed -i "/server.port=/cserver.port=${listen_port}" $out_path/conf/application-prod.properties
else
    echo "listen_port is err"
    exit -1
fi
echo "set lister_port success"

echo "broker module install success"
