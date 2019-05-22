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
		Node fromGrenzalleeToA113_b = factory.createNode(Id.create("900000000", Node.class), new Coord(4599089.059985, 5815695.864662));
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
		Set<Link> linkSet = new HashSet<Link>(); 
		
		//(1) Connection to existing A100 (Verbindung zur bestehenden A100)
		Link link1 = factory.createLink(Id.createLinkId(160889), grenzalleeNorthEast, sonnenalleeSouthEast);
		link1.setLength(1100); // length from : http://www.autobahnatlas-online.de/
		linkSet.add(link1);
		Link link2 = factory.createLink(Id.createLinkId(160890),  sonnenalleeSouthEast, grenzalleeNorthWest);
		link2.setLength(1100);
		linkSet.add(link2);
		
		//(2) Reopening of Grenzallee (Aufhebung Sperrung Grenzallee) - Creation of 2 links to reopen Grenzallee street
		Link link10 = factory.createLink(Id.createLinkId(160898), grenzallee11, grenzallee21);
		link10.setLength(280); //estimated with Via
		linkSet.add(link10);
		Link link11 = factory.createLink(Id.createLinkId(160899), grenzallee12, grenzallee22);
		link11.setLength(330); //estimated with Via
		linkSet.add(link11);
		
		//(3) HAS Grenzallee
		Link link12 = factory.createLink(Id.createLinkId(160900), fromA113ToGrenzallee_a, fromA113ToGrenzallee_b);
		link12.setLength(280); //estimated with Via
		linkSet.add(link12);
			/*from Grenzallee-A113*/
		Link link13 = factory.createLink(Id.createLinkId(160901), fromGrenzalleeToA113_a, fromGrenzalleeToA113_b);
		link13.setLength(35); //estimated with Via
		linkSet.add(link13);
		Link link14 = factory.createLink(Id.createLinkId(160902), fromGrenzalleeToA113_b, fromGrenzalleeToA113_c);
		link14.setLength(280); //estimated with Via
		linkSet.add(link14);
		
		//(4) AS Sonnenallee
		Link link3 = factory.createLink(Id.createLinkId(160891), sonnenalleeSouthEast, sonnenalleeNorthEast);
		link3.setLength(20);
		linkSet.add(link3);
		Link link4 = factory.createLink(Id.createLinkId(160892), sonnenalleeNorthEast, sonnenalleeSouthEast);
		link4.setLength(20);
		linkSet.add(link4);
		
		//Part Sonnenallee - Treptower Park
		Link link5 = factory.createLink(Id.createLinkId(160893), sonnenalleeSouthEast, treptowerParkSouthWest);
		link5.setLength(2000);
		linkSet.add(link5);
		Link link6 = factory.createLink(Id.createLinkId(160894), treptowerParkSouthWest, sonnenalleeSouthEast);
		link6.setLength(2000);
		linkSet.add(link6);
		
		//(5) AS Treptower Park
		Link link9 = factory.createLink(Id.createLinkId(160897), amTreptowerPark, treptowerParkSouthWest);
		link9.setLength(76.34);
		linkSet.add(link9);
		//additional link to guarantee that drivers can turn left
		Link link15 = factory.createLink(Id.createLinkId(160903), treptowerParkSouthWest, amTreptowerPark);
		link15.setLength(76.34);
		linkSet.add(link15);
		
		//Part Treptower Park - Ostkreuz
		Link link7 = factory.createLink(Id.createLinkId(160895), treptowerParkSouthWest, ostkreuz);
		link7.setLength(1000);
		linkSet.add(link7);
		Link link8 = factory.createLink(Id.createLinkId(160896), ostkreuz, treptowerParkSouthWest);
		link8.setLength(1000);
		linkSet.add(link8);
		
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

		
		for (Link link: linkSet) {
			addAttributes(link);
			network.addLink(link);	
		}	

		new NetworkWriter(network).write(outputNetwork.toString());
	}
	
}
