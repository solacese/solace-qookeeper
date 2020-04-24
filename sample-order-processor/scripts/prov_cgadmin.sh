#!/bin/bash

hostport="localhost:8080"
vpnuri="SEMP/v2/config/msgVpns/default"
user=admin
pass=admin

curl -X POST -u "$user:$pass" -H "content-type: application/json" \
	http://$hostport/$vpnuri/clientProfiles \
-d '{
        "clientProfileName":"cgadmin",
        "allowGuaranteedEndpointCreateDurability":"all",
        "allowGuaranteedEndpointCreateEnabled":true,
        "allowGuaranteedMsgReceiveEnabled":true,
        "allowGuaranteedMsgSendEnabled":true,
        "allowSharedSubscriptionsEnabled":true,
        "allowTransactedSessionsEnabled":true,
        "maxConnectionCountPerClientUsername":5,
        "maxEgressFlowCount":10,
        "maxEndpointCountPerClientUsername":1000,
        "maxIngressFlowCount":10,
        "maxSubscriptionCount":500000,
        "maxTransactedSessionCount":10,
        "maxTransactionCount":5000,
        "rejectMsgToSenderOnNoSubscriptionMatchEnabled":true,
        "tlsAllowDowngradeToPlainTextEnabled":true
    }'

curl -X POST -u "$user:$pass" -H "content-type: application/json" \
	http://$hostport/$vpnuri/clientUsernames \
-d '{
        "clientUsername":"cgadmin",
        "aclProfileName":"default",
        "clientProfileName":"cgadmin",
        "enabled":true,
        "guaranteedEndpointPermissionOverrideEnabled":true,
        "subscriptionManagerEnabled":true
    }
