package org.matsim.analysis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.contribs.bicycle.BikeLinkSpeedCalculator;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.matsim.core.utils.io.MatsimXmlParser;
import org.opengis.feature.simple.SimpleFeature;

public class RunBikeAnalysis {
	
	public static void main(String[] args) throws IOException {
		Path networkpath = Paths.get("./input/mergedNetwork.xml.gz");
		Path baseCaseEventsPath = Paths.get("./output/bike_basecase/ITERS/it.300/berlin-v5.3-1pct-bike_basecase.300.events.xml.gz");
		Path policyCaseEventsPath = Paths.get("./output/bike_highways/ITERS/it.300/berlin-v5.3-1pct-bike_highways.300.events.xml.gz");
		Path shapeHundekopf = Paths.get("./input/shapefiles/Hundekopf/Hundekopf.shp");
		//Path shapeBerlin = Paths.get("./input/shapefiles/Bezirksgrenzen/bezirksgrenzen.shp");
		Path shapeBerlin = Paths.get("./input/shapefiles/Bezirke/Bezirke_GK4.shp");
		Path planfile = Paths.get("./output/bike_highways/berlin-v5.3-1pct-bike_highways.output_plans.xml.gz");
		Path planfileBase = Paths.get("./output/bike_basecase/berlin-v5.3-1pct-bike_basecase.output_plans.xml.gz");
		Path outplanHundekopf = Paths.get("./output/bike_highways/plansHundekopf.xml.gz");
		Path outplanBerlin = Paths.get("./output/bike_highways/plansBerlin.xml.gz");
		
		

		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		PopulationReader populationReader = new PopulationReader(scenario);
		populationReader.readFile(planfile.toString());
		Population population = scenario.getPopulation();
		
		ArrayList<Plan> plans = new ArrayList<Plan>();
		Set<Id<Person>> persons = population.getPersons().keySet();
		System.out.println("Persons size: "+persons.size());
		for (Id<Person> person : persons) {
			 List<? extends Plan> plansOfPerson = population.getPersons().get(person).getPlans();
				//System.out.println("PersonPlans size: "+plansOfPerson.size());
			 Iterator<? extends Plan> plansOfPersonItr = plansOfPerson.iterator();
			 while (plansOfPersonItr.hasNext()) {
				 plans.add(plansOfPersonItr.next());
			 }
		}

		Double bikeDistancePolicy = 0.;
		int shortBikeTripsPolicy = 0;
		int middleBikeTripsPolicy = 0;
		int longBikeTripsPolicy = 0;
		int legcounter = 0;
		for(Plan plan: plans) {
			List<Leg> legs = PopulationUtils.getLegs(plan);
			
			for(Leg leg: legs) {
				legcounter ++;
				if (leg.getMode().equals("bicycle")) {
					Double distance = leg.getRoute().getDistance();
					bikeDistancePolicy += distance;
					if(distance<2000) {
						shortBikeTripsPolicy ++;
					} else if (distance<5000) {
						middleBikeTripsPolicy ++;
					} else {
						longBikeTripsPolicy ++;
					}
				}
			}
		}
		System.out.println("Legs size: "+legcounter);

		Scenario scenario1 = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		PopulationReader populationReader1 = new PopulationReader(scenario1);
		populationReader1.readFile(planfileBase.toString());
		Population populationBase = scenario1.getPopulation();
		
		ArrayList<Plan> plansBase = new ArrayList<Plan>();
		Set<Id<Person>> personsBase = populationBase.getPersons().keySet();
		for (Id<Person> person : personsBase) {
			 List<? extends Plan> plansOfPerson = populationBase.getPersons().get(person).getPlans();
			 Iterator<? extends Plan> plansOfPersonItr = plansOfPerson.iterator();
			 while (plansOfPersonItr.hasNext()) {
				 plansBase.add(plansOfPersonItr.next());
			 }
		}
		Double bikeDistanceBase = 0.;
		int shortBikeTripsBase = 0;
		int middleBikeTripsBase = 0;
		int longBikeTripsBase = 0;
		for(Plan plan: plansBase) {
			List<Leg> legs = PopulationUtils.getLegs(plan);
			for(Leg leg: legs) {
				if (leg.getMode().equals("bicycle")) {
					Double distance = leg.getRoute().getDistance();
					bikeDistanceBase += distance;
					if(distance<2000) {
						shortBikeTripsBase ++;
					} else if (distance<5000) {
						middleBikeTripsBase ++;
					} else {
						longBikeTripsBase ++;
					}
				}
			}
		}
		
		
		File f = new File(outplanBerlin.toString());
		File f1 = new File(outplanHundekopf.toString());
		if(!f.exists() || !f1.exists()) { 
			Map<String, Geometry> hundekopf = readShapeFile(shapeHundekopf.toString());
			Map<String, Geometry> berlin = readShapeFile(shapeBerlin.toString());
			
			createNewPopulation(outplanHundekopf.toString(), hundekopf, population);
			createNewPopulation(outplanBerlin.toString(), berlin, population);
		}
		
		
		
		Scenario scenario2 = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		PopulationReader populationReader2 = new PopulationReader(scenario2);
		populationReader2.readFile(outplanHundekopf.toString());
		Population population2 = scenario2.getPopulation();
		
		Scenario scenario3 = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		PopulationReader populationReader3 = new PopulationReader(scenario3);
		populationReader3.readFile(outplanBerlin.toString());
		Population population3 = scenario3.getPopulation();
		
		Set<Id<Person>> personsLivingInBerlin = population3.getPersons().keySet();
		Set<Id<Person>> personsLivingInHundekopf = population2.getPersons().keySet();
		
		ArrayList<Plan> plansPolicyBerlin = new ArrayList<Plan>();
		//System.out.println("Person size: "+persons.size());
		for (Id<Person> person : personsLivingInBerlin) {
			 List<? extends Plan> plansOfPerson = population.getPersons().get(person).getPlans();
				//System.out.println("PersonPlans size: "+plansOfPerson.size());
			 Iterator<? extends Plan> plansOfPersonItr = plansOfPerson.iterator();
			 while (plansOfPersonItr.hasNext()) {
				 plansPolicyBerlin.add(plansOfPersonItr.next());
			 }
		}

		Double bikeDistanceBerlinPolicy = 0.;
		for(Plan plan: plansPolicyBerlin) {
			List<Leg> legs = PopulationUtils.getLegs(plan);
			for(Leg leg: legs) {
				if (leg.getMode().equals("bicycle")) {
					bikeDistanceBerlinPolicy += leg.getRoute().getDistance();
				}
			}
		}

		ArrayList<Plan> plansBaseBerlin = new ArrayList<Plan>();
		for (Id<Person> person : personsLivingInBerlin) {
			 List<? extends Plan> plansOfPerson = populationBase.getPersons().get(person).getPlans();
				//System.out.println("PersonPlans size: "+plansOfPerson.size());
			 Iterator<? extends Plan> plansOfPersonItr = plansOfPerson.iterator();
			 while (plansOfPersonItr.hasNext()) {
				 plansBaseBerlin.add(plansOfPersonItr.next());
			 }
		}

		Double bikeDistanceBerlinBase = 0.;
		for(Plan plan: plansBaseBerlin) {
			List<Leg> legs = PopulationUtils.getLegs(plan);
			for(Leg leg: legs) {
				if (leg.getMode().equals("bicycle")) {
					bikeDistanceBerlinBase += leg.getRoute().getDistance();
				}
			}
		}
		
		ArrayList<Plan> plansPolicyHundekopf = new ArrayList<Plan>();
		//System.out.println("Person size: "+persons.size());
		for (Id<Person> person : personsLivingInHundekopf) {
			 List<? extends Plan> plansOfPerson = population.getPersons().get(person).getPlans();
				//System.out.println("PersonPlans size: "+plansOfPerson.size());
			 Iterator<? extends Plan> plansOfPersonItr = plansOfPerson.iterator();
			 while (plansOfPersonItr.hasNext()) {
				 plansPolicyHundekopf.add(plansOfPersonItr.next());
			 }
		}

		Double bikeDistanceHundekopfPolicy = 0.;
		for(Plan plan: plansPolicyHundekopf) {
			List<Leg> legs = PopulationUtils.getLegs(plan);
			for(Leg leg: legs) {
				if (leg.getMode().equals("bicycle")) {
					bikeDistanceHundekopfPolicy += leg.getRoute().getDistance();
				}
			}
		}

		ArrayList<Plan> plansBaseHundekopf = new ArrayList<Plan>();
		for (Id<Person> person : personsLivingInHundekopf) {
			 List<? extends Plan> plansOfPerson = populationBase.getPersons().get(person).getPlans();
				//System.out.println("PersonPlans size: "+plansOfPerson.size());
			 Iterator<? extends Plan> plansOfPersonItr = plansOfPerson.iterator();
			 while (plansOfPersonItr.hasNext()) {
				 plansBaseHundekopf.add(plansOfPersonItr.next());
			 }
		}

		Double bikeDistanceHundekopfBase = 0.;
		for(Plan plan: plansBaseBerlin) {
			List<Leg> legs = PopulationUtils.getLegs(plan);
			for(Leg leg: legs) {
				if (leg.getMode().equals("bicycle")) {
					bikeDistanceHundekopfBase += leg.getRoute().getDistance();
				}
			}
		}
		/*
		Path listOfAddedLinksFilePath = Paths.get("./output/bicycleHighwayLinks.txt");
		File listOfAddedLinks = new File(listOfAddedLinksFilePath.toString());
*/
		// read in the simulation network
		Network network = NetworkUtils.createNetwork();
		new MatsimNetworkReader(network).readFile(networkpath.toString());
		
		
		
/*		
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
		*/
		TravelDistanceEventHandler travelDistanceEventHandlerPolicy = new TravelDistanceEventHandler(network);
		TravelTimeEventHandler travelTimeEventHandlerPolicy = new TravelTimeEventHandler();
		BicycleTravelTimeEventHandler bicycleTravelTimeEventHandlerPolicy = new BicycleTravelTimeEventHandler();
		AgentUsesLegModeEventHandler agentUsesBicycleEventHandlerPolicy = new AgentUsesLegModeEventHandler("bicycle");
		AgentUsesLegModeEventHandler agentUsesCarEventHandlerPolicy = new AgentUsesLegModeEventHandler(TransportMode.car);
		AgentUsesLegModeEventHandler agentUsesPtEventHandlerPolicy = new AgentUsesLegModeEventHandler(TransportMode.pt);
		//AgentUsesLegModeEventHandler agentUsesBikeEventHandlerPolicy = new AgentUsesLegModeEventHandler(TransportMode.bike);
		AgentUsesLegModeEventHandler agentWalksEventHandlerPolicy = new AgentUsesLegModeEventHandler("walk");
		BicycleTravelDistanceEventHandler bicycleTravelDistanceEventHandlerPolicy = new BicycleTravelDistanceEventHandler();
		
		EventsManager policyCaseManager = EventsUtils.createEventsManager();
		// policyCaseManager.addHandler(agentTravelledOnLinkEventHandler);
		policyCaseManager.addHandler(travelDistanceEventHandlerPolicy);
		policyCaseManager.addHandler(travelTimeEventHandlerPolicy);
		policyCaseManager.addHandler(agentUsesBicycleEventHandlerPolicy);
		policyCaseManager.addHandler(agentUsesCarEventHandlerPolicy);
		policyCaseManager.addHandler(agentUsesPtEventHandlerPolicy);
		policyCaseManager.addHandler(agentWalksEventHandlerPolicy);
		policyCaseManager.addHandler(bicycleTravelTimeEventHandlerPolicy);
		policyCaseManager.addHandler(bicycleTravelDistanceEventHandlerPolicy);
		
		

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
		BicycleTravelTimeEventHandler bicycleTravelTimeEventHandlerBase = new BicycleTravelTimeEventHandler();
		AgentUsesLegModeEventHandler agentUsesBicycleEventHandlerBase = new AgentUsesLegModeEventHandler("bicycle");
		AgentUsesLegModeEventHandler agentUsesCarEventHandlerBase = new AgentUsesLegModeEventHandler(TransportMode.car);
		AgentUsesLegModeEventHandler agentUsesPtEventHandlerBase = new AgentUsesLegModeEventHandler(TransportMode.pt);
		AgentUsesLegModeEventHandler agentWalksEventHandlerBase = new AgentUsesLegModeEventHandler("walk");
		BicycleTravelDistanceEventHandler bicycleTravelDistanceEventHandlerBase = new BicycleTravelDistanceEventHandler();
		
		EventsManager baseCaseManager = EventsUtils.createEventsManager();
		baseCaseManager.addHandler(agentUsesPtEventHandlerBase);
		baseCaseManager.addHandler(agentUsesCarEventHandlerBase);
		baseCaseManager.addHandler(agentUsesBicycleEventHandlerBase);
		baseCaseManager.addHandler(travelTimeEventHandlerBase);
		baseCaseManager.addHandler(travelDistanceEventHandlerBase);
		baseCaseManager.addHandler(agentWalksEventHandlerBase);
		baseCaseManager.addHandler(bicycleTravelTimeEventHandlerBase);
		baseCaseManager.addHandler(bicycleTravelDistanceEventHandlerBase);
		

		new MatsimEventsReader(baseCaseManager).readFile(baseCaseEventsPath.toString());
		
		Double bicycleRiderTravelTimePolicy = calculateTravelTimeOfSetOfAgents(agentUsesBicycleEventHandlerPolicy.getVehicleUsers(), travelTimeEventHandlerPolicy);
		Double bicycleRiderTravelTimeBase = calculateTravelTimeOfSetOfAgents(agentUsesBicycleEventHandlerBase.getVehicleUsers(), travelTimeEventHandlerBase);
		/*
		Double ridersOnHighwayTotalTravelTimePolicy = calculateTravelTimeOfSetOfAgents(agentTravelledOnLinkEventHandler.getPersonOnWatchedLinks(), travelTimeEventHandlerPolicy);
		System.out.println(agentTravelledOnLinkEventHandler.getPersonOnWatchedLinks());
		System.out.println(agentTravelledOnLinkEventHandler.getPersonOnWatchedLinks().size());
		Double ridersOnHighwayTotalTravelTimeBase = calculateTravelTimeOfSetOfAgents(agentTravelledOnLinkEventHandler.getPersonOnWatchedLinks(), travelTimeEventHandlerBase);
		*/
		
		Double bicycleRiderTravelDistancePolicy = calculateTravelDistanceOfSetOfAgents(agentUsesBicycleEventHandlerPolicy.getVehicleUsers(), travelDistanceEventHandlerPolicy);
		Double bicycleRiderTravelDistanceBase = calculateTravelDistanceOfSetOfAgents(agentUsesBicycleEventHandlerBase.getVehicleUsers(), travelDistanceEventHandlerBase);
		
		/*
		Double ridersOnHighwayTotalTravelDistancePolicy = calculateTravelDistanceOfSetOfAgents(agentTravelledOnLinkEventHandler.getPersonOnWatchedLinks(), travelDistanceEventHandlerPolicy);
		Double ridersOnHighwayTotalTravelDistanceBase = calculateTravelDistanceOfSetOfAgents(agentTravelledOnLinkEventHandler.getPersonOnWatchedLinks(), travelDistanceEventHandlerBase);
		*/
		
		
		int numberOfBicycleRidersLivingInBerlinPolicy = (int) agentUsesBicycleEventHandlerPolicy.getVehicleUsers().stream()
				.filter(person -> personsLivingInBerlin.contains(person)).count();
		int numberOfBicycleRidersLivingInBerlinBase = (int) agentUsesBicycleEventHandlerBase.getVehicleUsers().stream()
				.filter(person -> personsLivingInBerlin.contains(person)).count();
		int numberOfBicycleLegsOfBerlinersPolicy = 0;
		int numberOfBicycleLegsOfBerlinersBase = 0;
		int numberOfCarLegsOfBerlinersPolicy = 0;
		int numberOfCarLegsOfBerlinersBase = 0;
		int numberOfPtLegsOfBerlinersPolicy = 0;
		int numberOfPtLegsOfBerlinersBase = 0;
		int numberOfWalkLegsOfBerlinersPolicy = 0;
		int numberOfWalkLegsOfBerlinersBase = 0;
		for (Id<Person> person :personsLivingInBerlin) {
			if(agentUsesBicycleEventHandlerPolicy.getLegsPerPerson().get(person)!=null)
				numberOfBicycleLegsOfBerlinersPolicy += agentUsesBicycleEventHandlerPolicy.getLegsPerPerson().get(person);
			if(agentUsesBicycleEventHandlerBase.getLegsPerPerson().get(person)!=null)
				numberOfBicycleLegsOfBerlinersBase += agentUsesBicycleEventHandlerBase.getLegsPerPerson().get(person);
			if(agentUsesCarEventHandlerPolicy.getLegsPerPerson().get(person)!=null)
				numberOfCarLegsOfBerlinersPolicy += agentUsesCarEventHandlerPolicy.getLegsPerPerson().get(person);
			if(agentUsesCarEventHandlerBase.getLegsPerPerson().get(person)!=null)
				numberOfCarLegsOfBerlinersBase += agentUsesCarEventHandlerBase.getLegsPerPerson().get(person);
			if(agentUsesPtEventHandlerPolicy.getLegsPerPerson().get(person)!=null)
				numberOfPtLegsOfBerlinersPolicy += agentUsesPtEventHandlerPolicy.getLegsPerPerson().get(person);
			if(agentUsesPtEventHandlerBase.getLegsPerPerson().get(person)!=null)
				numberOfPtLegsOfBerlinersBase += agentUsesPtEventHandlerBase.getLegsPerPerson().get(person);
			if(agentWalksEventHandlerPolicy.getLegsPerPerson().get(person)!=null)
				numberOfWalkLegsOfBerlinersPolicy += agentWalksEventHandlerPolicy.getLegsPerPerson().get(person);
			if(agentWalksEventHandlerBase.getLegsPerPerson().get(person)!=null)
				numberOfWalkLegsOfBerlinersBase += agentWalksEventHandlerBase.getLegsPerPerson().get(person);
		}
		
		int numberOfBicycleLegsOfHundekopfPolicy = 0;
		int numberOfBicycleLegsOfHundekopfBase = 0;
		int numberOfCarLegsOfHundekopfPolicy = 0;
		int numberOfCarLegsOfHundekopfBase = 0;
		int numberOfPtLegsOfHundekopfPolicy = 0;
		int numberOfPtLegsOfHundekopfBase = 0;
		int numberOfWalkLegsOfHundekopfPolicy = 0;
		int numberOfWalkLegsOfHundekopfBase = 0;
		for (Id<Person> person :personsLivingInHundekopf) {
			if(agentUsesBicycleEventHandlerPolicy.getLegsPerPerson().get(person)!=null)
				numberOfBicycleLegsOfHundekopfPolicy += agentUsesBicycleEventHandlerPolicy.getLegsPerPerson().get(person);
			if( agentUsesBicycleEventHandlerBase.getLegsPerPerson().get(person)!=null)
				numberOfBicycleLegsOfHundekopfBase += agentUsesBicycleEventHandlerBase.getLegsPerPerson().get(person);
			if(agentUsesCarEventHandlerPolicy.getLegsPerPerson().get(person)!=null)
				numberOfCarLegsOfHundekopfPolicy += agentUsesCarEventHandlerPolicy.getLegsPerPerson().get(person);
			if(agentUsesCarEventHandlerBase.getLegsPerPerson().get(person)!=null)
				numberOfCarLegsOfHundekopfBase += agentUsesCarEventHandlerBase.getLegsPerPerson().get(person);
			if(agentUsesPtEventHandlerPolicy.getLegsPerPerson().get(person)!=null)
				numberOfPtLegsOfHundekopfPolicy += agentUsesPtEventHandlerPolicy.getLegsPerPerson().get(person);
			if(agentUsesPtEventHandlerBase.getLegsPerPerson().get(person)!=null)
				numberOfPtLegsOfHundekopfBase += agentUsesPtEventHandlerBase.getLegsPerPerson().get(person);
			if(agentWalksEventHandlerPolicy.getLegsPerPerson().get(person)!=null)
				numberOfWalkLegsOfHundekopfPolicy += agentWalksEventHandlerPolicy.getLegsPerPerson().get(person);
			if(agentWalksEventHandlerBase.getLegsPerPerson().get(person)!=null)
				numberOfWalkLegsOfHundekopfBase += agentWalksEventHandlerBase.getLegsPerPerson().get(person);
		}
		int numberOfBicycleRidersLivingInHundekopfPolicy = (int) agentUsesBicycleEventHandlerPolicy.getVehicleUsers().stream()
				.filter(person -> personsLivingInHundekopf.contains(person)).count();
		int numberOfBicycleRidersLivingInHundekopfBase = (int) agentUsesBicycleEventHandlerBase.getVehicleUsers().stream()
				.filter(person -> personsLivingInHundekopf.contains(person)).count();
		
		int numberOfCarDriversLivingInBerlinPolicy = (int) agentUsesCarEventHandlerPolicy.getVehicleUsers().stream()
				.filter(person -> personsLivingInBerlin.contains(person)).count();
		int numberOfCarDriversLivingInBerlinBase = (int) agentUsesCarEventHandlerBase.getVehicleUsers().stream()
				.filter(person -> personsLivingInBerlin.contains(person)).count();
		int numberOfCarDriversLivingInHundekopfPolicy = (int) agentUsesCarEventHandlerPolicy.getVehicleUsers().stream()
				.filter(person -> personsLivingInHundekopf.contains(person)).count();
		int numberOfCarDriversLivingInHundekopfBase = (int) agentUsesCarEventHandlerBase.getVehicleUsers().stream()
				.filter(person -> personsLivingInHundekopf.contains(person)).count();
		
		int numberOfPtUsersLivingInBerlinPolicy = (int) agentUsesPtEventHandlerPolicy.getVehicleUsers().stream()
				.filter(person -> personsLivingInBerlin.contains(person)).count();
		int numberOfPtUsersLivingInBerlinBase = (int) agentUsesPtEventHandlerBase.getVehicleUsers().stream()
				.filter(person -> personsLivingInBerlin.contains(person)).count();
		int numberOfPtUsersLivingInHundekopfPolicy = (int) agentUsesPtEventHandlerPolicy.getVehicleUsers().stream()
				.filter(person -> personsLivingInHundekopf.contains(person)).count();
		int numberOfPtUsersLivingInHundekopfBase = (int) agentUsesPtEventHandlerBase.getVehicleUsers().stream()
				.filter(person -> personsLivingInHundekopf.contains(person)).count();
		
		int numberOfWalkersLivingInBerlinPolicy = (int) agentWalksEventHandlerPolicy.getVehicleUsers().stream()
				.filter(person -> personsLivingInBerlin.contains(person)).count();
		int numberOfWalkersLivingInBerlinBase = (int) agentWalksEventHandlerBase.getVehicleUsers().stream()
				.filter(person -> personsLivingInBerlin.contains(person)).count();
		int numberOfWalkersLivingInHundekopfPolicy = (int) agentWalksEventHandlerPolicy.getVehicleUsers().stream()
				.filter(person -> personsLivingInHundekopf.contains(person)).count();
		int numberOfWalkersLivingInHundekopfBase = (int) agentWalksEventHandlerBase.getVehicleUsers().stream()
				.filter(person -> personsLivingInHundekopf.contains(person)).count();

		System.out.println("-------------------------------------------------------------------------------");
		System.out.println("-------------------------------------------------------------------------------");
		System.out.println("Number of agents living in Berlin: "+ personsLivingInBerlin.size());
		System.out.println("Number of agents living in Hundekopf: "+ personsLivingInHundekopf.size());
		System.out.println("-------------------------------------------------------------------------------");
		System.out.println("POLICY CASE:");
		System.out.println("Number of bicycle riders: "+ agentUsesBicycleEventHandlerPolicy.getVehicleUsers().size());
		System.out.println("Number of bicycle riders living in Berlin: "+ numberOfBicycleRidersLivingInBerlinPolicy);
		System.out.println("Number of bicycle riders living in Hundekopf: "+ numberOfBicycleRidersLivingInHundekopfPolicy);
		System.out.println("Number of bicycle rides: "+ agentUsesBicycleEventHandlerPolicy.getNumberOfLegs());
		System.out.println("Number of bicycle rides of Berliners: "+ numberOfBicycleLegsOfBerlinersPolicy);
		System.out.println("Number of bicycle rides of Hundekopfers: "+ numberOfBicycleLegsOfHundekopfPolicy);
		System.out.println("Number of car drivers: "+ agentUsesCarEventHandlerPolicy.getVehicleUsers().size());
		System.out.println("Number of car drivers living in Berlin: "+ numberOfCarDriversLivingInBerlinPolicy);
		System.out.println("Number of car drivers living in Hundekopf: "+ numberOfCarDriversLivingInHundekopfPolicy);
		System.out.println("Number of car legs: "+ agentUsesCarEventHandlerPolicy.getNumberOfLegs());
		System.out.println("Number of car legs of Berliners: "+ numberOfCarLegsOfBerlinersPolicy);
		System.out.println("Number of car legs of Hundekopfers: "+ numberOfCarLegsOfHundekopfPolicy);
		System.out.println("Number of pt users: "+ agentUsesPtEventHandlerPolicy.getVehicleUsers().size());
		System.out.println("Number of pt users living in Berlin: "+ numberOfPtUsersLivingInBerlinPolicy);
		System.out.println("Number of pt users living in Hundekopf: "+ numberOfPtUsersLivingInHundekopfPolicy);
		System.out.println("Number of pt legs: "+ agentUsesPtEventHandlerPolicy.getNumberOfLegs());
		System.out.println("Number of pt legs of Berliners: "+ numberOfPtLegsOfBerlinersPolicy);
		System.out.println("Number of pt legs of Hundekopfers: "+ numberOfPtLegsOfHundekopfPolicy);
		System.out.println("Number of Walkers: "+ agentWalksEventHandlerPolicy.getVehicleUsers().size());
		System.out.println("Number of Walkers living in Berlin: "+ numberOfWalkersLivingInBerlinPolicy);
		System.out.println("Number of Walkers living in Hundekopf: "+ numberOfWalkersLivingInHundekopfPolicy);
		System.out.println("Number of walk legs: "+ agentWalksEventHandlerPolicy.getNumberOfLegs());
		System.out.println("Number of walk legs of Berliners: "+ numberOfWalkLegsOfBerlinersPolicy);
		System.out.println("Number of walk legs of Hundekopfers: "+ numberOfWalkLegsOfHundekopfPolicy);
		System.out.println("-------------------------------------------------------------------------------");
		System.out.println("Total travel time: "+ (travelTimeEventHandlerPolicy.calculateOverallTravelTime()/3600) +" hours");
		System.out.println("Total Travel time of bicycle riders: "+ bicycleRiderTravelTimePolicy/3600 +"hours" );
		System.out.println("Mean of travel times of bicycle riders: "+ bicycleRiderTravelTimePolicy/agentUsesBicycleEventHandlerPolicy.getVehicleUsers().size()/60 +" min");
		System.out.println("Travel times on bicycles: " + bicycleTravelTimeEventHandlerPolicy.getTotalTravelTime()/(3600) + " hours" );

		//System.out.println("Travel time for people using the bike highways: " + ridersOnHighwayTotalTravelTimePolicy / 3600 + " hours");
		System.out.println("-------------------------------------------------------------------------------");
		System.out.println("Total travel distance: "+ (travelDistanceEventHandlerPolicy.getTotalTravelDistance() / 1000) +" km");
		System.out.println("Mean of travel distance of bicycle riders: "+ bicycleRiderTravelDistancePolicy/agentUsesBicycleEventHandlerPolicy.getVehicleUsers().size()/1000 +" km");
		System.out.println("Bicycle distance: "+ bikeDistancePolicy/1000 + " km");
		System.out.println("Bicycle distance of Berliners: "+ bikeDistanceBerlinPolicy/1000 + " km");
		System.out.println("Bicycle distance of Hundekopfers: "+ bikeDistanceHundekopfPolicy/1000 + " km");
	//	System.out.println("Travel distance for people using the bike highways: " + ridersOnHighwayTotalTravelDistancePolicy / 1000 + " km");

		System.out.println("-------------------------------------------------------------------------------");
		System.out.println("short bike trips: " + shortBikeTripsPolicy);
		System.out.println("middle bike trips: " + middleBikeTripsPolicy);
		System.out.println("long bike trips: " + longBikeTripsPolicy);	

		System.out.println("-------------------------------------------------------------------------------");
		System.out.println("-------------------------------------------------------------------------------");
		System.out.println("BASE CASE:");
		System.out.println("Number of bicycle riders: "+ agentUsesBicycleEventHandlerBase.getVehicleUsers().size());
		System.out.println("Number of bicycle riders living in Berlin: "+ numberOfBicycleRidersLivingInBerlinBase);
		System.out.println("Number of bicycle riders living in Hundekopf: "+ numberOfBicycleRidersLivingInHundekopfBase);
		System.out.println("Number of bicycle rides: "+ agentUsesBicycleEventHandlerBase.getNumberOfLegs());
		System.out.println("Number of bicycle rides of Berliners: "+ numberOfBicycleLegsOfBerlinersBase);
		System.out.println("Number of bicycle rides of Hundekopfers: "+ numberOfBicycleLegsOfHundekopfBase);
		System.out.println("Number of car drivers: "+ agentUsesCarEventHandlerBase.getVehicleUsers().size());
		System.out.println("Number of car drivers living in Berlin: "+ numberOfCarDriversLivingInBerlinBase);
		System.out.println("Number of car drivers living in Hundekopf: "+ numberOfCarDriversLivingInHundekopfBase);
		System.out.println("Number of car legs: "+ agentUsesCarEventHandlerBase.getNumberOfLegs());
		System.out.println("Number of car legs of Berliners: "+ numberOfCarLegsOfBerlinersBase);
		System.out.println("Number of car legs of Hundekopfers: "+ numberOfCarLegsOfHundekopfBase);
		System.out.println("Number of pt users: "+ agentUsesPtEventHandlerBase.getVehicleUsers().size());
		System.out.println("Number of pt users living in Berlin: "+ numberOfPtUsersLivingInBerlinBase);
		System.out.println("Number of pt users living in Hundekopf: "+ numberOfPtUsersLivingInHundekopfBase);
		System.out.println("Number of pt legs: "+ agentUsesPtEventHandlerBase.getNumberOfLegs());
		System.out.println("Number of pt legs of Berliners: "+ numberOfPtLegsOfBerlinersBase);
		System.out.println("Number of pt legs of Hundekopfers: "+ numberOfPtLegsOfHundekopfBase);
		System.out.println("Number of Walkers: "+ agentWalksEventHandlerBase.getVehicleUsers().size());
		System.out.println("Number of Walkers living in Berlin: "+ numberOfWalkersLivingInBerlinBase);
		System.out.println("Number of Walkers living in Hundekopf: "+ numberOfWalkersLivingInHundekopfBase);
		System.out.println("Number of walk legs: "+ agentWalksEventHandlerBase.getNumberOfLegs());
		System.out.println("Number of walk legs of Berliners: "+ numberOfWalkLegsOfBerlinersBase);
		System.out.println("Number of walk legs of Hundekopfers: "+ numberOfWalkLegsOfHundekopfBase);
		System.out.println("-------------------------------------------------------------------------------");
		System.out.println("Total travel time: "+ (travelTimeEventHandlerBase.calculateOverallTravelTime()/3600) +" hours");
		System.out.println("Mean of travel times of bicycle riders: "+ bicycleRiderTravelTimeBase/agentUsesBicycleEventHandlerBase.getVehicleUsers().size()/60 +" min");
		System.out.println("Travel times on bicycles: " + bicycleTravelTimeEventHandlerBase.getTotalTravelTime()/(3600) + " hours" );
		
		// System.out.println("Travel time for people using the bike highways: " + ridersOnHighwayTotalTravelTimeBase / 3600 + " hours");
		System.out.println("-------------------------------------------------------------------------------");
		System.out.println("Total travel distance: "+ (travelDistanceEventHandlerBase.getTotalTravelDistance() / 1000) +" km");
		System.out.println("Mean of travel distance of bicycle riders: "+ bicycleRiderTravelDistanceBase/agentUsesBicycleEventHandlerBase.getVehicleUsers().size()/1000 +" km");
		// System.out.println("Travel distance for people using the bike highways: " + ridersOnHighwayTotalTravelDistanceBase / 1000 + " km");
		System.out.println("Bicycle distance: "+ bikeDistanceBase/1000 + " km");
		System.out.println("Bicycle distance of Berliners: "+ bikeDistanceBerlinBase/1000 + " km");
		System.out.println("Bicycle distance of Hundekopfers: "+ bikeDistanceHundekopfBase/1000 + " km");
		System.out.println("-------------------------------------------------------------------------------");
		System.out.println("short bike trips: " + shortBikeTripsBase);
		System.out.println("middle bike trips: " + middleBikeTripsBase);
		System.out.println("long bike trips: " + longBikeTripsBase);		
		System.out.println("-------------------------------------------------------------------------------");
		System.out.println("-------------------------------------------------------------------------------");
		System.out.println("End of Analysis.");
	}
	
	private static double calculateTravelTimeOfSetOfAgents(Set<Id<Person>> persons,TravelTimeEventHandler eventHandler) {
		Map<Id<Person>, Double> personTimeMapping = eventHandler.getTravelTimesByPerson();
		Double timeSum = 0.;
		for (Id<Person> person: persons) {
			timeSum += personTimeMapping.get(person);
		}
		return timeSum;
		
	}
	
	private static double calculateTravelDistanceOfSetOfAgents(Set<Id<Person>> persons,TravelDistanceEventHandler eventHandler) {
		Map<Id<Person>, Double> personDistanceMapping = eventHandler.getTravelDistancesByPerson();
		Double distanceSum = 0.;
		for (Id<Person> person: persons) {
			distanceSum += personDistanceMapping.get(person);
		}
		return distanceSum;
		
	}
	private static Map<String, Geometry> readShapeFile(String shapeFile){
        Collection<SimpleFeature> features = ShapeFileReader.getAllFeatures(shapeFile);
        Map<String, Geometry> districts = new HashMap<>();

        for (SimpleFeature feature : features) {
            String id = feature.getID();
            Geometry geometry = (Geometry) feature.getDefaultGeometry();
            districts.put(id, geometry);
        }

        return districts;
	}
	/*
	private static String inDistrict( Map<String, Geometry> allZones, Coord coord) {
        Point point = MGC.coord2Point(coord);
        for (String nameZone : allZones.keySet()) {
            Geometry geo = allZones.get(nameZone);
            if (geo.contains(point)) {
                return nameZone;
            }
        }
        return "noZone";
	}*/
	
	 private static boolean inDistrict( Map<String, Geometry> allZones, Coord coord) {
	        Point point = MGC.coord2Point(coord);
	        for (String nameZone : allZones.keySet()) {
	            Geometry geo = allZones.get(nameZone);
	            if (geo.contains(point)) {
	                return true;
	            }
	        }
	        return false;
	    }
	
	private static void createNewPopulation(String outputPlans, Map<String, Geometry> allZones,
			Population population) {
		
		 Population outPopulation = ScenarioUtils.createScenario(ConfigUtils.createConfig()).getPopulation();
		 for (Person person : population.getPersons().values()) {
			boolean isInDistrict = false;
            for (Plan plan : person.getPlans()) {
                for (PlanElement planElement : plan.getPlanElements()) {
                    if (planElement instanceof Activity) {
                        Activity activity = (Activity) planElement;
                        if(activity.getType().contains("home")) {
	          
	                        if(inDistrict(allZones, activity.getCoord())){
	                        	isInDistrict = true;
	                        }
                        }
                    }
                }
            }
            if(isInDistrict) {
                outPopulation.addPerson(person);
            }
        }

        new PopulationWriter(outPopulation).write(outputPlans);
	}
}
