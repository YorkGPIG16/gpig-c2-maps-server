package gpig.group2.mapsservices.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.GeoJsonObject;
import org.geojson.LngLatAlt;
import org.geojson.Polygon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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
			Task ast = gisService.getDeploymentAreas().get(daId);
			ast.setId(daId);
			tasks.add(ast);
		}

		return rm;
	}
	
	@RequestMapping(value = "/forMap", produces = "application/json", method = RequestMethod.GET)
	@ResponseBody
	public FeatureCollection getDeploymentAreasForMap() {

		FeatureCollection fc = new FeatureCollection();
		for (Task da : gisService.getDeploymentAreas().values()) {
			if(da instanceof AerialSurveyTask) {
				Feature f = new Feature();

				f.setProperty("value",da.getPriorityX());

				Polygon poly = boundingBoxToPolygon(((AerialSurveyTask) da).getLocationX());
				f.setGeometry(poly);
				fc.add(f);
			}

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
		
		LngLatAlt tlLLA = new LngLatAlt(tlLon, tlLat);
		LngLatAlt trLLA = new LngLatAlt(brLon, tlLat);
		LngLatAlt brLLA = new LngLatAlt(brLon, brLat);
		LngLatAlt blLLA = new LngLatAlt(tlLon, brLat);
		
		Polygon poly = new Polygon(tlLLA, trLLA, brLLA, blLLA, tlLLA); // Have to repeat the last coord
		return poly;
	}

	@RequestMapping(value = "/create", consumes = "application/json", method = RequestMethod.POST)
	@ResponseBody
	public String createDeploymentArea(@RequestBody Feature daFeature) {

		Double prior = 500.0;

		GeoJsonObject daGjo = daFeature.getGeometry();

		if(daFeature.getProperties()!=null) {
			if (daFeature.getProperty("value") != null) {
				prior = daFeature.getProperty("value");
			}

		}

		if (daGjo instanceof Polygon) {
			BoundingBox deploymentArea = getBoundingBoxFromPolygon(daGjo);

			AerialSurveyTask ast = new AerialSurveyTask();
			ast.setLocation(deploymentArea);
			ast.setTimestamp(new Date());
			ast.setPriority(prior.intValue());

			gisService.newDeploymentArea(ast);

			return "Accepted";
		} else {
			return "Not a polygon";
		}
	}



	@RequestMapping(value = "/createForPOI/{poiid}", consumes = "application/json", method = RequestMethod.POST)
	@ResponseBody
	public String createDeploymentAreaPOI(@PathVariable Integer poiid, @RequestBody Feature daFeature) {



		Double prior = 500.0;

		GeoJsonObject daGjo = daFeature.getGeometry();

		if(daFeature.getProperty("value") != null) {
			prior = daFeature.getProperty("value");
		}


		if (daGjo instanceof Polygon) {
			BoundingBox deploymentArea = getBoundingBoxFromPolygon(daGjo);

			AerialSurveyTask ast = new AerialSurveyTask();
			ast.setLocation(deploymentArea);
			ast.setTimestamp(new Date());
			ast.setPriority(prior.intValue());


			gisService.newDeploymentAreaForPOI(poiid,ast);

			return "Accepted";
		} else {
			return "Not a polygon";
		}


	}


	private BoundingBox getBoundingBoxFromPolygon(GeoJsonObject daGjo) {

		Polygon daPoly = (Polygon) daGjo;
		List<LngLatAlt> coords = daPoly.getCoordinates().get(0);

		Double minLat,minLong,maxLat,maxLong;
		minLong = maxLong = coords.get(0).getLongitude();
		minLat = maxLat = coords.get(0).getLatitude();

		for(LngLatAlt lla : coords ) {

			if(lla.getLatitude()> maxLat) {
				maxLat = lla.getLatitude();
			}

			if(lla.getLongitude()> maxLong) {
				maxLong = lla.getLongitude();
			}

			if(lla.getLatitude()< minLat) {
				minLat = lla.getLatitude();
			}

			if(lla.getLongitude() < minLong) {
				minLong = lla.getLongitude();
			}

		}



		Point tlPoint = new Point(maxLat, minLong);
		Point brPoint = new Point(minLat, maxLong);
		BoundingBox deploymentArea = new BoundingBox(tlPoint, brPoint);
		return deploymentArea;
	}

	@RequestMapping(value = "/complete", produces = "application/xml", method = RequestMethod.POST)
	@ResponseBody
	public String completeDeploymentArea(@RequestBody Completed completed) {

		gisService.completeTask(completed.getTaskIdX());
		return "Accepted";
	}
}
