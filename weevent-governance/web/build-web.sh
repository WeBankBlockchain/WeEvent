#!/bin/bash

npm -v 2>&1 >/dev/null
if [[ $? -ne 0 ]];then
    exit 1
fi

npm install
npm run build
