#!/bin/bash
mvn exec:java -Dexec.mainClass=com.orbekk.same.App -Dcom.orbekk.same.config.file="$1"
