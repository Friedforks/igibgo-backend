docker build -t igibgo-backend:v2 .
docker tag igibgo-backend:v2 registry.cn-hangzhou.aliyuncs.com/friedforks/igibgo-backend:v2
docker push registry.cn-hangzhou.aliyuncs.com/friedforks/igibgo-backend:v2