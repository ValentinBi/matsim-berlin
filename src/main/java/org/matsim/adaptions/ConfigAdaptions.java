package org.matsim.adaptions;

import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.OutputDirectoryHierarchy;

public class ConfigAdaptions {
	public static void main(String[] args) {
		String configfile = "./scenarios/berlin-v5.3-1pct/input/berlin-v5.3-1pct.config.xml";
		
		Config config = ConfigUtils.loadConfig(configfile);
		
		config.controler().setOutputDirectory("/home/valentin/MATSim/Berlin/output_adapted");
		config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);
		config.controler().setFirstIteration(0);
		config.controler().setLastIteration(500);
		config.controler().setRunId("berlin-v5.3-1pct-A100");
		config.network().setInputFile("/home/valentin/MATSim/Berlin/berlin-v5-A100-network.xml.gz");
		
		String newconfigfilename = "/home/valentin/MATSim/Berlin/berlin-v5.3-1pct-adapted.config.xml";
		ConfigUtils.writeConfig(config, newconfigfilename);
	}
	
}
