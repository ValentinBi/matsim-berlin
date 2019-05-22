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

import jdk.javadoc.internal.doclets.formats.html.markup.Links;

public class NetworkAdaptions {
	


	static Set<String> allowedModes = new HashSet<>();

	private static void setMotorwayAttributesAndAddLink(Link link, Network network) {
		
		link.setCapacity(6000);
		link.setFreespeed(22.222222); // = 80 km/h
		link.setAllowedModes(allowedModes);
		link.setNumberOfLanes(3);
		network.addLink(link);
	}
	

	private static long getFirstFreeNodeId(Network network) {
		long counter = Long.parseLong("1");
		//System.out.println(network.getNodes().get(Id.createNodeId(counter)).getId());
		while (!(network.getNodes().get(Id.createNodeId(counter)) == null)) {
			counter ++;
			//System.out.println(network.getNodes().get(Id.createNodeId(counter)));
		}
		System.out.println("NodeId: "+ counter);
		return counter;
		
	}
	
	private static long getFirstFreeLinkId(Network network) {
		long counter = Long.parseLong("1");
		while (!(network.getLinks().get(Id.createLinkId(counter)) == null)) {
			counter ++;
			//System.out.println((network.getLinks().get(Id.createLinkId(counter))));
		}
		System.out.println("LinkID: "+ counter);
		return counter;
		
	}
		
		
	
	public static void main(String[] args) {
		Path inputNetwork= Paths.get("./input/berlin-v5-network.xml.gz");
		//Path inputNetwork= Paths.get("/home/valentin/MATSim/Berlin/berlin-v5-network.xml.gz");
		//Path inputNetwork= Paths.get("http://svn.vsp.tu-berlin.de/repos/public-svn/matsim/scenarios/countries/de/berlin/berlin-v5.3-10pct/input/berlin-v5-network.xml.gz");
		Path outputNetwork= Paths.get("./input/berlin-v5-A100-network.xml.gz");
		Network network = NetworkUtils.createNetwork();

		NetworkFactory factory = network.getFactory();
			
		new MatsimNetworkReader(network).readFile(inputNetwork.toString());
		
		//retrieve nodes (Knotenpunkte vom Netzwerk abrufen)
		
		//(1) Connection to existing A100 (Verbindung zur bestehenden A100)
		Node grenzalleeNorthEast = network.getNodes().get(Id.createNodeId("27542432"));
		Node grenzalleeNorthWest = network.getNodes().get(Id.createNodeId("27542414"));
		
		//(2) Reopening of Grenzallee (Aufhebung Sperrung Grenzallee)
		Node grenzallee11 = network.getNodes().get(Id.createNodeId("31390642"));
		Node grenzallee12 = network.getNodes().get(Id.createNodeId("1039457368"));		
		Node grenzallee21 = network.getNodes().get(Id.createNodeId("261694008"));
		Node grenzallee22 = network.getNodes().get(Id.createNodeId("27555275"));
		
		//(3) HAS Grenzallee
		Node fromA113ToGrenzallee_a = network.getNodes().get(Id.createNodeId("596372782"));
		Node fromA113ToGrenzallee_b = network.getNodes().get(Id.createNodeId("254868141"));	
			/* Node for Grenzallee-A100*/
		Node fromGrenzalleeToA113_a = network.getNodes().get(Id.createNodeId("31390642"));
		Node fromGrenzalleeToA113_b = factory.createNode(Id.createNodeId(getFirstFreeNodeId(network)), new Coord(4599089.059985, 5815695.864662));
		network.addNode(fromGrenzalleeToA113_b);
		Node fromGrenzalleeToA113_c = network.getNodes().get(Id.createNodeId("27542427"));
		//create additional node between node 27542427 and node 206191220 - cars can't drive to A100
		
		//detach the following nodes off the Autobahn [Bergiusstrasse] - it's just a HAS
		//27542414
		//27542432
		
		//(4) AS Sonnenallee
		Node sonnenalleeSouthEast = network.getNodes().get(Id.createNodeId("5332081036"));
		Node sonnenalleeNorthEast = network.getNodes().get(Id.createNodeId("3386901041"));
		
		//(5) AS Treptower Park
		Node treptowerParkSouthWest = network.getNodes().get(Id.createNodeId("287932598"));
		Node amTreptowerPark = network.getNodes().get(Id.createNodeId("20246103"));
		//additional link to guarantee that drivers can turn left
		
		//(6) AS Ostkreuz
		Node ostkreuz = network.getNodes().get(Id.createNodeId("2395404884"));
		
		//(7) AS Frankfurter Allee (not in the official BVWP anymore)
		/* connect with nodes 598234402 and 12614683 */
		
		//(8) Connection to Storkower Strasse
		/* connect with node 27195097 */
		

		allowedModes.add(TransportMode.car);

		// create links
		System.out.println("Creating Links..." );
		
		//(1) Connection to existing A100 (Verbindung zur bestehenden A100)
		Link link1 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), grenzalleeNorthEast, sonnenalleeSouthEast);
		link1.setLength(1100); // length from : http://www.autobahnatlas-online.de/
		setMotorwayAttributesAndAddLink(link1, network);
		Link link2 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)),  sonnenalleeSouthEast, grenzalleeNorthWest);
		link2.setLength(1100);
		setMotorwayAttributesAndAddLink(link2, network);
		
		//(2) Reopening of Grenzallee (Aufhebung Sperrung Grenzallee) - Creation of 2 links to reopen Grenzallee street
		Link link10 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), grenzallee11, grenzallee21);
		link10.setLength(280); //estimated with Via
		setMotorwayAttributesAndAddLink(link10, network);
		Link link11 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), grenzallee12, grenzallee22);
		link11.setLength(330); //estimated with Via
		setMotorwayAttributesAndAddLink(link11, network);
		
		//(3) HAS Grenzallee
		Link link12 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), fromA113ToGrenzallee_a, fromA113ToGrenzallee_b);
		link12.setLength(280); //estimated with Via
		setMotorwayAttributesAndAddLink(link12, network);
			/*from Grenzallee-A113*/
		Link link13 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), fromGrenzalleeToA113_a, fromGrenzalleeToA113_b);
		link13.setLength(35); //estimated with Via
		setMotorwayAttributesAndAddLink(link13, network);
		Link link14 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), fromGrenzalleeToA113_b, fromGrenzalleeToA113_c);
		link14.setLength(280); //estimated with Via
		setMotorwayAttributesAndAddLink(link14, network);
		
		//(4) AS Sonnenallee
		Link link3 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), sonnenalleeSouthEast, sonnenalleeNorthEast);
		link3.setLength(20);
		setMotorwayAttributesAndAddLink(link3, network);
		Link link4 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), sonnenalleeNorthEast, sonnenalleeSouthEast);
		link4.setLength(20);
		setMotorwayAttributesAndAddLink(link4, network);
		
		//Part Sonnenallee - Treptower Park
		Link link5 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), sonnenalleeSouthEast, treptowerParkSouthWest);
		link5.setLength(2000);
		setMotorwayAttributesAndAddLink(link5, network);
		Link link6 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), treptowerParkSouthWest, sonnenalleeSouthEast);
		link6.setLength(2000);
		setMotorwayAttributesAndAddLink(link6, network);
		
		//(5) AS Treptower Park
		Link link9 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), amTreptowerPark, treptowerParkSouthWest);
		link9.setLength(76.34);
		setMotorwayAttributesAndAddLink(link9, network);
		//additional link to guarantee that drivers can turn left
		Link link15 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), treptowerParkSouthWest, amTreptowerPark);
		link15.setLength(76.34);
		setMotorwayAttributesAndAddLink(link15, network);
		
		//Part Treptower Park - Ostkreuz
		Link link7 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), treptowerParkSouthWest, ostkreuz);
		link7.setLength(1000);
		setMotorwayAttributesAndAddLink(link7, network);
		Link link8 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), ostkreuz, treptowerParkSouthWest);
		link8.setLength(1000);
		setMotorwayAttributesAndAddLink(link8, network);
		
		//(6) AS Ostkreuz
		
			/*finished, maybe adjust network close to the Autobahn*/
		
		//Part Ostkreuz - Frankfurter Allee
		
			/*create link between node 2395404884 and new node around "Frankfurter Allee Ring Center" */
			//research link length
		
		//(7) AS Frankfurter Allee
		
			/*create link between node "Frankfurter Allee Ring Center" and 598234402 [both directions!]*/
			/*create link between node "Frankfurter Allee Ring Center" and 12614683 [both directions!]*/
		
		//Part Frankfurter Allee - Storkower Strasse
		
			/*create link between node "Frankfurter Allee Ring Center" and new node 27195097 */
			//research link length

		

		new NetworkWriter(network).write(outputNetwork.toString());
	}
	
}
