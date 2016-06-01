package gpig.group2.mapsservices.model;

import java.util.HashMap;
import java.util.Map;

public enum LayerId {
	DRONE_LOCATION(1, "Drone locations"), STRANDED_PERSONS(2, "Stranded persons"), BUILDING_OCCUPANCY(3,
			"Building occupancy"), WATER_EDGE(4, "Water's edge"),
	STRANDED_PERSONS_EXTERNAL(5,"Interop Stranded Persons"), RESPONDER(6,"Responder");

	private int id;
	private String name;
	private static Map<Integer, LayerId> mappings;

	static {
		mappings = new HashMap<>();

		for (LayerId layerId : LayerId.values()) {
			mappings.put(layerId.getId(), layerId);
		}
	}

	public int getId() {

		return id;
	}

	public Layer toLayer() {

		return new Layer(id, name);
	}

	public static LayerId getLayerIdFromIntId(int id) {

		return mappings.get(id);
	}

	private LayerId(int id, String name) {
		this.id = id;
		this.name = name;
	}
}