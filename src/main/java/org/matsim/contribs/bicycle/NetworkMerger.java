package org.matsim.contribs.bicycle;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;

public class NetworkMerger {

	private static Path BERLIN_NETWORK = Paths
			.get("D:/Scientific_Computing/work/input/berlin-v5-network.xml.gz");
	private static Path BIKE_NETWORK = Paths
			.get("D:/Scientific_Computing/work/shape file conversion/output/test.xml");
	static Network berlinNet = NetworkUtils.createNetwork();
	static Network bikeNetwork = NetworkUtils.createNetwork();
	
	public static void main(String[] args) {
		
		
		new MatsimNetworkReader(berlinNet).readFile(BERLIN_NETWORK.toString());
		new MatsimNetworkReader(bikeNetwork).readFile(BIKE_NETWORK.toString());
		BikeNetworkMerger merge = new BikeNetworkMerger(berlinNet);
		Network mergedNetwork = merge.mergeBikeHighways(bikeNetwork);
		NetworkMerger mrgr = new NetworkMerger();
		mrgr.addBikeModeToExistingNetwork(mergedNetwork);
		//mergedNetwork = addBikeModeToExistingNetwork(mergedNetwork);
		
		Path output = Paths.get("D:/Scientific_Computing/work/shape file conversion/output");
		new NetworkWriter(mergedNetwork).write(output.resolve("mergedNetwork.xml").toString());
	}
	
	private  Network addBikeModeToExistingNetwork(Network network) {
		network.getLinks().values().forEach(link -> {
			if (link.getFreespeed()<=50/3.6){
				Set<String> allowedModes = link.getAllowedModes();
				allowedModes.add(TransportMode.bike);
				link.setAllowedModes(allowedModes);
				link.getAttributes().putAttribute(BikeLinkSpeedCalculator.BIKE_SPEED_FACTOR_KEY, 0.5);
			}
		});
		return network;
		
	}

}
