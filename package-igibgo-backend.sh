#!/bin/bash

version="v1.3.0"
#docker build -t igibgo-backend:$version .
docker tag igibgo-backend:$version registry.cn-hangzhou.aliyuncs.com/friedforks/igibgo-backend:$version
docker push registry.cn-hangzhou.aliyuncs.com/friedforks/igibgo-backend:$version