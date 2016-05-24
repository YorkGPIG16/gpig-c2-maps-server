package gpig.group2.mapsservices.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import gpig.group2.mapsservices.service.LayerService;
import gpig.group2.models.drone.status.DroneStatusMessage;

@Controller
@RequestMapping("/push")
public class InputPushController {

	@Autowired
	LayerService layerService;

	@RequestMapping(consumes = "application/xml", method = RequestMethod.POST)
	public void pushDrone(DroneStatusMessage statusMsg) {

		layerService.addOrUpdateDrone(statusMsg.getIdX(), statusMsg.getPositionX(), statusMsg.getWaypointsX());
	}
}
