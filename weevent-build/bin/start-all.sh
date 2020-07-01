#!/bin/bash
current_path=$(pwd)

if [[ -e ${current_path}/install-all.sh ]];then
   echo "Error operation "
   echo "Current path ${current_path} is source code package, only install path can execute start-all.sh "
   exit 1
fi

# start zookeeper first
cd ${current_path}/zookeeper/apache-zookeeper-3.6.0-bin/bin/
./zkServer.sh start
cd ${current_path}
sleep 3

for module in $(ls -l|grep ^d|awk '{print $9}');
do
    # every directory is a single module
    if [[ ${module} != "lib" &&  ${module} != "zookeeper" ]];then
        cd ${module};
        ./${module}.sh start;
    fi

    cd ${current_path}
done

