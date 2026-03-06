package tools.vitruv.methodologisttemplate.vsum;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.io.TempDir;

import UVLPackage.FeatureTree;
import UVLPackage.Group;
import UVLPackage.Mandatory;
import UVLPackage.UVLModel;
import UVLPackage.uvlFactory;
import configurationPackage.ConfigurationModel;
import configurationPackage.Feature;
import configurationPackage.Configuration;
import configurationPackage.ConfigurationPackageFactory;
import configurationPackage.ConfigurationPackagePackage;
import mir.reactions.feature2config.Feature2configChangePropagationSpecification;
import tools.vitruv.change.propagation.ChangePropagationSpecification;
import tools.vitruv.framework.views.CommittableView;
import tools.vitruv.framework.views.View;
import tools.vitruv.framework.vsum.VirtualModel;

public class FeatureModelToConfigTest {

    TestUtil util = new TestUtil();
    Iterable<ChangePropagationSpecification> additionalCPS = List.of(new Feature2configChangePropagationSpecification());


    	@BeforeAll
	static void setup() {
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("*",
				new XMIResourceFactoryImpl());

	}


    @Test
    public void testFMToConfig(@TempDir Path tempDir) {
       VirtualModel vsum = util.createDefaultVirtualModel(tempDir,additionalCPS);
		util.registerRootFMObjects(vsum, tempDir);

        CommittableView configview = util.getDefaultView(vsum, List.of(ConfigurationModel.class)).withChangeDerivingTrait();
        CommittableView view = util.getDefaultView(vsum, List.of(UVLModel.class)).withChangeDerivingTrait();

        util.modifyView(configview, (CommittableView v) -> {
            ConfigurationModel configModel = (ConfigurationModel) v.getRootObjects(ConfigurationModel.class).iterator().next();
            configModel.getConfiguration().add(createDefaultConfig());
        });

        util.modifyView(view, (CommittableView v) -> {
            UVLModel uvlModel = (UVLModel) v.getRootObjects(UVLModel.class).iterator().next();
            FeatureTree featureTree = createDefaultFMTree();
            uvlModel.setTree(featureTree);
        });

        Assertions.assertTrue(assertView(util.getDefaultView(vsum, List.of(ConfigurationModel.class)), (View v) -> {
            ConfigurationModel configModel = (ConfigurationModel) v.getRootObjects(ConfigurationModel.class).iterator().next();
            return configModel.getConfiguration().size() == 1 && configModel.getConfiguration().get(0).getFeature().size() == 1 && configModel.getConfiguration().get(0).getFeature().get(0).getName().equals("Root");
        }));


    }

    private boolean assertView(View view, Function<View, Boolean> viewAssertionFunction) {
		return viewAssertionFunction.apply(view);
	}

    private FeatureTree createDefaultFMTree(){
        FeatureTree featureTree = uvlFactory.eINSTANCE.createFeatureTree();
        UVLPackage.Feature root = uvlFactory.eINSTANCE.createFeature();
        root.setName("Root");
        
        Mandatory Rootgroup = uvlFactory.eINSTANCE.createMandatory();
        Rootgroup.getFeature().add(root);
        root.setGroup(Rootgroup);

        featureTree.setRoot(Rootgroup);

        return featureTree;

    }


    private Configuration createDefaultConfig(){
        Configuration config = ConfigurationPackageFactory.eINSTANCE.createConfiguration();
        config.setPLVersion(0);
        return config;
    }



    
}
