#!/bin/bash

MAPS="maps GPIGGroup2MapsServer/app/ 10080"
VUI="vui GPIGGroup2ImageVerUI/app/ 10080"
ALERTS="alerts GPIGGroup2UI/app/ 10080"

mvn exec:java -Dexec.mainClass="co.j6mes.infra.srf.proxy.WebServiceSRFProxy" -Dexec.args="c2 $MAPS $VUI $ALERTS"

