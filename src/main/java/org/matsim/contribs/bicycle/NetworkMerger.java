package org.matsim.contribs.bicycle;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;

public class NetworkMerger {

	private static Path BERLIN_NETWORK = Paths
			.get("./input/berlin-v5-network.xml.gz");
	private static Path BIKE_NETWORK = Paths
			.get("./input/shapefile_network.xml");
	static Network berlinNet = NetworkUtils.createNetwork();
	static Network bikeNetwork = NetworkUtils.createNetwork();
	
	public static void main(String[] args) {
		
		
		new MatsimNetworkReader(berlinNet).readFile(BERLIN_NETWORK.toString());
		new MatsimNetworkReader(bikeNetwork).readFile(BIKE_NETWORK.toString());
		BikeNetworkMerger merge = new BikeNetworkMerger(berlinNet);
		Network mergedNetwork = merge.mergeBikeHighways(bikeNetwork);
		NetworkMerger mrgr = new NetworkMerger();
		//addBikeModeToExistingNetwork(mergedNetwork);
		mergedNetwork = addBikeModeToExistingNetwork(mergedNetwork);
		
		Path output = Paths.get("./input/");
		new NetworkWriter(mergedNetwork).write(output.resolve("mergedNetwork.xml.gz").toString());
		System.out.println("Merged Network file written.");
	}
	
	private static Network addBikeModeToExistingNetwork(Network network) {
		Collection<? extends Link> links = network.getLinks().values();
		for (Link link : links) {
			if (link.getFreespeed()<=50/3.6){
				addBikeMode(link);
			}
		}
		return network;
		
	}
	
	private static void addBikeMode(Link link) {
		Set<String> allowedModes = link.getAllowedModes();
		Set<String> newAllowedModes = new HashSet<String>();
		newAllowedModes.add(TransportMode.bike);
		newAllowedModes.addAll(allowedModes);
		link.setAllowedModes(newAllowedModes);
		link.getAttributes().putAttribute(BikeLinkSpeedCalculator.BIKE_SPEED_FACTOR_KEY, 0.5);
	}

}
