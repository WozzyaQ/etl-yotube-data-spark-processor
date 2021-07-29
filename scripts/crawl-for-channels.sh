#!/usr/bin/bash
YOUTUBE_API_KEY=$1

read -p "Input bucket name" BUCKET_NAME
read -p "Input prefix" INPUT_PREFIX
read -p "Store bucket name" STORE_BUCKET_NAME
read -p "Store prefix" STORE_PREFIX
read -p "Task (v - for gathering video, c - for gathering comments)" OPTION


case "$OPTION" in
	v)
	TASK="gather-video"
	;;
	c)
	TASK="gather-comments"
	;;
	*)
	echo "No such an option $OPTION"
	exit
	;;
esac
	

PAYLOAD='{"input-bucket-name": "'"$BUCKET_NAME"'","input-prefix": "'"$INPUT_PREFIX"'", "store-bucket-name" : "'"$STORE_BUCKET_NAME"'","store-prefix" : "'"$STORE_PREFIX"'","youtube-api-key" : "'"$YOUTUBE_API_KEY"'",task: "'"$TASK"'"}'

aws invoke --function-name
