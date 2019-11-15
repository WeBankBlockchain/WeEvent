#!/bin/bash

function nginx_setup() { 
    echo "install nginx into ${nginx_path}"
    mkdir -p ${nginx_path}

    if [[ ! -d ${top_path}/build ]];then
        mkdir -p ${top_path}/build
    fi

    tar -zxf ${top_path}/third-packages/nginx-1.14.2.tar.gz -C ${top_path}/build/
    if [[ $? -ne 0 ]]; then
        echo "decompress nginx-1.14.2.tar.gz failed, skip"
        exit 1
    fi

    echo "build & install nginx"
    cd ${top_path}/build/nginx-1.14.2
    ./configure --with-http_ssl_module --with-stream --with-stream_ssl_module --prefix=${nginx_path}; make; make install
    if [[ $? -ne 0 ]]; then
        echo "install nginx failed, skip"
        exit 1
    fi
    cd ${current_path}

    echo "configure nginx.conf"
    mkdir -p ${nginx_path}/nginx_temp
    mkdir -p ${nginx_path}/conf/conf.d
    cp ./conf/nginx.conf ${nginx_path}/conf/
    cp ./conf/server.* ${nginx_path}/conf/
    cp ./conf/conf.d/*.conf ${nginx_path}/conf/conf.d/

    sed -i "s/443/$nginx_port/g" ${nginx_path}/conf/conf.d/https.conf
    sed -i "s/8080/$nginx_port/g" ${nginx_path}/conf/conf.d/http_quickinstall.conf
    sed -i "s/nobody/${USER}/g" ${nginx_path}/conf/nginx.conf

    if [[ "$ssl" = "true" ]]; then
        sed -i "s/http.conf/https.conf/g" ${nginx_path}/conf/nginx.conf
        sed -i "s/tcp.conf/tcp_tls.conf/g" ${nginx_path}/conf/nginx.conf
    fi
    
    if [[ -n ${broker_port} ]]; then
        broker_url="localhost:$broker_port"
        echo "set broker_url: $broker_url"
        sed -i "s/localhost:7000/$broker_url/g" ${nginx_path}/conf/conf.d/http_rs_quickinstall.conf
    fi
    if  [[ -n ${governance_port} ]]; then
        governance_url="localhost:${governance_port}"
        echo "set governance_url: $governance_url"
        sed -i "s/localhost:7009/$governance_url/g" ${nginx_path}/conf/conf.d/http_rs_quickinstall.conf
    fi

    if  [[ -n ${processor_port} ]]; then
        processor_url="localhost:${processor_port}"
        echo "set processor_url: localhost:${processor_port}"
        sed -i "s/localhost:7008/$processor_url/g" ${nginx_path}/conf/conf.d/http_rs_quickinstall.conf
    fi
    
    cp nginx.sh ${nginx_path}

    echo "nginx module install success"
}

# usage
if [[ $# -lt 2 ]]; then
    echo "Usage:"
    echo "    $0 --nginx_path /data/app/weevent/nginx "
    echo "    --broker_port 7000 --governance_port 7009 --processor_port 7008"
    exit 1
fi

# param list
nginx_path=
nginx_port=
ssl=
broker_port=
governance_port=
processor_port=
current_path=$(pwd)
top_path=$(dirname $(dirname $(pwd)))

param="";
while [[ $# -ge 2 ]]; do
    case "$1" in
        --nginx_path) param="$1 = $2;"; nginx_path="$2"; shift 2;;
        --nginx_port) param="$1 = $2;"; nginx_port="$2"; shift 2;;        
        --ssl) param="$1 = $2;"; ssl="$2"; shift 2;;
        --broker_ip) param="$1 = $2;"; broker_ip="$2"; shift 2;;
        --broker_port) param="$1 = $2;"; broker_port="$2"; shift 2;;
        --governance_ip) param="$1 = $2;"; governance_ip="$2"; shift 2;;
        --governance_port) param="$1 = $2;"; governance_port="$2"; shift 2;;
        --processor_port) param="$1 = $2;"; processor_port="$2"; shift 2;;
        *) echo "unknown parameter $1."; exit 1; break;;
        esac
done

nginx_setup
