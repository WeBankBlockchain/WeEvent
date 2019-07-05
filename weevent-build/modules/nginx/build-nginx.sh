#!/bin/bash
# usage
if [ $# -lt 2 ]; then 
    echo "Usage:"
    echo "     ./build-nginx.sh -p /data/app/nginx "
    exit 1
fi

# param list
current_path=`pwd`
out_path=""

function nginx_setup() { 
    echo "install nginx into $out_path"
    
	if [[ ! -d $current_path/build ]];then
	    mkdir -p $current_path/build
	fi
    	
    tar -zxf $current_path/third-packages/nginx-1.14.2.tar.gz -C $current_path/build/
    if [ $? -ne 0 ]; then
        echo "decompress nginx-1.14.2.tar.gz failed, skip"
        exit 1
    fi
   
    tar -zxf $current_path/third-packages/pcre-8.20.tar.gz -C $current_path/build/
    if [ $? -ne 0 ]; then
        echo "decompress pcre-8.20.tar.gz failed, skip"
        exit 1
    fi

    echo "build & install pcre" 
    cd $current_path/build/pcre-8.20
    ./configure --prefix=$current_path/../third-packages
    make; make install
    if [ $? -ne 0 ]; then
        echo "install pcre failed, skip"
        exit 1
    fi

    echo "build & install nginx"
    cd ../nginx-1.14.2
    # --with-pcre need a pcre source path, not installed path
    ./configure --with-http_ssl_module --with-stream --with-stream_ssl_module --prefix=$out_path/nginx --with-pcre=../pcre-8.20
    make; make install
    if [ $? -ne 0 ]; then
        echo "install nginx failed, skip"
        exit 1
    fi
    cd $current_path

    echo "configure nginx.conf"
    mkdir -p $out_path/nginx/nginx_temp
    mkdir -p $out_path/nginx/conf/conf.d
    cp ./conf/nginx.conf $out_path/nginx/conf/
    cp ./conf/server.* $out_path/nginx/conf/
    cp ./conf/conf.d/*.conf $out_path/nginx/conf/conf.d/

    cp nginx.sh $out_path/nginx

    echo "nginx install complete!"
}

function main(){
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
    out_path=$2
    
    if [[ $2 == .* ]];then
       out_path=`cd $2; pwd`
    fi
    
	nginx_setup
	
}

main $1 $2