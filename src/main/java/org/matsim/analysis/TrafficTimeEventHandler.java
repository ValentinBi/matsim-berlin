/**
 * 
 */
package org.matsim.analysis;

import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.VehicleEntersTrafficEvent;
import org.matsim.api.core.v01.events.VehicleLeavesTrafficEvent;
import org.matsim.api.core.v01.events.handler.VehicleEntersTrafficEventHandler;
import org.matsim.api.core.v01.events.handler.VehicleLeavesTrafficEventHandler;
import org.matsim.api.core.v01.population.Person;


/**
 * @author Aravind
 *
 */

public class TrafficTimeEventHandler implements VehicleEntersTrafficEventHandler, VehicleLeavesTrafficEventHandler {

	private final Map<Id<Person>, VehicleEntersTrafficEvent> inTraffic = new HashMap<>();
	private final Map<Id<Person>, Double> timeInTraffic = new HashMap<>();

	Map<Id<Person>, Double> getTimeInTrafficByPerson() {
		return timeInTraffic;
	}
	
	double calculateTimeInTraffic() {
		return timeInTraffic.values().stream().mapToDouble(d -> d).sum();
	}

	@Override
	public void handleEvent(VehicleEntersTrafficEvent event) {

		if (isNotInteraction(event.getEventType()))
			inTraffic.put(event.getPersonId(), event);
	}

	@Override
	public void handleEvent(VehicleLeavesTrafficEvent event) {

		if (isNotInteraction(event.getEventType()) && inTraffic.containsKey(event.getPersonId())) {
			VehicleEntersTrafficEvent enterTraffic = inTraffic.remove(event.getPersonId());
			double timeSpentInTraffic = event.getTime() - enterTraffic.getTime();
			timeInTraffic.merge(event.getPersonId(), timeSpentInTraffic, Double::sum);
		}
	}

	private boolean isNotInteraction(String activityType) {
		return !activityType.contains(" interaction");
	}

}
