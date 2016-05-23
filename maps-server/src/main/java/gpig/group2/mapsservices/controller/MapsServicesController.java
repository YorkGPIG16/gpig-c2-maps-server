package gpig.group2.mapsservices.controller;

import java.util.Collection;

import org.geojson.FeatureCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import gpig.group2.mapsservices.model.Layer;
import gpig.group2.mapsservices.model.LayerId;
import gpig.group2.mapsservices.service.LayerService;

@Controller
@RequestMapping("/layers")
public class MapsServicesController {

	@Autowired
	LayerService layerService;

	@RequestMapping(value = "/{layerIdInt}", produces = "application/json", method = RequestMethod.GET)
	@ResponseBody
	public FeatureCollection getLayer(@PathVariable int layerIdInt) {

		LayerId layerId = LayerId.getLayerIdFromIntId(layerIdInt);
		return layerService.getLayer(layerId);
	}

	@RequestMapping(value = "", produces = "application/json", method = RequestMethod.GET)
	@ResponseBody
	public Collection<Layer> getLayers() {

		return layerService.getLayers();
	}
}
