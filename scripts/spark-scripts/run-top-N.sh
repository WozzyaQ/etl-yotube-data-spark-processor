if [ "$1" == "help" ]
then
    echo "usage ./run-top-N.sh <DATE> <NUMBER>"
    exit
fi
N=$2
DATE=$1

spark-submit --class org.ua.wozzya.analytics.Top100 spark.jar  "$DATE" "$N"
