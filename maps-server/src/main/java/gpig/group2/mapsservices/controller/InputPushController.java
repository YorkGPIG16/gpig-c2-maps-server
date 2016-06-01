package gpig.group2.mapsservices.controller;

import org.geojson.Feature;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import gpig.group2.mapsservices.service.GisService;
import gpig.group2.model.sensor.OccupiedBuilding;
import gpig.group2.model.sensor.StrandedPerson;
import gpig.group2.models.drone.response.responsedatatype.BuildingOccupancyResponse;
import gpig.group2.models.drone.status.DroneStatusMessage;

@Controller
@RequestMapping("/push")
public class InputPushController {

	@Autowired
	private GisService gisService;

	@RequestMapping(value = "/droneStatus", consumes = "application/xml", method = RequestMethod.POST)
	@ResponseBody
	public String pushDrone(@RequestBody DroneStatusMessage statusMsg) {

		gisService.addOrUpdateDrone(statusMsg.getIdX(), statusMsg.getPositionX(), statusMsg.getWaypointsX());
		return "Accepted";
	}

	@RequestMapping(value = "/strandedPerson", consumes = "application/xml", method = RequestMethod.POST)
	@ResponseBody
	public String pushStrandedPerson(@RequestBody StrandedPerson sp) {

		gisService.addStrandedPerson(sp);
		return "Accepted";
	}

	@RequestMapping(value = "/strandedPerson/{pid}", method = RequestMethod.DELETE)
	@ResponseBody
	public String pushStrandedPersonDelete(@PathVariable Integer pid) {

		gisService.clearStrandedPerson(pid);
		return "Accepted";

	}

	@RequestMapping(value = "/rtb", method = RequestMethod.POST)
	@ResponseBody
	public String pushStrandedPersonDelete() {

		gisService.rtb();
		return "Accepted";

	}


	@RequestMapping(value = "/responder", consumes = "application/json", method = RequestMethod.POST)
	@ResponseBody
	public String pushResponder(@RequestBody Feature feature) {

		gisService.addResponder(feature);
		return "Accepted";

	}


	@RequestMapping(value = "/responder/{pid}", method = RequestMethod.DELETE)
	@ResponseBody
	public String pushDeleteResponder(@PathVariable Integer pid) {

		gisService.deleteResponder(pid);
		return "Accepted";

	}




	@RequestMapping(value = "/strandedPerson/{pid}/{tid}", consumes = "application/xml", method = RequestMethod.PUT)
	@ResponseBody
	public String pushStrandedPersonNewTask(@PathVariable Integer pid, @PathVariable Integer tid) {

		gisService.setPersonTask(pid,tid);
		return "Accepted";

	}



	@RequestMapping(value = "/buildingOccupancy", consumes = "application/xml", method = RequestMethod.POST)
	@ResponseBody
	public String pushStrandedPeron(@RequestBody BuildingOccupancyResponse bor) {

		OccupiedBuilding ob = new OccupiedBuilding(bor.getOriginX(), bor.getEstimatedNumberOfPeopleX(), new DateTime(bor.getTimestampX()));
		gisService.addOccupiedBuilding(ob);
		return "Accepted";
	}


}
