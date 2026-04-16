package tools.vitruv.methodologisttemplate.vsum.PropagationTestProblemSpaceDomainOneChanged;

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
import brakesystem.Brakesystem;
import edu.kit.ipd.sdq.metamodels.cad.CAD_Model;
import mir.reactions.brakesystem2cad.Brakesystem2cadChangePropagationSpecification;
import mir.reactions.cad2brakesystem.Cad2brakesystemChangePropagationSpecification;
import mir.reactions.combinedUVLToBS.CombinedUVLToBSChangePropagationSpecification;
import mir.reactions.combinedUVLToCAD.CombinedUVLToCADChangePropagationSpecification;
import mir.reactions.feature2brakesystem.Feature2brakesystemChangePropagationSpecification;
import mir.reactions.feature2cad.Feature2cadChangePropagationSpecification;
import mir.reactions.feature2config.Feature2configChangePropagationSpecification;
import tools.vitruv.change.propagation.ChangePropagationSpecification;
import tools.vitruv.framework.views.CommittableView;
import tools.vitruv.framework.views.View;
import tools.vitruv.framework.vsum.VirtualModel;
import tools.vitruv.methodologisttemplate.vsum.TestUtil;


public class FeatureToDomainToDomainTest {
    private static final int SELECT_NEW = 1;

    TestUtil util = new TestUtil();
    Iterable<ChangePropagationSpecification> additionalCPS = List.of(new CombinedUVLToCADChangePropagationSpecification(),new CombinedUVLToBSChangePropagationSpecification(),new Brakesystem2cadChangePropagationSpecification(), new Cad2brakesystemChangePropagationSpecification());


    @BeforeAll
	static void setup() {
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("*",
				new XMIResourceFactoryImpl());

	}
    

    //This test checks that if a feature is added to the problem space and then assigned to a domain, the change is propagated to the other domain as well.
    @Test
    public void testAddedFeatureToCADDomainFM(@TempDir Path tempDir) {
        util.userInteraction.addNextSingleSelection(0);
        util.userInteraction.addNextSingleSelection(0);
        VirtualModel vsum = util.createDefaultVirtualModel(tempDir,additionalCPS);
        util.registerRootFMObjects(vsum, tempDir);
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
        

        Assertions.assertTrue(TestUtil.assertView(util.getDefaultView(vsum, List.of(Brakesystem.class)), (View v) -> {
            Brakesystem brakesystem = v.getRootObjects(Brakesystem.class).iterator().next();
            return !brakesystem.getBrakeComponents().isEmpty();
        }));


    }



    //This test checks that if a feature is added to the problem space and then assigned to a domain, the change is propagated to the other domain as well.
    @Test
    public void testAddedFeatureToBSDomainFM(@TempDir Path tempDir) {
        util.userInteraction.addNextSingleSelection(0);
        util.userInteraction.addNextSingleSelection(0);
         util.userInteraction.addNextSingleSelection(SELECT_NEW);
        VirtualModel vsum = util.createDefaultVirtualModel(tempDir,additionalCPS);
        util.registerRootFMObjects(vsum, tempDir);
        CommittableView view = util.getDefaultView(vsum, List.of(UVLModel.class)).withChangeDerivingTrait();


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
            Brakesystem brakesystem = v.getRootObjects(Brakesystem.class).iterator().next();
            return !brakesystem.getBrakeComponents().isEmpty();
        }));

        Assertions.assertTrue(TestUtil.assertView(util.getDefaultView(vsum, List.of(CAD_Model.class)), (View v) -> {
            CAD_Model cadModel = v.getRootObjects(CAD_Model.class).iterator().next();
            return !cadModel.getNamespaces().isEmpty();
        }));
        

        


    }
    

}
