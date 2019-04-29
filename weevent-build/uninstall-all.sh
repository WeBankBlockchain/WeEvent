#!/bin/bash
cur_path=`pwd`
brokerpid_path=$cur_path/broker/logs/broker.pid
nginxpid_path=$cur_path/nginx/logs/nginx.pid

function main(){
    cd $cur_path
	
    if [ -e $cur_path/install-all.sh ];then 
        echo "Error operation "
        echo "Current path $cur_path is source code package,only install path can execute uninstall.sh "
    	exit 1
    fi
	
    if [ ! -e $cur_path/uninstall-all.sh ] || [ ! -e $cur_path/start-all.sh ] || [ ! -d $cur_path/broker ] || [ ! -d $cur_path/nginx ] ;then
        echo "Error operation "
        echo "Current path $cur_path is not the install path,please retry after enter the install path"
        exit 1
    fi
    
    if [ -e $brokerpid_path ]; then
        eventbroker_pid=`cat $brokerpid_path`
        currentbroker_pid=`ps aux|grep "broker" | grep "$eventbroker_pid" | grep -v grep|awk '{print $2}'|head -1`
    fi
     
    if [ -e $nginxpid_path ]; then
       file_pid=`cat $nginxpid_path`
       currentnginx_pid=`ps aux|grep "nginx" | grep "$file_pid" | grep -v grep|awk '{print $2}'|head -1`
    fi
   
    if [ -n "$currentbroker_pid" ] || [ -n "$currentnginx_pid" ];then
        read -p "WeEvent is running, stop it first? [Y/N]" cmd_input
        if [ "Y" != "$cmd_input" ]; then
            echo "input $cmd_input, stop skipped"
            exit 1
        fi
        
        ./stop-all.sh
        if [ $? -ne 0 ];then
            echo "stop WeEvent fail"
            exit 1
        fi	
    fi
	
    read -p "Please confirm if  you remove the WeEvent?[Y/N]" cmd_input
    if [ "Y" != "$cmd_input" ]; then
        echo "input $cmd_input, uninstall skipped"
        exit 1
    fi
	   
    cd ..
    #uninstall;
    rm -rf $cur_path
    if [ $? -ne 0 ];then
        echo "uninstall fail "
        exit 1
    fi
    echo "uninstall weevent success "
}

main