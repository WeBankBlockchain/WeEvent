#!/bin/bash

function nginx_setup() { 
    echo "install nginx into $nginx_path" &>> $top_path/install.log
    mkdir -p $nginx_path
    
    tar -zxf $top_path/third-packages/nginx-1.14.2.tar.gz -C $top_path/build/
    if [ $? -ne 0 ]; then
        echo "decompress nginx-1.14.2.tar.gz failed, skip"
        exit 1
    fi
   
    tar -zxf $top_path/third-packages/pcre-8.20.tar.gz -C $top_path/build/
    if [ $? -ne 0 ]; then
        echo "decompress pcre-8.20.tar.gz failed, skip"
        exit 1
    fi

    echo "build & install pcre" 
    cd $top_path/build/pcre-8.20
    ./configure --prefix=$current_path/../third-packages --with-http_ssl_module &>> $top_path/install.log
    make &>> $top_path/install.log
    make install &>> $top_path/install.log
    if [ $? -ne 0 ]; then
        echo "install pcre failed, skip"
        exit 1
    fi
           
    echo "build & install nginx"
    cd ../nginx-1.14.2
    ./configure --prefix=$nginx_path --with-http_ssl_module --with-pcre=../pcre-8.20 &>> $top_path/install.log
    make &>> $top_path/install.log
    make install &>> $top_path/install.log
    if [ $? -ne 0 ]; then
        echo "install nginx failed, skip"
        exit 1
    fi
    cd $current_path

    echo "configure nginx.conf" &>> $top_path/install.log
    mkdir -p $nginx_path/nginx_temp
    mkdir -p $nginx_path/conf/conf.d
    cp ./conf/nginx.conf $nginx_path/conf/
    cp ./conf/cert.* $nginx_path/conf/
    
    if [ "$ssl" = "true" ]; then
        cp ./conf/conf.d/https.conf $nginx_path/conf/conf.d/
        sed -i "s/8080/$nginx_port/g" $nginx_path/conf/conf.d/https.conf
    else
        cp ./conf/conf.d/http.conf $nginx_path/conf/conf.d/
        sed -i "s/8080/$nginx_port/g" $nginx_path/conf/conf.d/http.conf
    fi
    cp ./conf/conf.d/rs.conf $nginx_path/conf/conf.d/
    
    if [[ -n $broker_port ]]; then
        broker_url="localhost:$broker_port"
        echo "set broker_url: $broker_url" &>> $top_path/install.log
        sed -i "s/localhost:8081/$broker_url/g" $nginx_path/conf/conf.d/rs.conf
    fi
    if  [[ -n $governance_port ]]; then
        governance_url="localhost:$governance_port"
        echo "set governance_url: $governance_url" &>> $top_path/install.log
        sed -i "s/localhost:8082/$governance_url/g" $nginx_path/conf/conf.d/rs.conf
    fi
    
    cp nginx.sh $nginx_path

    echo "nginx mdoule install complete!" &>> $top_path/install.log
}

# usage
if [ $# -lt 2 ]; then 
    echo "Usage:"
    echo "    $0 --nginx_path /data/app/weevent/nginx "
    echo "    --broker_port --governance_port"
    exit 1
fi

# param list
nginx_path=
nginx_port=
ssl=
broker_port=
governance_port=
current_path=`pwd`
top_path=$(dirname $(dirname `pwd`))

param="";
while [ $# -ge 2 ]; do
    case "$1" in
        --nginx_path) param="$1 = $2;"; nginx_path="$2"; shift 2;;
        --nginx_port) param="$1 = $2;"; nginx_port="$2"; shift 2;;        
        --ssl) param="$1 = $2;"; ssl="$2"; shift 2;;
        --broker_ip) param="$1 = $2;"; broker_ip="$2"; shift 2;;
        --broker_port) param="$1 = $2;"; broker_port="$2"; shift 2;;
        --governance_ip) param="$1 = $2;"; governance_ip="$2"; shift 2;;
        --governance_port) param="$1 = $2;"; governance_port="$2"; shift 2;;
        *) echo "unknown parameter $1."; exit 1; break;;
        esac
done

nginx_setup
