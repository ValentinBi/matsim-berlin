package org.matsim.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.api.core.v01.events.handler.PersonEntersVehicleEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.mobsim.framework.MobsimAgent;

public class AgentUsesLegModeEventHandler implements PersonDepartureEventHandler  {
	
	private final Set<Id<Person>> vehicleUsers = new HashSet<>();
	private final Map<Id<Person>, Integer> legsPerPerson = new HashMap();
	private final String legMode;
	private int numberOfLegs;
	
	public AgentUsesLegModeEventHandler(String legmode) {
		this.legMode = legmode;
		this.numberOfLegs = 0;
	}
	
	Set<Id<Person>> getVehicleUsers(){
		return vehicleUsers;
		
	}
	Map<Id<Person>, Integer> getLegsPerPerson(){
		return legsPerPerson;
	}
	
	
	int getNumberOfLegs() {
		return numberOfLegs;
	}

	@Override
	public void handleEvent(PersonDepartureEvent event) {
		if(event.getLegMode().contains(legMode)) {
			vehicleUsers.add(event.getPersonId());
			numberOfLegs ++;
			legsPerPerson.merge(event.getPersonId(), 1, Integer::sum);
		}
		
	}
	
	

}
