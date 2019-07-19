/**
 * 
 */
package org.matsim.analysis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;

/**
 * @author Aravind
 *
 */
public class RunBerlinAnalysis {

	/**
	 * @param args
	 *
	 */
	public static void main(String[] args) {

		// get the paths for the network and the events
		Path networkpath = Paths.get("./input/berlin-v5-A100-network.xml.gz");
		Path baseCaseEventsPath = Paths.get("./output/berlin-v5.3-1pct.output_events.xml.gz");
		Path policyCaseEventsPath = Paths
				.get("./output/output_a100/it.350/berlin-v5.3-1pct-A100.350.events.xml.gz");

		Set<Id<Link>> linksToWatch = getLinksFromFile("./input/addedLinks.txt");

		// read in the simulation network
		Network network = NetworkUtils.createNetwork();
		new MatsimNetworkReader(network).readFile(networkpath.toString());

		// start preparing the events manager

		TravelDistanceEventHandler travelDistanceEventHandler = new TravelDistanceEventHandler(network);
		TrafficTimeEventHandler trafficTimeEventHandler = new TrafficTimeEventHandler();

		EventsManager baseCaseManager = EventsUtils.createEventsManager();
		baseCaseManager.addHandler(travelDistanceEventHandler);
		baseCaseManager.addHandler(trafficTimeEventHandler);

		// read the base case events file
		new MatsimEventsReader(baseCaseManager).readFile(baseCaseEventsPath.toString());

		TravelDistanceEventHandler travelDistanceEventHandlerPolicy = new TravelDistanceEventHandler(network);
		AgentTravelledOnLinkEventHandler agentTravelledOnLinkEventHandler = new AgentTravelledOnLinkEventHandler(
				linksToWatch);
		TrafficTimeEventHandler trafficTimeEventHandlerPolicy = new TrafficTimeEventHandler();

		EventsManager policyCaseManager = EventsUtils.createEventsManager();
		policyCaseManager.addHandler(agentTravelledOnLinkEventHandler);
		policyCaseManager.addHandler(travelDistanceEventHandlerPolicy);
		policyCaseManager.addHandler(trafficTimeEventHandlerPolicy);

		// read the policy case events file
		new MatsimEventsReader(policyCaseManager).readFile(policyCaseEventsPath.toString());

		System.out.println("==========================================================================");
		System.out.println("Total number of people used the newly created links "
				+ agentTravelledOnLinkEventHandler.getPersonOnWatchedLinks().size());
		System.out.println("==========================================================================");
		System.out.println("Analysing the trip of all agents");
		double baseCaseForAllDistance = travelDistanceEventHandler.getTotalTravelDistance() / 1000;
		double policyCaseForAllDistance = travelDistanceEventHandlerPolicy.getTotalTravelDistance() / 1000;
		System.out.println("Total travel distance of all persons base case: " + baseCaseForAllDistance + "km");
		System.out.println("Total travel distance of all persons policy case: " + policyCaseForAllDistance + "km");
		System.out.println("Difference in travel distances for all persons: "
				+ (baseCaseForAllDistance - policyCaseForAllDistance) + "km");

		double baseCaseForAllTimeInTraffic = trafficTimeEventHandler.calculateTimeInTraffic() / 60 / 60;
		double policyCaseForAllTimeInTraffic = trafficTimeEventHandlerPolicy.calculateTimeInTraffic() / 60 / 60;
		System.out.println("Total time in traffic of all persons base case: " + baseCaseForAllTimeInTraffic + " hours");
		System.out.println(
				"Total time in traffic of all persons policy case: " + policyCaseForAllTimeInTraffic + " hours");
		System.out.println("Difference in time spent by all persons in traffic "
				+ (baseCaseForAllTimeInTraffic - policyCaseForAllTimeInTraffic) + " hours");

		System.out.println("==========================================================================");
		System.out.println("Analysing the trip of affected agents");

		// calculate total travel distances for people who used the street
		double baseCaseDistance = travelDistanceEventHandler.getTravelDistancesByPerson().entrySet().stream()
				.filter(entry -> agentTravelledOnLinkEventHandler.getPersonOnWatchedLinks().contains(entry.getKey()))
				.mapToDouble(entry -> entry.getValue()).sum();
		baseCaseDistance = baseCaseDistance / 1000;
		double policyCaseDistance = travelDistanceEventHandlerPolicy.getTravelDistancesByPerson().entrySet().stream()
				.filter(entry -> agentTravelledOnLinkEventHandler.getPersonOnWatchedLinks().contains(entry.getKey()))
				.mapToDouble(entry -> entry.getValue()).sum();
		policyCaseDistance = policyCaseDistance / 1000;
		System.out.println("Total distance travelled by affected agents - base case " + baseCaseDistance + "km");
		System.out.println("Total distance travelled by affected agents - policy case " + policyCaseDistance + "km");
		System.out.println("Difference in travel distances for affected agents: "
				+ (baseCaseDistance - policyCaseDistance) + "km");

		// Calculate the time spent in traffic by affected agents
		double baseCaseTimeInTraffic = trafficTimeEventHandler.getTimeInTrafficByPerson().entrySet().stream()
				.filter(entry -> agentTravelledOnLinkEventHandler.getPersonOnWatchedLinks().contains(entry.getKey()))
				.mapToDouble(entry -> entry.getValue()).sum();
		baseCaseTimeInTraffic = baseCaseTimeInTraffic / 60 / 60;
		// Calculate the time spent in traffic by affected agents
		double policyCaseTimeInTraffic = trafficTimeEventHandlerPolicy.getTimeInTrafficByPerson().entrySet().stream()
				.filter(entry -> agentTravelledOnLinkEventHandler.getPersonOnWatchedLinks().contains(entry.getKey()))
				.mapToDouble(entry -> entry.getValue()).sum();
		policyCaseTimeInTraffic = policyCaseTimeInTraffic / 60 / 60;
		System.out.println("Time spent in traffic by affectd agents - base case " + baseCaseTimeInTraffic + " hours");
		System.out
				.println("Time spent in traffic by affectd agents - policy case " + policyCaseTimeInTraffic + " hours");
		System.out.println("Difference in time spent by affected agents in traffic "
				+ (baseCaseTimeInTraffic - policyCaseTimeInTraffic) + " hours");

		System.out.println("==========================================================================");
//		Iterator<Id<Person>> agents = agentTravelledOnLinkEventHandler.getPersonOnWatchedLinks().iterator();
//
//		while (agents.hasNext()) {
//
//			String id = agents.next().toString();
//
//			System.out.println(id);
//		}

	}

	public static Set<Id<Link>> getLinksFromFile(String filename) {
		BufferedReader reader;
		Set<Id<Link>> linkSet = new HashSet<Id<Link>>();
		try {
			reader = new BufferedReader(new FileReader(filename));
			String line = reader.readLine();
			while (line != null) {
				linkSet.add(Id.createLinkId(line));
				line = reader.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return linkSet;
	}

}
