package tools.vitruv.methodologisttemplate.vsum.PropagationTestProblemSpaceDomainOneChanged;

import java.nio.file.Path;
import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import UVLPackage.Feature;
import UVLPackage.FeatureTree;
import UVLPackage.Mandatory;
import UVLPackage.UVLModel;
import UVLPackage.uvlFactory;
import brakesystem.ABSSensor;
import brakesystem.Brakesystem;
import edu.kit.ipd.sdq.metamodels.cad.CAD_Model;
import mir.reactions.combinedUVLToBS.CombinedUVLToBSChangePropagationSpecification;
import mir.reactions.feature2brakesystem.Feature2brakesystemChangePropagationSpecification;
import mir.reactions.feature2cad.Feature2cadChangePropagationSpecification;
import mir.reactions.feature2config.Feature2configChangePropagationSpecification;
import tools.vitruv.change.propagation.ChangePropagationSpecification;
import tools.vitruv.framework.views.CommittableView;
import tools.vitruv.framework.views.View;
import tools.vitruv.framework.vsum.VirtualModel;
import tools.vitruv.methodologisttemplate.vsum.TestUtil;

public class FeatureToDomainTest {
    
    
    private static final int SELECT_EXISTING = 0;
    private static final int SELECT_NEW = 1;

    TestUtil util = new TestUtil();
    Iterable<ChangePropagationSpecification> additionalCPS = List.of(new Feature2cadChangePropagationSpecification(),new CombinedUVLToBSChangePropagationSpecification());


    @BeforeAll
	static void setup() {
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("*",
				new XMIResourceFactoryImpl());

	}

    
    @Test
    public void testAddedFeatureToCADDomainFM(@TempDir Path tempDir) {

        //util.userInteraction.addNextSingleSelection(0);
        VirtualModel vsum = util.createDefaultVirtualModel(tempDir,additionalCPS);
        util.registerRootFMObjects(vsum, tempDir);
        CommittableView view = util.getDefaultView(vsum, List.of(UVLModel.class)).withChangeDerivingTrait();


        util.modifyView(view, (CommittableView v) -> {
           
            UVLModel uvlModel = (UVLModel) v.getRootObjects(UVLModel.class).iterator().next();
            FeatureTree featureTree = TestUtil.createDefaultFMTree();
            uvlModel.setTree(featureTree);



            Mandatory mandatoryFeature = uvlFactory.eINSTANCE.createMandatory();

            featureTree.getRoot().getFeature().get(0).getChildren().add(mandatoryFeature);

            Feature newDomainFeature = uvlFactory.eINSTANCE.createFeature();
            newDomainFeature.setName("DomainOneRoot");

            mandatoryFeature.getFeature().add(newDomainFeature);
            newDomainFeature.setGroup(mandatoryFeature);

            


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


    }



    @Test
    public void testAddedFeatureToBSDomainFM(@TempDir Path tempDir) {
        //Userinteraction: First selects that the new feature should add a new component to the brake system 
        util.userInteraction.addNextSingleSelection(SELECT_NEW);
        VirtualModel vsum = util.createDefaultVirtualModel(tempDir,additionalCPS);
        util.registerRootFMObjects(vsum, tempDir);
        CommittableView view = util.getDefaultView(vsum, List.of(UVLModel.class)).withChangeDerivingTrait();
        util.userInteraction.addNextSingleSelection(0);

        util.modifyView(view, (CommittableView v) -> {
           
            UVLModel uvlModel = (UVLModel) v.getRootObjects(UVLModel.class).iterator().next();
            FeatureTree featureTree = TestUtil.createDefaultFMTree();
            uvlModel.setTree(featureTree);

            Mandatory mandatoryFeature = uvlFactory.eINSTANCE.createMandatory();
            Feature newDomainFeature = uvlFactory.eINSTANCE.createFeature();
            newDomainFeature.setName("DomainTwoRoot");

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



        Assertions.assertTrue(TestUtil.assertView(util.getDefaultView(vsum, List.of(Brakesystem.class)), (View v) -> {
            Brakesystem brModel = v.getRootObjects(Brakesystem.class).iterator().next();
            System.out.println(brModel.getBrakeComponents());
            return !brModel.getBrakeComponents().isEmpty() && brModel.getBrakeComponents().get(0) instanceof ABSSensor;
        }));


    }

}
