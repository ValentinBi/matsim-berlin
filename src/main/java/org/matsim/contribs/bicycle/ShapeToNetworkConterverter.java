/**
 * 
 */
package org.matsim.contribs.bicycle;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.geotools.feature.simple.SimpleFeatureTypeImpl;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;

/**
 * @author asasidharan
 *
 */
public class ShapeToNetworkConterverter {

	// private static String epsg = "EPSG:3006";
	private static Path BERLIN_NETWORK = Paths.get("./input/berlin-v5-network.xml.gz");

	private final static String NETWORK_SHAPE_FILE = "./input/Y-Trasse_Vorzugstrasse.shp";

	private static final String GRADIENT = "gradient";
	private static final String AVERAGE_ELEVATION = "averageElevation";
	private static final String SURFACE = "surface";
	private static final String SMOOTHNESS = "smoothness";
	private static final String CYCLEWAY = "cycleway";
	private static final String WAY_TYPE = "type";
	private static final String BICYCLE_INFRASTRUCTURE_SPEED_FACTOR = "bicycleInfrastructureSpeedFactor";

	private static final Map<Id<Node>, Coord> coordinateMap = new ConcurrentHashMap<Id<Node>, Coord>();

	ArrayList<Coord> coordinateList = new ArrayList<Coord>();
	ArrayList<Coord> duplicateCoordinates = new ArrayList<Coord>();
	Set<Coord> coordSet = new HashSet<Coord>();
	ArrayList<ArrayList<Id<Node>>> nodePair = new ArrayList<ArrayList<Id<Node>>>();
	ArrayList<ArrayList<Link>> linksToChange = new ArrayList<ArrayList<Link>>();
	Set<Id<Node>> nodesToRemove = new HashSet<Id<Node>>();

	Network net = NetworkUtils.createNetwork();
	NetworkFactory fac = net.getFactory();

	static Network berlinNet = NetworkUtils.createNetwork();

	public static void main(String[] args) {

		new ShapeToNetworkConterverter().create();
	}

	private void create() {

		new MatsimNetworkReader(berlinNet).readFile(BERLIN_NETWORK.toString());

		ShapeFileReader reader = new ShapeFileReader();
		Collection<SimpleFeature> features = reader.readFileAndInitialize(NETWORK_SHAPE_FILE);

		for (SimpleFeature sf : features) {

			ArrayList<Node> node = new ArrayList<Node>();
			if (sf.getFeatureType() instanceof SimpleFeatureTypeImpl) {
				System.out.println(sf.getAttribute(0));
				String[] geom = sf.getAttribute(0).toString().split("MULTILINESTRING");
				String[] splitgeom = geom[1].split(",");
				for (String rawcood : splitgeom) {

					String cood = rawcood.replaceAll("[\\[\\](){}]", "");

					String[] xy = cood.trim().split("\\s+");
					String x = xy[0];
					String y = xy[1];
					double x_double = Double.parseDouble(x);
					double y_double = Double.parseDouble(y);

					Coord coordinates = new Coord(x_double, y_double);
					Node newNode = fac.createNode(Id.createNodeId(getFirstFreeNodeId(berlinNet, net)), coordinates);
					node.add(newNode);
					net.addNode(newNode);
					coordinateMap.put(newNode.getId(), coordinates);
					coordinateList.add(coordinates);

				}

				createLink(node, berlinNet);

			}
		}
		//finding the coordinates having multiple nodes.
		for (Coord coor : coordinateList) {
			if (!coordSet.add(coor)) {
				duplicateCoordinates.add(coor);
			}
		}
		//removing all the coordinates having a single node from coordinateMap so that the final coordinateMap contains only the coordinates having multiple nodes
		for (Entry<Id<Node>, Coord> entry : coordinateMap.entrySet()) {
			if (!duplicateCoordinates.contains(entry.getValue())) {
				coordinateMap.remove(entry.getKey());
			}
		}

		// getting the nodes in same coordinates
		Iterator<Id<Node>> keyItr1 = coordinateMap.keySet().iterator();

		while (keyItr1.hasNext()) {

			Id<Node> key1 = keyItr1.next();
			Iterator<Id<Node>> keyItr2 = coordinateMap.keySet().iterator();
			while (keyItr2.hasNext()) {

				Id<Node> key2 = keyItr2.next();
				if (coordinateMap.get(key1).equals(coordinateMap.get(key2)) && key1 != key2) {
					ArrayList<Id<Node>> nodes = new ArrayList<Id<Node>>();
					nodes.add(key1);
					nodes.add(key2);
					nodePair.add(nodes);
					System.out.println("pair " + key1 + " " + key2);
				}
			}
		}

		// some coordinates are having multiple nodes, so reconnecting the to and from
		// nodes
		Iterator<ArrayList<Id<Node>>> duplicateNode = nodePair.iterator();
		while (duplicateNode.hasNext()) {

			ArrayList<Id<Node>> nodes = duplicateNode.next();
			Iterator<Id<Link>> linkItr = net.getLinks().keySet().iterator();
			while (linkItr.hasNext()) {
				Id<Link> linkid = linkItr.next();
				Link link = net.getLinks().get(linkid);

				if (nodes.contains(link.getFromNode().getId())) {
					link.setFromNode(net.getNodes().get(nodes.get(0)));
					nodesToRemove.add(nodes.get(1));
				} else if (nodes.contains(link.getToNode().getId())) {
					link.setToNode(net.getNodes().get(nodes.get(0)));
					nodesToRemove.add(nodes.get(1));
				}

			}

		}

		// removeRepeatNodes(nodesToRemove);
		Path output = Paths.get("D:/Scientific_Computing/work/shape file conversion/output");
		new NetworkWriter(net).write(output.resolve("network1.xml").toString());

	}

	private void createLink(ArrayList<Node> node, Network berlinNet) {

		int size = node.size();

		for (int x = 0; x < size; x++) {
			Node from = node.get(x);
			if (x < size - 1) {
				Node to = node.get(x + 1);
				Link link = fac.createLink(Id.createLinkId(getFirstFreeLinkId(berlinNet, net)), from, to);
				setLinkAttributes(link);
				net.addLink(link);
			}

		}

	}

	private static long getFirstFreeNodeId(Network baseNetwork, Network generatedNetwork) {
		long counter = Long.parseLong("1");
		while (!(baseNetwork.getNodes().get(Id.createNodeId(counter)) == null)
				|| !(generatedNetwork.getNodes().get(Id.createNodeId(counter)) == null)) {
			counter++;
		}
		System.out.println("NodeId: " + counter);
		return counter;

	}

	private static long getFirstFreeLinkId(Network baseNetwork, Network generatedNetwork) {
		long counter = Long.parseLong("1");
		while (!(baseNetwork.getLinks().get(Id.createLinkId(counter)) == null)
				|| !(generatedNetwork.getLinks().get(Id.createLinkId(counter)) == null)) {
			counter++;
		}
		System.out.println("LinkID: " + counter);
		return counter;

	}

	private static void setLinkAttributes(Link link) {

		link.getAttributes().putAttribute(SURFACE, "asphalt");
		link.getAttributes().putAttribute(BICYCLE_INFRASTRUCTURE_SPEED_FACTOR, 1.0);

	}

//	private void removeRepeatNodes(Set<Id<Node>> nodesToRemove2) {
//		Iterator<Id<Node>> nodes = nodesToRemove2.iterator();
//		
//		while(nodes.hasNext()) {
//			Id<Node> node = nodes.next();
//			System.out.println("Removing node "+node);
//			net.removeNode(node);
//		}
//	}

}
