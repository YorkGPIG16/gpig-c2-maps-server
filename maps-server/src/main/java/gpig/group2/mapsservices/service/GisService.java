package gpig.group2.mapsservices.service;

import java.util.*;

import gpig.group2.models.drone.request.task.GoToLocationTask;
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
import gpig.group2.models.drone.request.Task;

@Service
public class GisService {

	private static final String ESTIMATED_OCCUPANCY = "estimated-occupancy";
	private static final String TIME_IDENTIFIED = "time-identified";
	private static final String WAYPOINTS = "waypoints";
	private static final String HISTORY = "history";
	private static final String CURRENT_LOCATION = "current-location";
	private static final String TYPE = "type";

	private int deploymentAreaCntr = 1;
	private Map<Integer, Drone> drones = new HashMap<>();
	private Map<Integer, Task> deploymentAreas = new HashMap<>();
	private Set<StrandedPerson> strandedPersons = new HashSet<>();
	private Set<OccupiedBuilding> occupiedBuildings = new HashSet<>();
	private Map<Integer, List<Feature>> floodRiskAreas = new HashMap<>();
	private FeatureCollection waterEdge;

	private Integer lastStrandedPersonId = 0;
	private FeatureCollection externalSrandedPersons = new FeatureCollection();

	public FeatureCollection getWaterEdge() {

		if (this.waterEdge == null) {
			return new FeatureCollection();
		} else {
			return waterEdge;
		}
	}

	public void setWaterEdge(FeatureCollection waterEdge) {

		this.waterEdge = waterEdge;
	}

	public synchronized void addOccupiedBuilding(OccupiedBuilding ob) {
		lastStrandedPersonId += 1;
		ob.setId(lastStrandedPersonId);
		occupiedBuildings.add(ob);
	}

	public synchronized void addStrandedPerson(StrandedPerson p) {

		lastStrandedPersonId += 1;
		p.setId(lastStrandedPersonId);

		strandedPersons.add(p);
	}

	public synchronized void clearStrandedPerson(Integer id) {

		StrandedPerson found = null;
		for (StrandedPerson person : strandedPersons) {
			if (person.getId() == id) {
				found = person;
			}
		}

		if (found != null) {
			strandedPersons.remove(found);
		}

		OccupiedBuilding fb = null;
		for (OccupiedBuilding person : occupiedBuildings) {
			if (person.getId() == id) {
				fb = person;
			}
		}

		if (fb != null) {
			occupiedBuildings.remove(fb);
		}

	}

	public void setPersonTask(Integer pid, Integer tid) {

		StrandedPerson found = null;
		for (StrandedPerson person : strandedPersons) {
			if (person.getId() == pid) {
				found = person;
			}
		}

		if (found != null) {
			found.setOwningTask(tid);
		}
	}

	public synchronized void addFloodRiskArea(int riskId, List<Feature> riskMap) {

		floodRiskAreas.put(riskId, riskMap);
	}

	public Set<StrandedPerson> getStrandedPersons() {

		return strandedPersons;
	}

	public Set<OccupiedBuilding> getOccupiedBuildings() {

		return occupiedBuildings;
	}

	public synchronized Map<Integer, Task> getDeploymentAreas() {

		return deploymentAreas;
	}

	public synchronized void newDeploymentArea(Task deploymentArea) {

		deploymentAreas.put(deploymentAreaCntr, deploymentArea);
		deploymentAreaCntr++;
	}

	public synchronized void newDeploymentAreaForPOI(Integer pid, Task deploymentArea) {

		deploymentAreas.put(deploymentAreaCntr, deploymentArea);
		setPersonTask(pid, deploymentAreaCntr);

		deploymentAreaCntr++;
	}

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
				return getStrandedPersonsGeoJson();
			case WATER_EDGE:
				return getWaterEdge();
			case STRANDED_PERSONS_EXTERNAL:
				return this.externalSrandedPersons;
			case RESPONDER:
				return this.responders;
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


			spFeature.setProperty("delete", "delete");
			spFeature.setProperty("id", occupiedBuilding.getId());

			spFeature.setProperty(ESTIMATED_OCCUPANCY, occupiedBuilding.getEstimatedOccupancy());
			spFeature.setProperty(TIME_IDENTIFIED, occupiedBuilding.getTimeIdentified().toString());

			fc.add(spFeature);
		}

		return fc;
	}

	private synchronized FeatureCollection getStrandedPersonsGeoJson() {

		FeatureCollection fc = new FeatureCollection();

		for (StrandedPerson strandedPerson : strandedPersons) {
			Feature spFeature = new Feature();
			spFeature.setProperty("task", strandedPerson.getOwningTask());
			spFeature.setProperty("id", strandedPerson.getId());
			spFeature.setProperty("delete", "delete");

			Point spPoint = new Point();
			spPoint.setCoordinates(convertPointToPoint(strandedPerson.getLocation()).getCoordinates());
			spFeature.setGeometry(spPoint);

			spFeature.setProperty(TIME_IDENTIFIED, strandedPerson.getTimeIdentified().toString());

			fc.add(spFeature);
		}

		return fc;
	}

	private synchronized FeatureCollection getDroneLocations() {

		FeatureCollection fc = new FeatureCollection();

		for (Drone drone : drones.values()) {
			Feature currLoc = getDroneLocationAsFeature(drone);
			Feature history = getDroneHistoryAsFeature(drone);
			Feature waypoints = getDroneWaypointsAsFeature(drone);

			addDroneIdToFeatures(drone, currLoc, history, waypoints);

			fc.add(currLoc);
			fc.add(history);
			fc.add(waypoints);
		}

		return fc;
	}

	private void addDroneIdToFeatures(Drone drone, Feature... features) {

		for (Feature feature : features) {
			feature.setProperty("drone-id", drone.getId());
		}
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

		double latitude = point.getLatitudeX();
		double longitude = point.getLongitudeX();

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
		} else {
			drone = new Drone();
			drones.put(droneId, drone);
			drone.setPosition(dronePosition);
			drone.setId(droneId);
		}

		if (!drone.getPosition().equals(dronePosition)) {
			drone.updateLocation(dronePosition);
		}

		if (droneWaypoints == null) {
			drone.setWaypoints(new CoordinateList());
		} else {
			drone.setWaypoints(droneWaypoints);
		}
	}

	public void completeTask(int taskId) {

		deploymentAreas.remove(taskId);
	}

	public void setExternalSrandedPersons(FeatureCollection externalSrandedPersons) {
		this.externalSrandedPersons = externalSrandedPersons;
	}

	public synchronized void rtb() {
		deploymentAreas.clear();
		GoToLocationTask glt = new GoToLocationTask();
		glt.setPoint(new gpig.group2.maps.geographic.Point(53.9467684,-1.0307718));
		glt.setPriority(9999);
		glt.setId(999);
		glt.setTimestamp(new Date());
		deploymentAreas.put(999,glt);
	}

	private FeatureCollection responders = new FeatureCollection();
	private Integer responderCount = 1;
	public void addResponder(Feature feature) {
		feature.setProperties(new HashMap<>());
		feature.setProperty("responderid",responderCount++);
		responders.add(feature);
	}

	public void deleteResponder(int id) {
		Feature found = null;
		for(Feature f : responders) {
			if (f.<Integer>getProperty("responderid").equals(id)) {
				found = f;
				break;
			}
		}

		if(found != null) {
			responders.getFeatures().remove(found);
		}
	}

}
