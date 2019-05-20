package org.matsim.adaptions;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;

public class NetworkAdaptions {
	


	static Set<String> allowedModes = new HashSet<>();

	private static void addAttributes(Link link) {
		
		link.setCapacity(6000);
		link.setFreespeed(22.222222); // = 80 km/h
		link.setAllowedModes(allowedModes);
		link.setNumberOfLanes(3);
	}
		
	
	public static void main(String[] args) {
		Path inputNetwork= Paths.get("./input/berlin-v5-network.xml.gz");
		//Path inputNetwork= Paths.get("/home/valentin/MATSim/Berlin/berlin-v5-network.xml.gz");
		//Path inputNetwork= Paths.get("http://svn.vsp.tu-berlin.de/repos/public-svn/matsim/scenarios/countries/de/berlin/berlin-v5.3-10pct/input/berlin-v5-network.xml.gz");
		Path outputNetwork= Paths.get("./input/berlin-v5-A100-network.xml.gz");
		Network network = NetworkUtils.createNetwork();

		NetworkFactory factory = network.getFactory();
			
		new MatsimNetworkReader(network).readFile(inputNetwork.toString());
		
		Node grenzalleeNorthEast = network.getNodes().get(Id.createNodeId("27542432"));
		Node grenzalleeNorthWest = network.getNodes().get(Id.createNodeId("27542414"));
		
		Node sonnenalleeSouthEast = network.getNodes().get(Id.createNodeId("5332081036"));
		
		Node sonnenalleeNorthEast = network.getNodes().get(Id.createNodeId("3386901041"));
		
		Node treptowerParkSouthWest = network.getNodes().get(Id.createNodeId("287932598"));
		
		Node ostkreuz = network.getNodes().get(Id.createNodeId("2395404884"));
		
		Node node1 = factory.createNode(Id.create("9000000000", Node.class), new Coord(4599089.059985, 5815695.864662));
		
		Node amTreptowerPark = network.getNodes().get(Id.createNodeId("20246103"));
		

		allowedModes.add(TransportMode.car);

		// create links
		Set<Link> linkSet = new HashSet<Link>(); 
		
		Link link1 = factory.createLink(Id.createLinkId(160889), grenzalleeNorthEast, sonnenalleeSouthEast);
		link1.setLength(1100); // length from : http://www.autobahnatlas-online.de/
		linkSet.add(link1);
		Link link2 = factory.createLink(Id.createLinkId(160890),  sonnenalleeSouthEast, grenzalleeNorthWest);
		link2.setLength(1100);
		linkSet.add(link2);
		
		Link link3 = factory.createLink(Id.createLinkId(160891), sonnenalleeSouthEast, sonnenalleeNorthEast);
		link3.setLength(20);
		linkSet.add(link3);
		Link link4 = factory.createLink(Id.createLinkId(160892), sonnenalleeNorthEast, sonnenalleeSouthEast);
		link4.setLength(20);
		linkSet.add(link4);
		
		Link link5 = factory.createLink(Id.createLinkId(160893), sonnenalleeSouthEast, treptowerParkSouthWest);
		link5.setLength(2000);
		linkSet.add(link5);
		Link link6 = factory.createLink(Id.createLinkId(160894), treptowerParkSouthWest, sonnenalleeSouthEast);
		link6.setLength(2000);
		linkSet.add(link6);

		Link link7 = factory.createLink(Id.createLinkId(160895), treptowerParkSouthWest, ostkreuz);
		link7.setLength(1000);
		linkSet.add(link7);
		Link link8 = factory.createLink(Id.createLinkId(160896), ostkreuz, treptowerParkSouthWest);
		link8.setLength(1000);
		linkSet.add(link8);
		
		Link link9 = factory.createLink(Id.createLinkId(160897), amTreptowerPark, treptowerParkSouthWest);
		link8.setLength(76.34);
		linkSet.add(link9);
		
		for (Link link: linkSet) {
			addAttributes(link);
			network.addLink(link);	
		}	

		new NetworkWriter(network).write(outputNetwork.toString());
	}
	
}
