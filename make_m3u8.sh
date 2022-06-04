#!/usr/bin/env bash
# ffmpeg -i input.mp4 -c copy  output.ts
function createM3U8 {
    file=$1
    if [ ! -f $file -a  ];then
        echo "Please specific an existed mp4/ts file" >&2
        return 1
    elif [[  $file == *.mp4 ]];then
        ffmpeg -i $file -c copy ${file/.mp4/.ts}
        path=${file/.mp4/}
    elif [[ $file == *.ts ]];then
        path=${/.ts/}
    else
        echo "Please specific an existed mp4/ts file" >&2
        return 1
    fi
    if [ ! -d $path.hls ];then
        mkdir -p $path.hls
    else
        echo "$path.hls existed!" >&2
        return 2
    fi
    ffmpeg -i $path.ts \
                -c copy \
                -map 0 \
                -f segment \
                -segment_list $path.hls/index.m3u8 \
                -segment_time 5 \
                $path.hls/v%04d.ts && rm -f $path.ts
    
}
if [ -d $1 ];then
    mp4s=`ls $1/*.mp4 2>/dev/null`
    for mp4 in ${mp4s[@]};do
        createM3U8 $mp4
    done
else
    createM3U8 $@
fi

