# movie-server
一个自建视频网站的工具，此项目为后端项目，基于springboot构建。前端项目参见[GCS-ZHN/movie-web](https://github.com/GCS-ZHN/movie-web)。为了部署方便，前端静态资源已经构建打包在本项目中。支持使用百度网盘作为数据后端。

## 适合场景
视频源在国外等低速访问网站，而本地缓存空间不足或本地观看不便携带。又因为一些资源的私密性等原因不想传到百度云等公共云盘上。

## 依赖
- java 语言依赖，刚需
- ffmpeg 用于转码脚本，非刚需

## 使用方法
### 本地资源后端
用户可以使用本地资源作为数据后端，使用方法如下
```bash
java -jar movie-XXX.jar \
--passwd=生成的SHA256加密密码 \
--resource.path=资源的绝对路径目录 \
--resource.backend=local \
--resource.type=m3u8,mp4
```
上述两个参数为可选参数，指定密码时需要安全验证。不指定资源路径时，默认为程序运行时路径为根目录。密码需要通过下列方式生成

```bash
java -jar movie.jar --generatePasswd=你的密码明文

Password is SHA256:KFj5z6JkQNREqA==:b06mKeu9sJIR***3zn6yqaZHRzjuG6UWBo=
```
然后再次启动程序，指定密码。

页面效果如下：
![home](fig/home.png)

![login](fig/login.png)

docker-compose部署如下：
```yml
version: '3'
services:
  movie:
    image: openjdk:11
    container_name: movie
    volumes:
      - /your/movie/resource/directory:/movie
      - /movie/jar/file/directory:/app
    ports:
      - 8080:8080
    restart: always
    working_dir: /app
    user: 1000:1000
    command: |
      java -jar movie.jar
      --passwd=<generated passwd SHA string>
      --resource.path=/movie
      --server.port=8080
      --server.address=0.0.0.0
```

### 百度网盘后端
视频是一个占内存的资源，没有充足容量的情况下，可以考虑将m3u8 HLS视频格式上传到百度网盘，
本应用支持设置百度网盘后端。首先根据[百度官方文档](https://pan.baidu.com/union/doc/fl0hhnulu)创建自己的百度网盘第三方应用，获得AppKey等信息。然后如下配置启动参数。
```bash
java -jar movie-XXX.jar \
--resource.path=/ \
--resource.backend=baidupan \
--resource.type=m3u8 \
--baidupan.client_id=你申请到的AppKey \
--baidupan.client_secret=您申请到的AppSecret \
--baidupan.redirect_url=http://你的主机域名:8080/auth
```
为了安全，百度要求上述redirect_url需要在百度上进行登记，参见[文档](https://pan.baidu.com/union/doc/Vl19c4jnx)。
然后就能正常使用了，页面会自动跳转要求登录百度网盘（理论上可以将地址贡献给别人，大家各自登录自己账号，如果有带宽够的服务器资源的话）。

**注意**：百度网盘对大文件不友好，因此暂时只能支持m3u8视频流，不能使用mp4。

## 支持的视频
- mp4
- m3u8 HLS格式

## 视频转码脚本解释
[video-utils.py](video-utils.py)脚本
用于将mp4格式与m3u8 HLS格式的互相转换。
后者更加适合大视频的观看。
因为播放mp4时，需要等服务器先将视频全部加载到服务器内存中。大容量的mp4此时预先加载较慢。
```shell
python video-utils.py . # 将当前目录的mp4转换为hls
python video-utils.py . -Rr # 递归将当前目录及子目录的mp4转换为hls，并删除mp4文件
python video-utils.py . -t m3u8 # 将hls转换为mp4
python video-utils.py v.mp4 -o v/index.m3u8 # 将指定m mp4
python video-utils.y input.hls/index.m3u8 # 输出为input.mp4
```

```shell
bash ts2mp4.sh 你的ts格式文件路径
```

## m3u8格式视频下载
欢迎访问[GCS-ZHN/m3u8Downloader](https://github.com/GCS-ZHN/m3u8Downloader)

