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
		Node id27542432 = network.getNodes().get(Id.createNodeId("27542432"));
		Node id27542414 = network.getNodes().get(Id.createNodeId("27542414"));
		
		//(2) Reopening of Grenzallee (Aufhebung Sperrung Grenzallee)
		Node id31390642 = network.getNodes().get(Id.createNodeId("31390642"));
		Node id1039457368 = network.getNodes().get(Id.createNodeId("1039457368"));		
		Node id261694008 = network.getNodes().get(Id.createNodeId("261694008"));
		Node id27555275 = network.getNodes().get(Id.createNodeId("27555275"));
		
		//(3) HAS Grenzallee
		Node id596372782 = network.getNodes().get(Id.createNodeId("596372782"));
		Node id4348866522 = network.getNodes().get(Id.createNodeId("4348866522"));	
			/* Node for Grenzallee-A100*/
			//Node id31390642 = network.getNodes().get(Id.createNodeId("31390642"));
			Node fromGrenzalleeToA113_b = factory.createNode(Id.createNodeId(getFirstFreeNodeId(network)), new Coord(4599089.059985, 5815695.864662));
			network.addNode(fromGrenzalleeToA113_b);
			Node id27542427 = network.getNodes().get(Id.createNodeId("27542427"));		
			//extra links and nodes to retrieve
			Node id1 = network.getNodes().get(Id.createNodeId("1"));
			Node neueNode = factory.createNode(Id.createNodeId(getFirstFreeNodeId(network)), new Coord(4599004.289921, 5815593.320732));
			network.addNode(neueNode); // Id 2
			Node id2 = network.getNodes().get(Id.createNodeId("2"));
			//Node id4348866522 = network.getNodes().get(Id.createNodeId("4348866522"));
			Node id206191220 = network.getNodes().get(Id.createNodeId("206191220"));
			//Margarete-Kubicka bridge
			Node id4075503905 = network.getNodes().get(Id.createNodeId("4075503905"));
			Node id4075503901 = network.getNodes().get(Id.createNodeId("4075503901"));	
		
		//(4) AS Sonnenallee	
		Node bl4 = factory.createNode(Id.createNodeId(getFirstFreeNodeId(network)), new Coord(4599286.072069, 5816101.45091));
		network.addNode(bl4); // Id
		Node br4 = factory.createNode(Id.createNodeId(getFirstFreeNodeId(network)), new Coord(4599310.721724, 5816100.455236));
		network.addNode(br4); // Id
		Node tl4 = factory.createNode(Id.createNodeId(getFirstFreeNodeId(network)), new Coord(4599452.914952, 5816650.425394));
		network.addNode(tl4); // Id
		Node tr4 = factory.createNode(Id.createNodeId(getFirstFreeNodeId(network)), new Coord(4599472.793197, 5816646.16577));
		network.addNode(tr4); // Id
		Node id3 = bl4;
		Node id4 = br4;
		Node id5 = tl4;
		Node id6 = tr4;
		
		Node id5332081036 = network.getNodes().get(Id.createNodeId("5332081036"));
		Node id3386901041 = network.getNodes().get(Id.createNodeId("3386901041"));
		Node id3386901047 = network.getNodes().get(Id.createNodeId("3386901047"));
		Node id5332081037 = network.getNodes().get(Id.createNodeId("5332081037"));
		
		//(5) AS Treptower Park
		
		Node bl5 = factory.createNode(Id.createNodeId(getFirstFreeNodeId(network)), new Coord(4599155.449419, 5818370.849152));
		network.addNode(bl5); // Id
		Node br5 = factory.createNode(Id.createNodeId(getFirstFreeNodeId(network)), new Coord(4599171.777977, 5818367.299465));
		network.addNode(br5); // Id
		Node tl5 = factory.createNode(Id.createNodeId(getFirstFreeNodeId(network)), new Coord(4599253.178604, 5818720.01298));
		network.addNode(tl5); // Id
		Node tr5 = factory.createNode(Id.createNodeId(getFirstFreeNodeId(network)), new Coord(4599283.705909, 5818704.394359));
		network.addNode(tr5); // Id
		Node id7 = bl5;
		Node id8 = br5;
		Node id9 = tl5;
		Node id10 = tr5;
		
		Node id287932598 = network.getNodes().get(Id.createNodeId("287932598"));
		Node id244430240 = network.getNodes().get(Id.createNodeId("244430240"));
		Node id20246103 = network.getNodes().get(Id.createNodeId("20246103"));
		
		//(6) AS Ostkreuz
		
		Node bl6 = factory.createNode(Id.createNodeId(getFirstFreeNodeId(network)), new Coord(4599750.716206, 5819658.360939));
		network.addNode(bl6); // Id
		Node br6 = factory.createNode(Id.createNodeId(getFirstFreeNodeId(network)), new Coord(4599775.327366, 5819647.475233));
		network.addNode(br6); // Id
		Node tl6 = factory.createNode(Id.createNodeId(getFirstFreeNodeId(network)), new Coord(4599900.515517, 5820047.660251));
		network.addNode(tl6); // Id
		Node tr6 = factory.createNode(Id.createNodeId(getFirstFreeNodeId(network)), new Coord(4599926.546552, 5820031.568338));
		network.addNode(tr6); // Id
		Node id11 = bl6;
		Node id12 = br6;
		Node id13 = tl6;
		Node id14 = tr6;
		
		Node id2395404884 = network.getNodes().get(Id.createNodeId("2395404884"));
		
		//(7) AS Frankfurter Allee (not in the official BVWP anymore)
		
		Node bl7 = factory.createNode(Id.createNodeId(getFirstFreeNodeId(network)), new Coord(4600305.96061, 5820871.913163));
		network.addNode(bl7); // Id
		Node br7 = factory.createNode(Id.createNodeId(getFirstFreeNodeId(network)), new Coord(4600339.327664, 5820847.065357));
		network.addNode(br7); // Id
		Node tl7 = factory.createNode(Id.createNodeId(getFirstFreeNodeId(network)), new Coord(4600233.082365, 5821295.71486));
		network.addNode(tl7); // Id
		Node tr7 = factory.createNode(Id.createNodeId(getFirstFreeNodeId(network)), new Coord(4600267.159356, 5821289.325424));
		network.addNode(tr7); // Id
		Node id15 = bl7;
		Node id16 = br7;
		Node id17 = tl7;
		Node id18 = tr7;
		
		Node id100078730 = network.getNodes().get(Id.createNodeId("100078730"));
		
		//(8) Connection to Storkower Strasse
		
		Node id27195069 = network.getNodes().get(Id.createNodeId("27195069"));

		allowedModes.add(TransportMode.car);

		// create links
		System.out.println("Creating Links..." );
		
		// --------------------------------------------------------------------------
		
		// (1) Connection to existing A100 (Verbindung zur bestehenden A100) + (3) HAS Grenzallee
		
		Link link12 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), id596372782, id4348866522);
		link12.setLength(280); //estimated with Via
		setMotorwayAttributesAndAddLink(link12, network);
		link12.setFreespeed(13.888888); // 50 km/h
		
			/*from Grenzallee-A113*/
			Link link13 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), id31390642, fromGrenzalleeToA113_b);
			setMotorwayAttributesAndAddLink(link13, network);
			link13.setLength(35); //estimated with Via
			
			// (2) Reopening of Grenzallee (Aufhebung Sperrung Grenzallee) - Creation of 2 links to reopen Grenzallee street
			
			Link link10 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), id31390642, id261694008);
			link10.setLength(280); //estimated with Via
			link10.setFreespeed(13.888888);
			setMotorwayAttributesAndAddLink(link10, network);
			
			Link link11 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), id1039457368, id27555275);
			link11.setLength(330); //estimated with Via
			setMotorwayAttributesAndAddLink(link11, network);
		
			//\DCberfahrt Bergiusstrasse von A100 abkoppeln
			
			Link link16 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), id1, id31390642);
			setMotorwayAttributesAndAddLink(link16, network);
			link16.setLength(35); //estimated with Via
			
			Link link17 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), id1, id4348866522);
			setMotorwayAttributesAndAddLink(link17, network); // Id 645
			link17.setLength(135); //estimated with Via
			link17.setFreespeed(13.888888); // 50 km/h
			
			Link link18 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), id4348866522, id1);
			setMotorwayAttributesAndAddLink(link18, network); // Id 780
			link18.setLength(135); //estimated with Via
			link18.setFreespeed(13.888888); // 50 km/h
			
			Link lid10077 = network.getLinks().get(Id.createLinkId("10077"));
			lid10077.setCapacity(0);
			Link lid10078 = network.getLinks().get(Id.createLinkId("10078"));
			lid10078.setCapacity(0);
			Link lid75754 = network.getLinks().get(Id.createLinkId("75754"));
			lid75754.setCapacity(0); 
			Link lid75753 = network.getLinks().get(Id.createLinkId("75753"));
			lid75753.setCapacity(0);
			Link lid75760 = network.getLinks().get(Id.createLinkId("75760"));
			lid75760.setCapacity(0);
			Link lid75759 = network.getLinks().get(Id.createLinkId("75759"));
			lid75759.setCapacity(0);
		
			//Abfahrt Richtung A113
		
			Link link19 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), id1, id2);
			setMotorwayAttributesAndAddLink(link19, network); // Id
			link19.setLength(125); //estimated with Via
			link19.setFreespeed(16.666666); // 60 km/h
			link19.setNumberOfLanes(1);
			
			Link link20 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), id2, id206191220);
			setMotorwayAttributesAndAddLink(link20, network); // Id
			link20.setLength(125); //estimated with Via
			link20.setFreespeed(16.666666); // 60 km/h
			link20.setNumberOfLanes(1);

			
			Link link21 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), id27542427, id2);
			setMotorwayAttributesAndAddLink(link21, network); // Id
			link21.setLength(75); //estimated with Via
			link21.setFreespeed(16.666666); // 60 km/h
			link21.setNumberOfLanes(1);

			Link lid435 = network.getLinks().get(Id.createLinkId("435"));
			lid435.setCapacity(0);
			Link lid15452 = network.getLinks().get(Id.createLinkId("15452"));
			lid15452.setCapacity(0);
			
			//Margarete-Kubicka-Br\FCcke
			
			Link link22 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), id4075503905, id4075503901);
			setMotorwayAttributesAndAddLink(link22, network); // Id
			link22.setLength(93); //estimated with Via
			link22.setFreespeed(13.888888); // 50 km/h
			link22.setNumberOfLanes(2);
			
			Link link23 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), id4075503901, id4075503905);
			setMotorwayAttributesAndAddLink(link23, network); // Id
			link23.setLength(93); //estimated with Via
			link23.setFreespeed(13.888888); // 50 km/h
			link23.setNumberOfLanes(2);
			
		//(4) AS Sonnenallee
			
		Link link24 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), id27542432, id4);
		setMotorwayAttributesAndAddLink(link24, network); // Id
		link24.setLength(490); //estimated with Via
		
		Link link25 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), id4, id5332081036);
		setMotorwayAttributesAndAddLink(link25, network); // Id
		link25.setLength(280); //estimated with Via
		link25.setFreespeed(16.666666); // 60 km/h
		link25.setNumberOfLanes(1);
		
		Link link33 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), id5332081036, id3386901041);
		setMotorwayAttributesAndAddLink(link33, network); // Id
		link33.setLength(5); //estimated with Via
		link33.setFreespeed(13.888888); // 50 km/h
		link33.setNumberOfLanes(2);
		
		Link link26 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), id3386901041, id6);
		setMotorwayAttributesAndAddLink(link26, network); // Id
		link26.setLength(280); //estimated with Via
		link26.setFreespeed(16.666666); // 60 km/h
		link26.setNumberOfLanes(1);
		
		Link link27 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), id4, id6);
		setMotorwayAttributesAndAddLink(link27, network); // Id
		link27.setLength(570); //estimated with Via
		
		//-----------------------------
		
		Link link28 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), id5, id3);
		setMotorwayAttributesAndAddLink(link28, network); // Id
		link28.setLength(575); //estimated with Via
		
		Link link29 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), id5, id3386901047);
		setMotorwayAttributesAndAddLink(link29, network); // Id
		link29.setLength(270); //estimated with Via
		link29.setFreespeed(16.666666); // 60 km/h
		link29.setNumberOfLanes(1);
		
		Link link30 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), id3386901047, id5332081037);
		setMotorwayAttributesAndAddLink(link30, network); // Id
		link30.setLength(10); //estimated with Via
		link30.setFreespeed(13.888888); // 50 km/h
		link30.setNumberOfLanes(2);
		
		Link link31 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), id5332081037, id3);
		setMotorwayAttributesAndAddLink(link31, network); // Id
		link31.setLength(290); //estimated with Via
		link31.setFreespeed(13.888888); // 60 km/h
		link31.setNumberOfLanes(1);
		
		Link link32 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), id3, id27542414);
		setMotorwayAttributesAndAddLink(link32, network); // Id
		link32.setLength(470); //estimated with Via
		
		//(5) AS Treptower Park
		
		Link link15 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), id287932598, id20246103);
		setMotorwayAttributesAndAddLink(link15, network); // Id
		link15.setLength(75); //estimated with Via
		link15.setFreespeed(13.888888); // 50 km/h
		link15.setNumberOfLanes(3);
		
		Link link34 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), id6, id8);
		setMotorwayAttributesAndAddLink(link34, network); // Id
		link34.setLength(2000); //estimated with Via
		
		Link link35 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), id7, id5);
		setMotorwayAttributesAndAddLink(link35, network); // Id
		link35.setLength(2000); //estimated with Via
		
		Link link36 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), id244430240, id287932598);
		setMotorwayAttributesAndAddLink(link36, network); // Id
		link36.setLength(355); //estimated with Via
		link36.setFreespeed(13.888888); // 50 km/h
		link36.setNumberOfLanes(3);
		
		Link link37 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), id287932598 , id7);
		setMotorwayAttributesAndAddLink(link37, network); // Id
		link37.setLength(200); //estimated with Via
		link37.setFreespeed(13.888888); // 50 km/h
		link37.setNumberOfLanes(1);
		
		Link link38 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), id8 , id287932598);
		setMotorwayAttributesAndAddLink(link38, network); // Id
		link38.setLength(200); //estimated with Via
		link38.setFreespeed(13.888888); // 50 km/h
		link38.setNumberOfLanes(1);
		
		Link link39 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), id9 , id287932598);
		setMotorwayAttributesAndAddLink(link39, network); // Id
		link39.setLength(200); //estimated with Via
		link39.setFreespeed(13.888888); // 50 km/h
		link39.setNumberOfLanes(1);
		
		Link link40 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), id287932598 , id10);
		setMotorwayAttributesAndAddLink(link40, network); // Id
		link40.setLength(200); //estimated with Via
		link40.setFreespeed(13.888888); // 50 km/h
		link40.setNumberOfLanes(1);
		
		Link link41 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), id8, id10);
		setMotorwayAttributesAndAddLink(link41, network); // Id
		link41.setLength(350); //estimated with Via
		
		Link link42 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), id9, id7);
		setMotorwayAttributesAndAddLink(link42, network); // Id
		link42.setLength(360); //estimated with Via
		
		//(6) AS Ostkreuz + Part Treptower Park - Ostkreuz
		
		Link link43 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), id12, id14);
		setMotorwayAttributesAndAddLink(link43, network); // Id
		link43.setLength(420); //estimated with Via
		
		Link link44 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), id13, id11);
		setMotorwayAttributesAndAddLink(link44, network); // Id
		link44.setLength(425); //estimated with Via
		
		Link link45 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), id10, id12);
		setMotorwayAttributesAndAddLink(link45, network); // Id
		link45.setLength(1100); //estimated with Via
		
		Link link46 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), id11, id9);
		setMotorwayAttributesAndAddLink(link46, network); // Id
		link46.setLength(1100); //estimated with Via
		
		Link link47 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), id2395404884 , id11);
		setMotorwayAttributesAndAddLink(link47, network); // Id
		link47.setLength(250); //estimated with Via
		link47.setFreespeed(13.888888); // 50 km/h
		link47.setNumberOfLanes(1);
		
		Link link48 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), id13 , id2395404884);
		setMotorwayAttributesAndAddLink(link48, network); // Id
		link48.setLength(250); //estimated with Via
		link48.setFreespeed(13.888888); // 50 km/h
		link48.setNumberOfLanes(1);
		
		Link link49 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), id12 , id2395404884);
		setMotorwayAttributesAndAddLink(link49, network); // Id
		link49.setLength(250); //estimated with Via
		link49.setFreespeed(13.888888); // 50 km/h
		link49.setNumberOfLanes(1);
		
		Link link50 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), id2395404884 , id14);
		setMotorwayAttributesAndAddLink(link50, network); // Id
		link50.setLength(250); //estimated with Via
		link50.setFreespeed(13.888888); // 50 km/h
		link50.setNumberOfLanes(1);
		
		
		//(7) AS Frankfurter Allee + Part Ostkreuz - Frankfurter Allee
		
		Link link58 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), id16, id18);
		setMotorwayAttributesAndAddLink(link58, network); // Id
		link58.setLength(450); //estimated with Via
		
		Link link51 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), id17, id15);
		setMotorwayAttributesAndAddLink(link51, network); // Id
		link51.setLength(450); //estimated with Via
		
		Link link52 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), id14, id16);
		setMotorwayAttributesAndAddLink(link52, network); // Id
		link52.setLength(1000); //estimated with Via
		
		Link link53 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), id15, id13);
		setMotorwayAttributesAndAddLink(link53, network); // Id
		link53.setLength(1000); //estimated with Via
		
		Link link54 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), id100078730, id18);
		setMotorwayAttributesAndAddLink(link54, network); // Id
		link54.setLength(100); //estimated with Via
		link54.setFreespeed(13.888888); // 50 km/h
		link54.setNumberOfLanes(1);
		
		Link link55 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), id16, id100078730);
		setMotorwayAttributesAndAddLink(link55, network); // Id
		link55.setLength(400); //estimated with Via
		link55.setFreespeed(13.888888); // 50 km/h
		link55.setNumberOfLanes(1);
		
		Link link56 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), id17, id100078730);
		setMotorwayAttributesAndAddLink(link56, network); // Id
		link56.setLength(100); //estimated with Via
		link56.setFreespeed(13.888888); // 50 km/h
		link56.setNumberOfLanes(1);
		
		Link link57 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), id100078730, id15);
		setMotorwayAttributesAndAddLink(link57, network); // Id
		link57.setLength(400); //estimated with Via
		link57.setFreespeed(13.888888); // 50 km/h
		link57.setNumberOfLanes(1);
		
		// (8) Finish - Part Frankfurter Allee - Storkower Strasse
		
		Link link59 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), id18, id27195069);
		setMotorwayAttributesAndAddLink(link59, network); // Id
		link59.setLength(950); //estimated with Via
		
		Link link60 = factory.createLink(Id.createLinkId(getFirstFreeLinkId(network)), id27195069, id17);
		setMotorwayAttributesAndAddLink(link60, network); // Id
		link60.setLength(950); //estimated with Via

		// example for closing link (change to the desired linkId):
		// network.getLinks().get(Id.createLinkId("16578")).setCapacity(0);

		new NetworkWriter(network).write(outputNetwork.toString());
		System.out.println("Network written successfully!");
	}
	
}