#!/bin/bash

hostport="localhost:8080"
vpnuri="SEMP/v2/config/msgVpns/default"
user=admin
pass=admin

curl -X POST -u "$user:$pass" -H "content-type: application/json" \
	http://$hostport/$vpnuri/queueTemplates \
	-d '{
            "queueTemplateName":"cgtemplate",
            "queueNameFilter":"cgtest/>",
            "maxMsgSpoolUsage":15,
            "permission":"consume",
            "maxBindCount":3,
            "maxMsgSize":10000,
            "maxRedeliveryCount":5,
            "maxDeliveredUnackedMsgsPerFlow":10000,
            "respectMsgPriorityEnabled":true,
            "respectTtlEnabled":true,
            "maxTtl":0,
            "eventBindCountThreshold":{
                "clearValue":1,
                "setValue":3
            },
            "eventMsgSpoolUsageThreshold":{
                "clearPercent":20,
                "setPercent":40
            }
        }'
