package org.matsim.analysis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contribs.bicycle.BikeLinkSpeedCalculator;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;

public class RunBikeAnalysis {
	
	public static void main(String[] args) throws IOException {
		Path networkpath = Paths.get("./input/mergedNetwork.xml.gz");
		Path baseCaseEventsPath = Paths.get("./output/bike_basecase/ITERS/it.300/berlin-v5.3-1pct-bike_basecase.300.events.xml.gz");
		Path policyCaseEventsPath = Paths.get("./output/bike_highways/ITERS/it.300/berlin-v5.3-1pct-bike_highways.300.events.xml.gz");
		Path listOfAddedLinksFilePath = Paths.get("./output/bicycleHighwayLinks.txt");
		File listOfAddedLinks = new File(listOfAddedLinksFilePath.toString());

		// read in the simulation network
		Network network = NetworkUtils.createNetwork();
		new MatsimNetworkReader(network).readFile(networkpath.toString());
		
		Collection<? extends Link> links = network.getLinks().values();
		Set<Link> bikeHighwayLinks = new HashSet<Link>();
		Set<Id<Link>> bikeHighwayLinkIds = new HashSet<Id<Link>>();

		//System.out.println("added Links for Bicycle highways:");
		try (FileWriter writer = new FileWriter(listOfAddedLinks)) {		
			for (Link link: links) {
				if (link.getAttributes().getAttribute(BikeLinkSpeedCalculator.BIKE_SPEED_FACTOR_KEY)!=null && link.getAttributes().getAttribute(BikeLinkSpeedCalculator.BIKE_SPEED_FACTOR_KEY).equals(1.0)) { // is Bike highway
					bikeHighwayLinks.add(link);
					bikeHighwayLinkIds.add(link.getId());
					writer.write(link.getId().toString()+ "\n");
					// System.out.println(link.getId());
				}
			}
		}
		AgentTravelledOnLinkEventHandler agentTravelledOnLinkEventHandler = new AgentTravelledOnLinkEventHandler(bikeHighwayLinkIds);
		TravelDistanceEventHandler travelDistanceEventHandlerPolicy = new TravelDistanceEventHandler(network);
		TravelTimeEventHandler travelTimeEventHandlerPolicy = new TravelTimeEventHandler();
		AgentUsesLegModeEventHandler agentUsesBicycleEventHandlerPolicy = new AgentUsesLegModeEventHandler("bicycle");
		AgentUsesLegModeEventHandler agentUsesCarEventHandlerPolicy = new AgentUsesLegModeEventHandler(TransportMode.car);
		AgentUsesLegModeEventHandler agentUsesPtEventHandlerPolicy = new AgentUsesLegModeEventHandler(TransportMode.pt);
		//AgentUsesLegModeEventHandler agentUsesBikeEventHandlerPolicy = new AgentUsesLegModeEventHandler(TransportMode.bike);
		AgentUsesLegModeEventHandler agentWalksEventHandlerPolicy = new AgentUsesLegModeEventHandler("walk");
		
		EventsManager policyCaseManager = EventsUtils.createEventsManager();
		policyCaseManager.addHandler(travelDistanceEventHandlerPolicy);
		policyCaseManager.addHandler(travelTimeEventHandlerPolicy);
		policyCaseManager.addHandler(agentUsesBicycleEventHandlerPolicy);
		policyCaseManager.addHandler(agentUsesCarEventHandlerPolicy);
		//policyCaseManager.addHandler(agentUsesBikeEventHandlerPolicy);
		policyCaseManager.addHandler(agentUsesPtEventHandlerPolicy);
		policyCaseManager.addHandler(agentWalksEventHandlerPolicy);
		
		

		new MatsimEventsReader(policyCaseManager).readFile(policyCaseEventsPath.toString());
		
		/*
		for (Id<Person> agent : agentUsesBikeEventHandlerPolicy.getVehicleUsers()) {
			System.out.println(agent.toString());
		}
		for (Id<Person> agent : agentUsesCarEventHandlerPolicy.getVehicleUsers()) {
			System.out.println(agent);
		}
		*/
		
		
		

		TravelDistanceEventHandler travelDistanceEventHandlerBase = new TravelDistanceEventHandler(network);
		TravelTimeEventHandler travelTimeEventHandlerBase = new TravelTimeEventHandler();
		AgentUsesLegModeEventHandler agentUsesBicycleEventHandlerBase = new AgentUsesLegModeEventHandler("bicycle");
		AgentUsesLegModeEventHandler agentUsesCarEventHandlerBase = new AgentUsesLegModeEventHandler(TransportMode.car);
		AgentUsesLegModeEventHandler agentUsesPtEventHandlerBase = new AgentUsesLegModeEventHandler(TransportMode.pt);
		AgentUsesLegModeEventHandler agentWalksEventHandlerBase = new AgentUsesLegModeEventHandler("walk");
		
		EventsManager baseCaseManager = EventsUtils.createEventsManager();
		baseCaseManager.addHandler(agentUsesPtEventHandlerBase);
		baseCaseManager.addHandler(agentUsesCarEventHandlerBase);
		baseCaseManager.addHandler(agentUsesBicycleEventHandlerBase);
		baseCaseManager.addHandler(travelTimeEventHandlerBase);
		baseCaseManager.addHandler(travelDistanceEventHandlerBase);
		baseCaseManager.addHandler(agentWalksEventHandlerBase);
		

		new MatsimEventsReader(baseCaseManager).readFile(baseCaseEventsPath.toString());
		
		Double bicycleRiderTravelTimePolicy = calculateTravelTimeOfSetOfAgent(agentUsesBicycleEventHandlerPolicy.getVehicleUsers(), travelTimeEventHandlerPolicy);
		Double bicycleRiderTravelTimeBase = calculateTravelTimeOfSetOfAgent(agentUsesBicycleEventHandlerBase.getVehicleUsers(), travelTimeEventHandlerBase);
		
		

		System.out.println("-------------------------------------------------------------------------------");
		System.out.println("-------------------------------------------------------------------------------");
		System.out.println("POLICY CASE:");
		System.out.println("Number of bicycle riders: "+ agentUsesBicycleEventHandlerPolicy.getVehicleUsers().size());
		System.out.println("Number of bicycle rides: "+ agentUsesBicycleEventHandlerPolicy.getNumberOfLegs());
		System.out.println("Number of car drivers: "+ agentUsesCarEventHandlerPolicy.getVehicleUsers().size());
		System.out.println("Number of car legs: "+ agentUsesCarEventHandlerPolicy.getNumberOfLegs());
		System.out.println("Number of pt users: "+ agentUsesPtEventHandlerPolicy.getVehicleUsers().size());
		System.out.println("Number of pt legs: "+ agentUsesPtEventHandlerPolicy.getNumberOfLegs());
		System.out.println("Number of persons who walk: "+ agentWalksEventHandlerPolicy.getVehicleUsers().size());
		System.out.println("Number of walk legs: "+ agentWalksEventHandlerPolicy.getNumberOfLegs());
		System.out.println("-------------------------------------------------------------------------------");
		System.out.println("Total travel time: "+ (travelTimeEventHandlerPolicy.calculateOverallTravelTime()/3600) +" hours");
		
		
		System.out.println("Total Travel time of bicycle riders: "+ bicycleRiderTravelTimePolicy/3600 +"hours" );
		System.out.println("Mean of travel times of bicycle riders: "+ bicycleRiderTravelTimePolicy/agentUsesBicycleEventHandlerPolicy.getVehicleUsers().size()/60 +" min");
		System.out.println("Travel times on bicycles: " );
		
	//	System.out.println("Number of bike drivers: "+ agentUsesBikeEventHandlerPolicy.getVehicleUsers().size());
	//	System.out.println("Number of bike legs: "+ agentUsesBikeEventHandlerPolicy.getNumberOfLegs());
		

		System.out.println("-------------------------------------------------------------------------------");
		System.out.println("-------------------------------------------------------------------------------");
		System.out.println("BASE CASE:");
		System.out.println("Number of bicycle riders: "+ agentUsesBicycleEventHandlerBase.getVehicleUsers().size());
		System.out.println("Number of bicycle rides: "+ agentUsesBicycleEventHandlerBase.getNumberOfLegs());
		System.out.println("Number of car drivers: "+ agentUsesCarEventHandlerBase.getVehicleUsers().size());
		System.out.println("Number of car legs: "+ agentUsesCarEventHandlerBase.getNumberOfLegs());
		System.out.println("Number of pt users: "+ agentUsesPtEventHandlerBase.getVehicleUsers().size());
		System.out.println("Number of pt legs: "+ agentUsesPtEventHandlerBase.getNumberOfLegs());
		System.out.println("Number of persons who walk: "+ agentWalksEventHandlerBase.getVehicleUsers().size());
		System.out.println("Number of walk legs: "+ agentWalksEventHandlerBase.getNumberOfLegs());
		System.out.println("-------------------------------------------------------------------------------");
		System.out.println("Total travel time: "+ (travelTimeEventHandlerBase.calculateOverallTravelTime()/3600) +" hours");
		System.out.println("Mean of travel times of bicycle riders: "+ bicycleRiderTravelTimeBase/agentUsesBicycleEventHandlerBase.getVehicleUsers().size()/60 +" min");
		System.out.println("Travel times on bicycles: " );
		

		System.out.println("-------------------------------------------------------------------------------");
		System.out.println("-------------------------------------------------------------------------------");
		System.out.println("End of Analysis.");

	}
	
	private static double calculateTravelTimeOfSetOfAgent(Set<Id<Person>> persons,TravelTimeEventHandler eventHandler) {
		Map<Id<Person>, Double> personTimeMapping = eventHandler.getTravelTimesByPerson();
		Double timeSum = 0.;
		for (Id<Person> person: persons) {
			timeSum += personTimeMapping.get(person);
		}
		return timeSum;
		
	}
	/*
	 * TODO:
	 * time of bike riders in general
	 * time of bike riders using the bike highways
	 * 
	 * distance of bike riders in general (sum and mean)
	 * distance of bike riders using the bike highway (sum and mean)
	 * distance travelled on bike highways (proportion to normal streets)
	 * 
	 * 
	 * in Via:
	 * Location of the bike highway users
	 * 
	 * if possible:
	 * change of modal split for departure or arrival near bike highway
	 */
}
