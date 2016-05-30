package gpig.group2.mapsservices.controller;

import gpig.group2.model.collection.StrandedPersonCollection;
import gpig.group2.model.sensor.StrandedPerson;
import org.geojson.FeatureCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import gpig.group2.mapsservices.service.GisService;

import java.util.Set;

@Controller
@RequestMapping("/floodRisk")
public class FloodRiskController {

	@Autowired
	private GisService gisService;

	@RequestMapping(value = "/risk/{riskId}", consumes = "application/json", method = RequestMethod.POST)
	@ResponseBody
	public String pushDrone(@PathVariable int riskId, @RequestBody FeatureCollection floodMap) {

		gisService.addFloodRiskArea(riskId, floodMap.getFeatures());
		return "Accepted";
	}



	@RequestMapping(value = "/strandedPersons", produces = "application/xml", method = RequestMethod.GET)
	@ResponseBody
	public StrandedPersonCollection getStrandedPersons() {

		StrandedPersonCollection c = new StrandedPersonCollection();
		c.strandedPersons = gisService.getStrandedPersons();
		return c;
	}

}
