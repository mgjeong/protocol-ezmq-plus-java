#!/bin/sh

export LD_LIBRARY_PATH=../libs
cd samples

if [ "topicdiscovery" = $1 ]; then
    echo "start topicdiscovery: $2"
	java -jar ezmqx-topicdiscovery-sample.jar -t $2
elif [ "publisher" = $1 ]; then
	echo "start publisher: $2"
	java -jar ezmqx-publisher-sample.jar -t $2
elif [ "amlsubscriber" = $1 ]; then
    echo "start subscriber with topic: $2"
	java -jar ezmqx-amlsubscriber-sample.jar -t $2 -h true
elif [ "xmlsubscriber" = $1 ]; then
    echo "start subscriber with topic: $3"
	java -jar ezmqx-xmlsubscriber-sample.jar -t $2 -h true
else
	echo "Wrong arguments!!!"
	echo "Examples:"
	echo " publisher topic"
	echo " amlsubscriber topic"
	echo " xmlsubscriber topic"
	echo " topicdiscovery topic"
fi



