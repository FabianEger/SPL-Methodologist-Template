package tools.vitruv.methodologisttemplate.vsum.AdditionScenarioTests;

import java.nio.file.Path;
import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import UVLPackage.Feature;
import UVLPackage.FeatureTree;
import UVLPackage.Mandatory;
import UVLPackage.UVLModel;
import UVLPackage.uvlFactory;
import brakesystem.ABSSensor;
import brakesystem.Brakesystem;
import configurationPackage.ConfigurationModel;
import edu.kit.ipd.sdq.metamodels.cad.CAD_Model;
import mir.reactions.cad2brakesystem.Cad2brakesystemChangePropagationSpecification;
import mir.reactions.combinedUVLToCAD.CombinedUVLToCADChangePropagationSpecification;
import mir.reactions.feature2config.Feature2configChangePropagationSpecification;
import mir.reactions.uvl2uvl.Uvl2uvlChangePropagationSpecification;
import tools.vitruv.change.propagation.ChangePropagationSpecification;
import tools.vitruv.framework.views.CommittableView;
import tools.vitruv.framework.views.View;
import tools.vitruv.framework.vsum.VirtualModel;
import tools.vitruv.methodologisttemplate.vsum.TestUtil;

public class AdditionScenarioOneTest {
    
    TestUtil util = new TestUtil();
    Iterable<ChangePropagationSpecification> scenarioOneDotOneCPS = List.of(new CombinedUVLToCADChangePropagationSpecification(),
                                                                        new Uvl2uvlChangePropagationSpecification(),
                                                                        new Feature2configChangePropagationSpecification() 
                                                                    );
    
    Iterable<ChangePropagationSpecification> scenarioOneDotTwoCPS = List.of(new CombinedUVLToCADChangePropagationSpecification(),
                                                                        new Uvl2uvlChangePropagationSpecification(),
                                                                        new Feature2configChangePropagationSpecification(),
                                                                        new Cad2brakesystemChangePropagationSpecification()         
                                                                    );
    
     Iterable<ChangePropagationSpecification> scenarioOneDotThreeCPS = List.of(new CombinedUVLToCADChangePropagationSpecification(),
                                                                        new Uvl2uvlChangePropagationSpecification(),
                                                                        new Feature2configChangePropagationSpecification(),
                                                                        new Cad2brakesystemChangePropagationSpecification()     
                                                                    );

    private static final int SELECT_EXISTING = 0;
    private static final int SELECT_NEW = 1;
    private static final int ABS_SENSOR = 0;

    @BeforeAll
	static void setup() {
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("*",
				new XMIResourceFactoryImpl());

	}

    /*
        This test checks the scenarion 1.1 of the variability evolution scenarios. 
        It checks that if a new feature is added to the problem space, it is propagated to the other solution space and the configuration model.
    */
    @Test
    public void testScenarioOnePointOne(@TempDir Path tempDir) {
        
        util.userInteraction.addNextSingleSelection(SELECT_NEW);

        VirtualModel vsum = util.createDefaultVirtualModel(tempDir,scenarioOneDotOneCPS);
        util.registerRootFMObjects(vsum, tempDir);
        CommittableView configview = util.getDefaultView(vsum, List.of(ConfigurationModel.class)).withChangeDerivingTrait();

        util.modifyView(configview, (CommittableView v) -> {
            ConfigurationModel configModel = (ConfigurationModel) v.getRootObjects(ConfigurationModel.class).iterator().next();
            configModel.getConfiguration().add(util.createDefaultConfig());
        });
        

        CommittableView view = util.getDefaultView(vsum, List.of(UVLModel.class)).withChangeDerivingTrait();
        util.modifyView(view, (CommittableView v) -> {
            UVLModel uvlModel = (UVLModel) v.getRootObjects(UVLModel.class).iterator().next();
            FeatureTree featureTree = TestUtil.createDefaultFMTree();
            uvlModel.setTree(featureTree);

            Mandatory mandatoryFeature = uvlFactory.eINSTANCE.createMandatory();
            Feature newDomainFeature = uvlFactory.eINSTANCE.createFeature();
            newDomainFeature.setName("DomainOneRoot");

            mandatoryFeature.getFeature().add(newDomainFeature);
            newDomainFeature.setGroup(mandatoryFeature);

            featureTree.getRoot().getFeature().get(0).getChildren().add(mandatoryFeature);

            Mandatory mandatorySubFeature = uvlFactory.eINSTANCE.createMandatory();
            Feature subFeature = uvlFactory.eINSTANCE.createFeature();
            subFeature.setName("SubFeature");
            mandatorySubFeature.getFeature().add(subFeature);
            subFeature.setGroup(mandatorySubFeature);   

            newDomainFeature.getChildren().add(mandatorySubFeature);


        
        });

       

        Assertions.assertTrue(TestUtil.assertView(util.getDefaultView(vsum, List.of(CAD_Model.class)), (View v) -> {
            CAD_Model cadModel = v.getRootObjects(CAD_Model.class).iterator().next();
            return !cadModel.getNamespaces().isEmpty();
        }));

        Assertions.assertTrue(TestUtil.assertView(util.getDefaultView(vsum, List.of(ConfigurationModel.class)), (View v) -> {
            ConfigurationModel configModel = (ConfigurationModel) v.getRootObjects(ConfigurationModel.class).iterator().next();
            return configModel.getConfiguration().size() == 1 && configModel.getConfiguration().get(0).getFeature().size() == 3 && configModel.getConfiguration().get(0).getFeature().get(2).getName().equals("SubFeature");
        }));
        

       
    }


    @Test
    public void testScenarioOnePointTwo(@TempDir Path tempDir) {
        
        util.userInteraction.addNextSingleSelection(SELECT_NEW);
        util.userInteraction.addNextSingleSelection(ABS_SENSOR);

        VirtualModel vsum = util.createDefaultVirtualModel(tempDir,scenarioOneDotTwoCPS);
        util.registerRootFMObjects(vsum, tempDir);
        CommittableView configview = util.getDefaultView(vsum, List.of(ConfigurationModel.class)).withChangeDerivingTrait();

        util.modifyView(configview, (CommittableView v) -> {
            ConfigurationModel configModel = (ConfigurationModel) v.getRootObjects(ConfigurationModel.class).iterator().next();
            configModel.getConfiguration().add(util.createDefaultConfig());
        });
        

        CommittableView view = util.getDefaultView(vsum, List.of(UVLModel.class)).withChangeDerivingTrait();
        util.modifyView(view, (CommittableView v) -> {
            UVLModel uvlModel = (UVLModel) v.getRootObjects(UVLModel.class).iterator().next();
            FeatureTree featureTree = TestUtil.createDefaultFMTree();
            uvlModel.setTree(featureTree);

            Mandatory mandatoryFeature = uvlFactory.eINSTANCE.createMandatory();
            Feature newDomainFeature = uvlFactory.eINSTANCE.createFeature();
            newDomainFeature.setName("DomainOneRoot");

            mandatoryFeature.getFeature().add(newDomainFeature);
            newDomainFeature.setGroup(mandatoryFeature);

            featureTree.getRoot().getFeature().get(0).getChildren().add(mandatoryFeature);

            Mandatory mandatorySubFeature = uvlFactory.eINSTANCE.createMandatory();
            Feature subFeature = uvlFactory.eINSTANCE.createFeature();
            subFeature.setName("SubFeature");
            mandatorySubFeature.getFeature().add(subFeature);
            subFeature.setGroup(mandatorySubFeature);   

            newDomainFeature.getChildren().add(mandatorySubFeature);


        
        });

       

        Assertions.assertTrue(TestUtil.assertView(util.getDefaultView(vsum, List.of(CAD_Model.class)), (View v) -> {
            CAD_Model cadModel = v.getRootObjects(CAD_Model.class).iterator().next();
            return !cadModel.getNamespaces().isEmpty();
        }));

        Assertions.assertTrue(TestUtil.assertView(util.getDefaultView(vsum, List.of(ConfigurationModel.class)), (View v) -> {
            ConfigurationModel configModel = (ConfigurationModel) v.getRootObjects(ConfigurationModel.class).iterator().next();
            return configModel.getConfiguration().size() == 1 && configModel.getConfiguration().get(0).getFeature().size() == 3 && configModel.getConfiguration().get(0).getFeature().get(2).getName().equals("SubFeature");
        }));

         Assertions.assertTrue(
                TestUtil.assertView(util.getDefaultView(vsum, List.of(Brakesystem.class)),
                        (View v) -> !v.getRootObjects(Brakesystem.class).iterator().next()
                                .getBrakeComponents().isEmpty() && v.getRootObjects(Brakesystem.class).iterator().next()
                                        .getBrakeComponents().get(0) instanceof ABSSensor
                ));
       
    }

     @Test
    public void testScenarioOnePointThree(@TempDir Path tempDir) {
        
        util.userInteraction.addNextSingleSelection(SELECT_NEW);
        util.userInteraction.addNextSingleSelection(ABS_SENSOR);

        VirtualModel vsum = util.createDefaultVirtualModel(tempDir,scenarioOneDotTwoCPS);
        util.registerRootFMObjects(vsum, tempDir);
        CommittableView configview = util.getDefaultView(vsum, List.of(ConfigurationModel.class)).withChangeDerivingTrait();

        util.modifyView(configview, (CommittableView v) -> {
            ConfigurationModel configModel = (ConfigurationModel) v.getRootObjects(ConfigurationModel.class).iterator().next();
            configModel.getConfiguration().add(util.createDefaultConfig());
        });
        

        CommittableView view = util.getDefaultView(vsum, List.of(UVLModel.class)).withChangeDerivingTrait();
        util.modifyView(view, (CommittableView v) -> {
            UVLModel uvlModel = (UVLModel) v.getRootObjects(UVLModel.class).iterator().next();
            FeatureTree featureTree = TestUtil.createDefaultFMTree();
            uvlModel.setTree(featureTree);

            Mandatory mandatoryFeature = uvlFactory.eINSTANCE.createMandatory();
            Feature newDomainFeature = uvlFactory.eINSTANCE.createFeature();
            newDomainFeature.setName("DomainOneRoot");

            mandatoryFeature.getFeature().add(newDomainFeature);
            newDomainFeature.setGroup(mandatoryFeature);

            featureTree.getRoot().getFeature().get(0).getChildren().add(mandatoryFeature);

            Mandatory mandatorySubFeature = uvlFactory.eINSTANCE.createMandatory();
            Feature subFeature = uvlFactory.eINSTANCE.createFeature();
            subFeature.setName("SubFeature");
            mandatorySubFeature.getFeature().add(subFeature);
            subFeature.setGroup(mandatorySubFeature);   

            newDomainFeature.getChildren().add(mandatorySubFeature);


        
        });

       

        Assertions.assertTrue(TestUtil.assertView(util.getDefaultView(vsum, List.of(CAD_Model.class)), (View v) -> {
            CAD_Model cadModel = v.getRootObjects(CAD_Model.class).iterator().next();
            return !cadModel.getNamespaces().isEmpty();
        }));

        Assertions.assertTrue(TestUtil.assertView(util.getDefaultView(vsum, List.of(ConfigurationModel.class)), (View v) -> {
            ConfigurationModel configModel = (ConfigurationModel) v.getRootObjects(ConfigurationModel.class).iterator().next();
            return configModel.getConfiguration().size() == 1 && configModel.getConfiguration().get(0).getFeature().size() == 3 && configModel.getConfiguration().get(0).getFeature().get(2).getName().equals("SubFeature");
        }));

         Assertions.assertTrue(
                TestUtil.assertView(util.getDefaultView(vsum, List.of(Brakesystem.class)),
                        (View v) -> !v.getRootObjects(Brakesystem.class).iterator().next()
                                .getBrakeComponents().isEmpty() && v.getRootObjects(Brakesystem.class).iterator().next()
                                        .getBrakeComponents().get(0) instanceof ABSSensor
                ));
       
    }

}
