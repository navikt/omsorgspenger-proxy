#!/usr/bin/env sh

if test -d /app/init-scripts;
then
    for FILE in /app/init-scripts/*.sh
    do
        echo Sourcing $FILE
        . $FILE
    done
else
    echo "/app/init-scripts does not exist, skipping startup scripts"
fi

exec /app/run-java.sh $@
