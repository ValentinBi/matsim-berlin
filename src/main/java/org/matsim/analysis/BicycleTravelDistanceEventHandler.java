package org.matsim.analysis;

import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.GenericEvent;
import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.handler.GenericEventHandler;
import org.matsim.api.core.v01.events.handler.PersonArrivalEventHandler;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.api.core.v01.population.Person;

public class BicycleTravelDistanceEventHandler implements PersonArrivalEventHandler, PersonDepartureEventHandler, GenericEventHandler {
	private final Map<Id<Person>, Double> travelDistances = new HashMap<>();
	private final Map<Id<Person>, PersonDepartureEvent> openTrips = new HashMap<Id<Person>, PersonDepartureEvent>();
	private Double totalTravelDistance;
	
	public BicycleTravelDistanceEventHandler() {
		totalTravelDistance = 0.;
	}
	
	public Double getTotalTravelTime() {
		return totalTravelDistance;
	}
	
	public Map<Id<Person>, Double> getTravelTimesByPerson(){
		return travelDistances;
	}

	@Override
	public void handleEvent(PersonDepartureEvent event) {
		openTrips.put(event.getPersonId(), event);
		
	}

	@Override
	public void handleEvent(PersonArrivalEvent event) {
		if(openTrips.containsKey(event.getPersonId())) {
			PersonDepartureEvent depEvent = openTrips.get(event.getPersonId());
			Double time = event.getTime() - depEvent.getTime();
			totalTravelDistance += time;
			if(travelDistances.containsKey(event.getPersonId())) {
				travelDistances.merge(event.getPersonId(), time, Double::sum);
			}
		}		
	}

	@Override
	public void handleEvent(GenericEvent event) {
		if(event.getEventType().contains("travelled")&& openTrips.containsKey(event.getAttributes().get("person"))){
			event.getAttributes().get("distance");
		}
	}
}
