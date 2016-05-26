package gpig.group2.mapsservices.controller;

import java.util.ArrayList;
import java.util.List;

import org.geojson.Feature;
import org.geojson.GeoJsonObject;
import org.geojson.LngLatAlt;
import org.geojson.Polygon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import gpig.group2.maps.geographic.Point;
import gpig.group2.maps.geographic.position.BoundingBox;
import gpig.group2.mapsservices.service.LayerService;
import gpig.group2.models.drone.request.RequestMessage;
import gpig.group2.models.drone.request.Task;
import gpig.group2.models.drone.request.task.AerialSurveyTask;
import gpig.group2.models.drone.response.responsedatatype.Completed;

@Controller
@RequestMapping("/deployAreas")
public class DeploymentAreasController {

	private static final int TL_COORD_LOC = 0;
	private static final int BR_COORD_LOC = 2;
	@Autowired
	private LayerService layerService;

	@RequestMapping(produces = "application/xml", method = RequestMethod.GET)
	@ResponseBody
	public RequestMessage getDeploymentAreas() {

		RequestMessage rm = new RequestMessage();
		List<Task> tasks = new ArrayList<>();
		rm.setTasks(tasks);

		for (int daId : layerService.getDeploymentAreas().keySet()) {
			BoundingBox da = layerService.getDeploymentAreas().get(daId);
			AerialSurveyTask ast = new AerialSurveyTask();
			ast.setLocation(da);
			ast.setId(daId);
			tasks.add(ast);
		}

		return rm;
	}

	@RequestMapping(value = "/create", consumes = "application/json", method = RequestMethod.POST)
	@ResponseBody
	public String createDeploymentArea(@RequestBody Feature daFeature) {

		GeoJsonObject daGjo = daFeature.getGeometry();
		if (daGjo instanceof Polygon) {
			BoundingBox deploymentArea = getBoundingBoxFromPolygon(daGjo);

			layerService.newDeploymentArea(deploymentArea);

			return "Accepted";
		} else {
			return "Not a polygon";
		}
	}

	private BoundingBox getBoundingBoxFromPolygon(GeoJsonObject daGjo) {

		Polygon daPoly = (Polygon) daGjo;
		List<LngLatAlt> coords = daPoly.getCoordinates().get(0);
		LngLatAlt tlCoord = coords.get(TL_COORD_LOC);
		LngLatAlt brCoord = coords.get(BR_COORD_LOC);
		Point tlPoint = new Point((float) tlCoord.getLatitude(), (float) tlCoord.getLongitude());
		Point brPoint = new Point((float) brCoord.getLatitude(), (float) brCoord.getLongitude());
		BoundingBox deploymentArea = new BoundingBox(tlPoint, brPoint);
		return deploymentArea;
	}

	@RequestMapping(value = "/complete", produces = "application/xml", method = RequestMethod.POST)
	@ResponseBody
	public String completeDeploymentArea(Completed completed) {

		completed.getTaskIdX();// DO this
		return "Accepted";
	}
}
