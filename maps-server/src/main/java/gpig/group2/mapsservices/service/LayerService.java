package gpig.group2.mapsservices.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.LineString;
import org.geojson.LngLatAlt;
import org.geojson.Point;
import org.springframework.stereotype.Service;

import gpig.group2.maps.geographic.position.CoordinateList;
import gpig.group2.maps.platform.Drone;
import gpig.group2.mapsservices.model.Layer;
import gpig.group2.mapsservices.model.LayerId;
import gpig.group2.model.sensor.OccupiedBuilding;
import gpig.group2.model.sensor.StrandedPerson;

@Service
public class LayerService {

	private static final String ESTIMATED_OCCUPANCY = "estimated-occupancy";
	private static final String TIME_IDENTIFIED = "time-identified";
	private static final String ESTIMATED_NUMBER = "estimated-number";
	private static final String WAYPOINTS = "waypoints";
	private static final String HISTORY = "history";
	private static final String CURRENT_LOCATION = "current-location";
	private static final String TYPE = "type";

	private Map<Integer, Drone> drones = new HashMap<>();
	private Set<StrandedPerson> strandedPersons = new HashSet<>();
	private Set<OccupiedBuilding> occupiedBuildings = new HashSet<>();

	public Collection<Layer> getLayers() {

		Collection<Layer> layers = new ArrayList<>(LayerId.values().length);

		for (LayerId layerId : LayerId.values()) {
			layers.add(layerId.toLayer());
		}

		return layers;
	}

	public FeatureCollection getLayer(LayerId layerId) {

		switch (layerId) {
			case BUILDING_OCCUPANCY:
				return getBuildingOccupancy();
			case DRONE_LOCATION:
				return getDroneLocations();
			case STRANDED_PERSONS:
				return getStrandedPersons();
			default:
				return null;
		}
	}

	private synchronized FeatureCollection getBuildingOccupancy() {

		FeatureCollection fc = new FeatureCollection();

		for (OccupiedBuilding occupiedBuilding : occupiedBuildings) {
			Feature spFeature = new Feature();

			Point spPoint = new Point();
			spPoint.setCoordinates(convertPointToPoint(occupiedBuilding.getLocation()).getCoordinates());
			spFeature.setGeometry(spPoint);

			spFeature.setProperty(ESTIMATED_OCCUPANCY, occupiedBuilding.getEstimatedOccupancy());
			spFeature.setProperty(TIME_IDENTIFIED, occupiedBuilding.getTimeIdentified().toString());

			fc.add(spFeature);
		}

		return fc;
	}

	private synchronized FeatureCollection getStrandedPersons() {

		FeatureCollection fc = new FeatureCollection();

		for (StrandedPerson strandedPerson : strandedPersons) {
			Feature spFeature = new Feature();

			Point spPoint = new Point();
			spPoint.setCoordinates(convertPointToPoint(strandedPerson.getLocation()).getCoordinates());
			spFeature.setGeometry(spPoint);

			spFeature.setProperty(ESTIMATED_NUMBER, strandedPerson.getEstimatedNumber());
			spFeature.setProperty(TIME_IDENTIFIED, strandedPerson.getTimeIdentified().toString());

			fc.add(spFeature);
		}

		return fc;
	}

	private synchronized FeatureCollection getDroneLocations() {

		FeatureCollection fc = new FeatureCollection();

		for (Drone drone : drones.values()) {
			Feature currLoc = getDroneLocationAsFeature(drone);
			fc.add(currLoc);

			Feature history = getDroneHistoryAsFeature(drone);
			fc.add(history);

			Feature waypoints = getDroneWaypointsAsFeature(drone);
			fc.add(waypoints);
		}

		return fc;
	}

	private Feature getDroneWaypointsAsFeature(Drone drone) {

		Feature wayPointsFeature = convertCoordListToLineString(drone.getWaypoints());
		wayPointsFeature.setProperty(TYPE, WAYPOINTS);
		return wayPointsFeature;
	}

	private Feature getDroneHistoryAsFeature(Drone drone) {

		Feature historyFeature = convertCoordListToLineString(drone.getHistory());
		historyFeature.setProperty(TYPE, HISTORY);
		return historyFeature;
	}

	private Feature convertCoordListToLineString(CoordinateList coordList) {

		Feature feature = new Feature();
		LineString linestring = new LineString();
		feature.setGeometry(linestring);

		for (gpig.group2.maps.geographic.Point point : coordList.getCoordinates()) {
			Point gjPoint = convertPointToPoint(point);
			linestring.add(gjPoint.getCoordinates());
		}

		return feature;
	}

	private Feature getDroneLocationAsFeature(Drone drone) {

		Point currLocPoint = convertPointToPoint(drone.getPosition());

		Feature currLocFeature = new Feature();
		currLocFeature.setGeometry(currLocPoint);
		currLocFeature.setProperty(TYPE, CURRENT_LOCATION);
		return currLocFeature;
	}

	private Point convertPointToPoint(gpig.group2.maps.geographic.Point point) {

		float latitude = point.getLatitude();
		float longitude = point.getLongitude();

		Point newPoint = new Point();
		LngLatAlt coordinates = new LngLatAlt();
		coordinates.setLatitude(latitude);
		coordinates.setLongitude(longitude);
		newPoint.setCoordinates(coordinates);

		return newPoint;
	}

	public synchronized void addOrUpdateDrone(Integer droneId, gpig.group2.maps.geographic.Point dronePosition,
			CoordinateList droneWaypoints) {
		
		Drone drone = null;
		if (drones.containsKey(droneId)) {
			drone = drones.get(droneId);
		}
		else {
			drone = new Drone();
			drones.put(droneId, drone);
			
			drone.setId(droneId);
		}
		
		drone.updateLocation(dronePosition);
		drone.setWaypoints(droneWaypoints);
	}
}
