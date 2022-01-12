#!/usr/bin/env sh

exec /blackbox.amd64 | java -Xms512m -Xmx1024m -cp /app/app.jar WordCount