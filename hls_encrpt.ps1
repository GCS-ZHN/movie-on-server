ffmpeg -y `
-i "D:\Data\movie\s.mp4" `
-hls_time "10" `
-hls_key_info_file "D:\Data\coding\Java\movie-on-server\key\hls.keyinfo" `
-hls_playlist_type "vod" `
-hls_list_size "0" `
-hls_segment_filename "D:\Data\movie\encrpt.hls\v%04d.ts" `
"D:\Data\movie\playlist.m3u8"