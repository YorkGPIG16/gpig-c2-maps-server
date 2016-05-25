package gpig.group2.mapsservices.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import gpig.group2.maps.geographic.position.BoundingBox;
import gpig.group2.mapsservices.service.LayerService;
import gpig.group2.models.drone.request.RequestMessage;
import gpig.group2.models.drone.request.Task;
import gpig.group2.models.drone.request.task.AerialSurveyTask;

@Controller
@RequestMapping("/deployAreas")
public class DeploymentAreasController {

	@Autowired
	private LayerService layerService;

	@RequestMapping(produces = "application/xml", method = RequestMethod.GET)
	@ResponseBody
	public RequestMessage getDeploymentAreas() {

		RequestMessage rm = new RequestMessage();
		List<Task> tasks = new ArrayList<>();
		rm.setTasks(tasks);

		for (BoundingBox da : layerService.getDeploymentAreas()) {
			AerialSurveyTask ast = new AerialSurveyTask();
			ast.setLocation(da);
			tasks.add(ast);
		}

		return rm;
	}
}
