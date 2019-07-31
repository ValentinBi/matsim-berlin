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

public class BicycleTravelDistanceEventHandler implements PersonDepartureEventHandler, GenericEventHandler {
	private final Map<Id<Person>, Double> travelDistances = new HashMap<>();
	private final Map<Id<Person>, PersonDepartureEvent> openTrips = new HashMap<Id<Person>, PersonDepartureEvent>();
	private Double totalTravelDistance;
	
	public BicycleTravelDistanceEventHandler() {
		totalTravelDistance = 0.;
	}
	
	public Double getTotalTravelDistance() {
		return totalTravelDistance;
	}
	
	public Map<Id<Person>, Double> getTravelDistancesByPerson(){
		return travelDistances;
	}

	@Override
	public void handleEvent(PersonDepartureEvent event) {
		if(event.getLegMode().equals("bicycle")) {
			openTrips.put(event.getPersonId(), event);
			//System.out.println(event.getPersonId()+" hinzugefügt");
		}
		
	}

	@Override
	public void handleEvent(GenericEvent event) {
		System.out.println("GenericEvent gefunden -----------------------------");
		if(event.getEventType().contains("travelled")) {
			System.out.println("travelled event detected");
			Id<Person> personId = Id.createPersonId(event.getAttributes().get("person"));
			if(openTrips.containsKey(personId)){
				System.out.println("travelled event zugeordnet");
				Double distance = Double.parseDouble(event.getAttributes().get("distance"));
				travelDistances.merge(personId, distance, Double::sum);
				totalTravelDistance += distance;
				openTrips.remove(personId);
			}
		}
		
	}
}
