#!/bin/bash
#get parameter
para="";
conf_path="./conf"
lib_path="./lib"
apps_path="./apps"
installPWD=$(dirname $(dirname `pwd`))


while [ $# -ge 2 ] ; do
    case "$1" in
    --out_path) para="$1 = $2;";out_path="$2";shift 2;;
    --listen_port) para="$1 = $2;";listen_port="$2";shift 2;;
    --web3sdk_certpath) para="$1 = $2;";web3sdk_certpath="$2";shift 2;;
    --channel_info) para="$1 = $2;";channel_info="$2";shift 2;;
    *) echo "unknown parameter $1." ; exit 1 ; break;;
    esac
done

echo "param out_path:"$out_path &>> $installPWD/install.log
echo "param listen_port:"$listen_port &>> $installPWD/install.log
echo "param web3sdk_certpath:"$web3sdk_certpath &>> $installPWD/install.log
echo "param channel_info:"$channel_info &>> $installPWD/install.log

#copy file
function copy_file(){
    cp $apps_path/* $out_path/apps/
    cp $conf_path/* $out_path/conf/
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


#deploy contract
function deploy_contract(){
    java -Xbootclasspath/a:$out_path/conf -cp $out_path/apps/* -Dloader.main=com.webank.weevent.broker.fisco.util.Web3sdkUtils org.springframework.boot.loader.PropertiesLauncher $out_path/conf/address.txt  &>> $installPWD/install.log
}


#replace conf parameter
if [[ -d $out_path ]]; then
    out_path=$out_path/broker
    make_file
    copy_file
else
    echo "out_path is not exist"
    exit -1
fi

if [[ -f $web3sdk_certpath/ca.crt ]] && [[ -f $web3sdk_certpath/client.keystore ]] && [[ -f $web3sdk_certpath/applicationContext.xml ]]; then
    rm -rf $out_path/conf/applicationContext.xml
    rm -rf $out_path/conf/ca.crt
    rm -rf $out_path/conf/client.keystore
    cp $web3sdk_certpath/ca.crt $out_path/conf/
    cp $web3sdk_certpath/client.keystore $out_path/conf/
    cp $web3sdk_certpath/applicationContext.xml $out_path/conf/
else
    echo "ca.crt or client.keystore or applicationContext.xml is not exist."
    exit -1
fi

if [[ -z $channel_info ]];
then
    echo "channel_info is empty."
    exit -1
else
    channel_ipport=""
    array=(${channel_info//;/ })
    for var in ${array[@]}
    do
       channel_ipport=$channel_ipport"<value>"$var"</value>"
    done
    line1=$(grep -n -o "<list>" $out_path/conf/applicationContext.xml | awk -F: '{print $1}')
    line2=$(grep -n -o "</list>" $out_path/conf/applicationContext.xml | awk -F: '{print $1}')
    sed -i "${line1},${line2}d" $out_path/conf/applicationContext.xml
    channel_port="<list>"$channel_ipport"</list>"
    sed -i "${line1}i ${channel_port}" $out_path/conf/applicationContext.xml
fi
echo "set channel_info success" &>> $installPWD/install.log

deploy_contract
contract_address=`cat $out_path/conf/address.txt | awk -F '=' '{print $2}'`
if [[ -z $contract_address ]];
then
    echo "deploy contract error"
    exit -1
else
    echo "deploy contract success"
    echo "contract_address:"$contract_address
    sed -i "/fisco.topic-controller.contract-address=/cfisco.topic-controller.contract-address=${contract_address}" $out_path/conf/weevent.properties
fi

if [[ $listen_port -gt 0 ]]; then
    sed -i "/server.port=/cserver.port=${listen_port}" $out_path/conf/application-prod.properties
else
    echo "listen_port is err"
    exit -1
fi
echo "set lister_port success" &>> $installPWD/install.log

echo "broker setup success" &>> $installPWD/install.log