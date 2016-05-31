package gpig.group2.mapsservices.controller;

import java.util.HashSet;
import java.util.List;

import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.GeoJsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import gpig.all.schema.Coord;
import gpig.all.schema.GISPosition;
import gpig.all.schema.GPIGData;
import gpig.all.schema.Image;
import gpig.all.schema.Point;
import gpig.all.schema.Timestamp;
import gpig.group2.mapsservices.service.GisService;
import gpig.group2.model.sensor.OccupiedBuilding;
import gpig.group2.model.sensor.StrandedPerson;

@Controller
@RequestMapping("/interop")
public class InteropController {

	@Autowired
	private GisService gisService;

	@RequestMapping(value = "waterEdge", produces = "application/json", method = RequestMethod.POST)
	public String pushWaterEdgeData(@RequestBody GeoJsonObject gjo) {

		FeatureCollection fc;
		if (gjo instanceof FeatureCollection) {
			fc = (FeatureCollection) gjo;
		} else {
			return "Not a FeatureCollection";
		}
		
		gisService.setWaterEdge(fc);
		
		return "Accepted";
	}



	@RequestMapping(value = "strandedPerson", produces = "application/json", method = RequestMethod.POST)
	public String pushStrandedPersons(@RequestBody GeoJsonObject gjo) {

		FeatureCollection fc;
		if (gjo instanceof FeatureCollection) {
			fc = (FeatureCollection) gjo;
		} else {
			return "Not a FeatureCollection";
		}

		gisService.setExternalSrandedPersons(fc);

		return "Accepted";
	}




	@RequestMapping(produces = "application/xml", method = RequestMethod.GET)
	@ResponseBody
	public GPIGData getInteropData() {

		GPIGData gpigData = new GPIGData();
		gpigData.positions = new HashSet<>();

		for (StrandedPerson sp : gisService.getStrandedPersons()) {
			GISPosition gp = new GISPosition();

			gp.position = latLonToPoint(sp.getLocation().getLatitudeX(), sp.getLocation().getLongitudeX());

			gp.timestamp = new Timestamp();
			gp.timestamp.date = sp.getTimeIdentified().toDate();

			gpig.all.schema.datatypes.StrandedPerson gpigSp = new gpig.all.schema.datatypes.StrandedPerson();
			gpigSp.image = new Image();
			gpigSp.image.url = sp.getImageUrl();
			gpigData.positions.add(gp);
			gp.payload = gpigSp;
		}

		// Treat occupied buildings as stranded people
		for (OccupiedBuilding ob : gisService.getOccupiedBuildings()) {
			GISPosition gp = new GISPosition();
			gp.position = latLonToPoint(ob.getLocation().getLatitudeX(), ob.getLocation().getLongitudeX());

			gp.timestamp = new Timestamp();
			gp.timestamp.date = ob.getTimeIdentified().toDate();

			gpig.all.schema.datatypes.StrandedPerson gpigSp = new gpig.all.schema.datatypes.StrandedPerson();
			gpigData.positions.add(gp);
			gp.payload = gpigSp;
		}

		return gpigData;
	}

	private Point latLonToPoint(Double lat, Double lon) {

		Point point = new Point();
		point.coord = new Coord();
		point.coord.latitude = (float) (double) lat;
		point.coord.longitude = (float) (double) lon;
		return point;
	}
}
