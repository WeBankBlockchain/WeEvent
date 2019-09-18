#!/bin/bash
current_path=$(pwd)

if [[ -e ${current_path}/install-all.sh ]];then
   echo "Error operation "
   echo "Current path ${current_path} is source code package, only install path can execute start-all.sh "
   exit 1
fi

for module in $(ls -l|grep ^d|awk '{print $9}');
do
    # every directory is a single module
    cd ${module};
    ./${module}.sh start;
    cd ${current_path}
done

