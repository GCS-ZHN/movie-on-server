#!/usr/bin/env bash
path=$1
if [ "$path" = "" ];then
    path="."
fi
data=`ls $path/*.ts 2>/dev/null` 
for file in ${data[@]};do
    echo $file
    ffmpeg -i $file -codec copy -f mp4 ${file/.ts/.mp4}
done