#!/bin/bash

mvn exec:java -Dexec.mainClass="co.j6mes.infra.srf.proxy.SRFProxy" -Dexec.args="c2 maps 10080"

