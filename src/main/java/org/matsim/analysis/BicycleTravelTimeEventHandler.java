package org.matsim.analysis;

import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.handler.PersonArrivalEventHandler;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.api.core.v01.population.Person;

public class BicycleTravelTimeEventHandler implements PersonArrivalEventHandler, PersonDepartureEventHandler {
	private final Map<Id<Person>, Double> travelTimes = new HashMap<>();
	private final Map<Id<Person>, PersonDepartureEvent> openTrips = new HashMap<Id<Person>, PersonDepartureEvent>();
	private Double totalTravelTime;
	
	public BicycleTravelTimeEventHandler() {
		totalTravelTime = 0.;
	}
	
	public Double getTotalTravelTime() {
		return totalTravelTime;
	}
	
	public Map<Id<Person>, Double> getTravelTimesByPerson(){
		return travelTimes;
	}

	@Override
	public void handleEvent(PersonDepartureEvent event) {
		if(event.getLegMode().equals("bicycle")) {
			openTrips.put(event.getPersonId(), event);
		}
	}

	@Override
	public void handleEvent(PersonArrivalEvent event) {
		if(openTrips.containsKey(event.getPersonId())) {
			PersonDepartureEvent depEvent = openTrips.remove(event.getPersonId());
			Double time = event.getTime() - depEvent.getTime();
			totalTravelTime += time;
			if(travelTimes.containsKey(event.getPersonId())) {
				travelTimes.merge(event.getPersonId(), time, Double::sum);
			}
		}
		
	}

	
	

}
