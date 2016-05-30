#!/bin/bash

MAPS="maps GPIGGroup2MapsServer/app/ 8080"
VUI="vui GPIGGroup2ImageVerificationUI/app/ 8080"
ALERTS="alerts GPIGGroup2UI/app/ 8080"

mvn exec:java -Dexec.mainClass="co.j6mes.infra.srf.proxy.WebServiceSRFProxy" -Dexec.args="c2 $MAPS $VUI $ALERTS"

