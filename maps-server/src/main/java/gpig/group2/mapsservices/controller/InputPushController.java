package gpig.group2.mapsservices.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import gpig.group2.mapsservices.service.LayerService;
import gpig.group2.models.drone.status.DroneStatusMessage;

@Controller
@RequestMapping("/push")
public class InputPushController {

	@Autowired
	LayerService layerService;

	@RequestMapping(value = "", consumes = "application/xml", method = RequestMethod.POST)
	@ResponseBody
	public String pushDrone(@RequestBody DroneStatusMessage statusMsg) {

		layerService.addOrUpdateDrone(statusMsg.getIdX(), statusMsg.getPositionX(), statusMsg.getWaypointsX());
		return "Accepted";
	}
}
