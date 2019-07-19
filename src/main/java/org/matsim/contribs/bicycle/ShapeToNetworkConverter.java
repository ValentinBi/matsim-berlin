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
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;

/**
 * @author asasidharan
 *
 */
public class ShapeToNetworkConverter {

	// private static String epsg = "EPSG:3006";
	private static Path BERLIN_NETWORK = Paths
			.get("./input/berlin-v5-network.xml.gz");

	//private final static String NETWORK_SHAPE_FILE = "./input/shapefiles/TOP12/TOP12.shp";
	private static Path NETWORK_SHAPE_FILE = Paths.get("./input/shapefiles/TOP12/TOP12.shp");

	private static final String GRADIENT = "gradient";
	private static final String AVERAGE_ELEVATION = "averageElevation";
	private static final String SURFACE = "surface";
	private static final String SMOOTHNESS = "smoothness";
	private static final String CYCLEWAY = "cycleway";
	private static final String WAY_TYPE = "type";
	private static final String BICYCLE_INFRASTRUCTURE_SPEED_FACTOR = "bicycleInfrastructureSpeedFactor";
	private final static CoordinateTransformation CT = TransformationFactory.getCoordinateTransformation("EPSG:3006",
			"EPSG:31468");
	// TransformationFactory.GK4
	private static final Map<Id<Node>, Coord> coordinateMap = new ConcurrentHashMap<Id<Node>, Coord>();

	ArrayList<Coord> coordinateList = new ArrayList<Coord>();
	ArrayList<Coord> duplicateCoordinates = new ArrayList<Coord>();
	Set<Coord> coordSet = new HashSet<Coord>();
	ArrayList<Set<Id<Node>>> nodePair = new ArrayList<Set<Id<Node>>>();
	ArrayList<ArrayList<Link>> linksToChange = new ArrayList<ArrayList<Link>>();
	Set<Id<Node>> nodesPair = new HashSet<Id<Node>>();

	Network net = NetworkUtils.createNetwork();
	NetworkFactory fac = net.getFactory();

	static Network berlinNet = NetworkUtils.createNetwork();

	public static void main(String[] args) {

		new ShapeToNetworkConverter().create();
	}

	private void create() {

		// new
		//new MatsimNetworkReader(berlinNet).readFile(BERLIN_NETWORK.toString());
		// new MatsimNetworkReader(net).readFile(BERLIN_NETWORK.toString());
		ShapeFileReader reader = new ShapeFileReader();
		Collection<SimpleFeature> features = reader.readFileAndInitialize(NETWORK_SHAPE_FILE.toString());
		ArrayList<Node> nodesToDelete = new ArrayList<Node>();

		for (SimpleFeature sf : features) {

			ArrayList<Node> node = new ArrayList<Node>();
			if (sf.getFeatureType() instanceof SimpleFeatureTypeImpl) {
				System.out.println(sf.getAttribute("the_geom"));
				String[] geom = sf.getAttribute("the_geom").toString().split("MULTILINESTRING");
				String[] splitgeom = geom[1].split(",");
				for (String rawcood : splitgeom) {

					String cood = rawcood.replaceAll("[\\[\\](){}]", "");

					String[] xy = cood.trim().split("\\s+");
					String x = xy[0];
					String y = xy[1];
					double x_double = Double.parseDouble(x);
					double y_double = Double.parseDouble(y);

					Coord coordinates = new Coord(x_double, y_double);
					// Node newNode = fac.createNode(Id.createNodeId(getFirstFreeNodeId(net)),
					// CT.transform(coordinates));
					Node newNode = fac.createNode(Id.createNodeId(getFirstFreeNodeId(net)), CT.transform(coordinates));
					node.add(newNode);
					net.addNode(newNode);
//					coordinateMap.put(newNode.getId(), CT.transform(coordinates));
//					coordinateList.add(CT.transform(coordinates));
					coordinateMap.put(newNode.getId(), CT.transform(coordinates));
					coordinateList.add(CT.transform(coordinates));

				}

				createLink(node);

			}
		}
		// finding the coordinates having multiple nodes.
		for (Coord coor : coordinateList) {
			if (!coordSet.add(coor)) {
				duplicateCoordinates.add(coor);
			}
		}
		// removing all the coordinates having a single node from coordinateMap
		// so that the final coordinateMap contains only the coordinates having
		// multiple nodes
		for (Entry<Id<Node>, Coord> entry : coordinateMap.entrySet()) {
			if (!duplicateCoordinates.contains(entry.getValue())) {
				coordinateMap.remove(entry.getKey());
			}
		}

		Iterator<Id<Node>> keyItr3 = coordinateMap.keySet().iterator();
		while (keyItr3.hasNext()) {
			Id<Node> id = keyItr3.next();
			Coord coor = coordinateMap.get(id);
			System.out.println("Node id " + id + " " + "coordinate " + coor);
		}
		// getting the nodes in same coordinates
		Iterator<Id<Node>> keyItr1 = coordinateMap.keySet().iterator();

		while (keyItr1.hasNext()) {

			Id<Node> key1 = keyItr1.next();
			Iterator<Id<Node>> keyItr2 = coordinateMap.keySet().iterator();
			while (keyItr2.hasNext()) {

				Id<Node> key2 = keyItr2.next();
				if (coordinateMap.get(key1).equals(coordinateMap.get(key2)) && key1 != key2) {
					Set<Id<Node>> nodes = new HashSet<Id<Node>>();

					if (nodesPair.add(key1) && nodesPair.add(key2)) {
						nodes.add(key1);
						nodes.add(key2);
						nodePair.add(nodes);
						System.out.println("pair " + key1 + " " + key2);
					}
				}
			}
		}

		// remove the duplicate nodes completely and create a new link with new nodes
		Iterator<Set<Id<Node>>> pair = nodePair.listIterator();

		while (pair.hasNext()) {
			Set<Id<Node>> nodeSet = pair.next();
			Iterator<Id<Node>> eachNode = nodeSet.iterator();
			while (eachNode.hasNext()) {
				Id<Node> nodeId = eachNode.next();
				Node node = net.getNodes().get(nodeId);
				nodesToDelete.add(node);

				Map<Id<Link>, ? extends Link> inlinks = node.getInLinks();
				Iterator<Id<Link>> inLinksKeys = inlinks.keySet().iterator();

				Map<Id<Link>, ? extends Link> outlinks = node.getOutLinks();
				Iterator<Id<Link>> outLinksKeys = outlinks.keySet().iterator();

				while (inLinksKeys.hasNext()) {
					Id<Link> linkid = inLinksKeys.next();
					changeToNode(linkid);
				}

				while (outLinksKeys.hasNext()) {
					Id<Link> linkid = outLinksKeys.next();
					changeFromNode(linkid);
				}

			}
		}

		removeUnusedNodes(nodesToDelete);
		
		Path output = Paths.get("./input/");

		//Path output = Paths.get("D:/Scientific_Computing/work/shape file conversion/output");
		new NetworkWriter(net).write(output.resolve("shapefile_network.xml").toString());

	}

	private void createLink(ArrayList<Node> node) {

		int size = node.size();

		for (int x = 0; x < size; x++) {
			Node from = node.get(x);
			if (x < size - 1) {
				Node to = node.get(x + 1);
				Link link = fac.createLink(Id.createLinkId(getFirstFreeLinkId(net)), from, to);
				setLinkAttributes(link);
				net.addLink(link);
			}

		}

	}

	private static long getFirstFreeNodeId(Network network) {
		long counter = Long.parseLong("1");
		while (!(network.getNodes().get(Id.createNodeId(counter)) == null)) {
			counter++;
		}
		System.out.println("NodeId: " + counter);
		return counter;

	}

	private static long getFirstFreeLinkId(Network network) {
		long counter = Long.parseLong("1");
		while (!(network.getLinks().get(Id.createLinkId(counter)) == null)) {
			counter++;
		}
		System.out.println("LinkID: " + counter);
		return counter;

	}

	private static void setLinkAttributes(Link link) {

		Set<String> allowedModes = new HashSet<>();
		allowedModes.add("bicycle");
		link.getAttributes().putAttribute(SURFACE, "asphalt");
		link.getAttributes().putAttribute(BICYCLE_INFRASTRUCTURE_SPEED_FACTOR, 1.0);
		link.setAllowedModes(allowedModes);

	}

	private void removeUnusedNodes(ArrayList<Node> nodes) {

		Iterator<Node> nodeItr = nodes.iterator();

		while (nodeItr.hasNext()) {
			Node node = nodeItr.next();
			net.removeNode(node.getId());
			System.out.println("removing nodes....... " + node.getId());
		}

	}

	private void changeToNode(Id<Link> linkid) {

		Node toNode = null;
		Node fromNode = net.getLinks().get(linkid).getFromNode();
		Coord toCord = net.getLinks().get(linkid).getToNode().getCoord();
		toNode = fac.createNode(Id.createNodeId(getFirstFreeNodeId(net)), toCord);
		net.addNode(toNode);
		createLink(fromNode, toNode);

	}

	private void changeFromNode(Id<Link> linkid) {

		Node fromNode = null;
		Node toNode = net.getLinks().get(linkid).getToNode();
		Coord fromCord = net.getLinks().get(linkid).getFromNode().getCoord();
		fromNode = fac.createNode(Id.createNodeId(getFirstFreeNodeId(net)), fromCord);
		net.addNode(fromNode);
		createLink(fromNode, toNode);

	}

	private void createLink(Node fromNode, Node toNode) {

		Link link = fac.createLink(Id.createLinkId(getFirstFreeLinkId(net)), fromNode, toNode);
		setLinkAttributes(link);
		net.addLink(link);

	}

}