#!/bin/bash
keytool -genkey -alias weevent -storetype PKCS12 -keyalg RSA -keysize 2048 -keystore server.p12 -validity 3650