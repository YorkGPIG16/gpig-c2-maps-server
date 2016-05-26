package gpig.group2.mapsservices.controller;

import org.geojson.FeatureCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import gpig.group2.mapsservices.service.LayerService;

@Controller
@RequestMapping("/floodRisk")
public class FloodRiskController {

	@Autowired
	private LayerService layerService;

	@RequestMapping(value = "/{riskId}", consumes = "application/json", method = RequestMethod.POST)
	@ResponseBody
	public String pushDrone(@PathVariable int riskId, @RequestBody FeatureCollection floodMap) {

		layerService.addFloodRiskArea(riskId, floodMap.getFeatures());
		return "Accepted";
	}
}
