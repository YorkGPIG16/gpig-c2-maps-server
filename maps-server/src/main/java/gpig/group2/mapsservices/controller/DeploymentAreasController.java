package gpig.group2.mapsservices.controller;

import java.util.ArrayList;
import java.util.List;

import org.geojson.Feature;
import org.geojson.FeatureCollection;
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
import gpig.group2.mapsservices.service.GisService;
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
	private GisService gisService;

	@RequestMapping(produces = "application/xml", method = RequestMethod.GET)
	@ResponseBody
	public RequestMessage getDeploymentAreas() {

		RequestMessage rm = new RequestMessage();
		List<Task> tasks = new ArrayList<>();
		rm.setTasks(tasks);

		for (int daId : gisService.getDeploymentAreas().keySet()) {
			BoundingBox da = gisService.getDeploymentAreas().get(daId);
			AerialSurveyTask ast = new AerialSurveyTask();
			ast.setLocation(da);
			ast.setId(daId);
			tasks.add(ast);
		}

		return rm;
	}
	
	@RequestMapping(value = "/forMap", produces = "application/json", method = RequestMethod.GET)
	@ResponseBody
	public FeatureCollection getDeploymentAreasForMap() {

		FeatureCollection fc = new FeatureCollection();
		for (BoundingBox da : gisService.getDeploymentAreas().values()) {
			Feature f = new Feature();
			Polygon poly = boundingBoxToPolygon(da);
			f.setGeometry(poly);
			fc.add(f);
		}
		return fc;
	}

	private Polygon boundingBoxToPolygon(BoundingBox da) {

		Point br = da.getBottomRightX();
		Point tl = da.getTopLeftX();

		double brLat = br.getLatitudeX(); // X
		double brLon = br.getLongitudeX(); // Y
		double tlLat = tl.getLatitudeX(); // X
		double tlLon = tl.getLongitudeX(); // Y
		
		LngLatAlt tlLLA = new LngLatAlt(tlLat, tlLon);
		LngLatAlt trLLA = new LngLatAlt(brLat, tlLon);
		LngLatAlt brLLA = new LngLatAlt(brLat, brLon);
		LngLatAlt blLLA = new LngLatAlt(tlLat, brLon);
		
		Polygon poly = new Polygon(tlLLA, trLLA, brLLA, blLLA, trLLA); // Have to repeat the last coord
		return poly;
	}

	@RequestMapping(value = "/create", consumes = "application/json", method = RequestMethod.POST)
	@ResponseBody
	public String createDeploymentArea(@RequestBody Feature daFeature) {

		GeoJsonObject daGjo = daFeature.getGeometry();
		if (daGjo instanceof Polygon) {
			BoundingBox deploymentArea = getBoundingBoxFromPolygon(daGjo);

			gisService.newDeploymentArea(deploymentArea);

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
		Point tlPoint = new Point((double) tlCoord.getLatitude(), (double) tlCoord.getLongitude());
		Point brPoint = new Point((double) brCoord.getLatitude(), (double) brCoord.getLongitude());
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
