#!/bin/bash
ngoyVersion={{ngoyVersion}}
ngoyPath=build/tmp/ngoy-$ngoyVersion

if [ ! -e $ngoyPath ]; then
	echo "Extracting ngoy binaries..."
    gradle extractNgoy
fi
java -cp "$ngoyPath/*" ngoy.Ngoy $@
