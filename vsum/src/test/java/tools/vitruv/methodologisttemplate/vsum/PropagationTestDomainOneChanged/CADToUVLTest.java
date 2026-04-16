package tools.vitruv.methodologisttemplate.vsum.PropagationTestDomainOneChanged;

import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
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
import mir.reactions.cad2feature.Cad2featureChangePropagationSpecification;
import mir.reactions.feature2brakesystem.Feature2brakesystemChangePropagationSpecification;
import mir.reactions.feature2cad.Feature2cadChangePropagationSpecification;
import mir.reactions.feature2config.Feature2configChangePropagationSpecification;
import mir.reactions.uvl2uvl.Uvl2uvlChangePropagationSpecification;
import tools.vitruv.change.propagation.ChangePropagationSpecification;
import tools.vitruv.framework.views.CommittableView;
import tools.vitruv.framework.views.View;
import tools.vitruv.framework.vsum.VirtualModel;
import tools.vitruv.methodologisttemplate.vsum.TestUtil;


public class CADToUVLTest {


    TestUtil util = new TestUtil();
    Iterable<ChangePropagationSpecification> additionalCPS = List.of(new Cad2featureChangePropagationSpecification(),new Uvl2uvlChangePropagationSpecification());


    @BeforeAll
	static void setup() {
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("*",
				new XMIResourceFactoryImpl());

	}


    @Test
    public void testAddedCADModelwithExistingDomainRootFeature(@TempDir Path tempDir) {
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
    
        });

        util.registerCADRootObjects(vsum, tempDir);

        
        Assertions.assertTrue(TestUtil.assertView(util.getDefaultView(vsum, List.of(UVLModel.class)), (View v) -> {
           UVLModel uvlModel = v.getRootObjects(UVLModel.class).iterator().next();
           return !uvlModel.getTree().getRoot().getFeature().get(0).getChildren().isEmpty() && uvlModel.getTree().getRoot().getFeature().get(0).getChildren().get(0).getFeature().get(0).getName().equals("DomainOneRoot");
        }));
    }


    @Test
    public void testAddedCADModelwithExistingDomainRootButNoDomainRootFeature(@TempDir Path tempDir) {
        VirtualModel vsum = util.createDefaultVirtualModel(tempDir,additionalCPS);
        

        util.registerRootFMObjects(vsum, tempDir);

        CommittableView view = util.getDefaultView(vsum, List.of(UVLModel.class)).withChangeDerivingTrait();
        util.modifyView(view, (CommittableView v) -> {
        
        UVLModel uvlModel = (UVLModel) v.getRootObjects(UVLModel.class).iterator().next();
        FeatureTree featureTree = TestUtil.createDefaultFMTree();
        uvlModel.setTree(featureTree);
        });

        util.registerCADRootObjects(vsum, tempDir);

        
        Assertions.assertTrue(TestUtil.assertView(util.getDefaultView(vsum, List.of(UVLModel.class)), (View v) -> {
           UVLModel uvlModel = v.getRootObjects(UVLModel.class).iterator().next();
           return !uvlModel.getTree().getRoot().getFeature().get(0).getChildren().isEmpty() && uvlModel.getTree().getRoot().getFeature().get(0).getChildren().get(0).getFeature().get(0).getName().equals("DomainOneRoot");
        }));
    }


     @Test
    public void testAddedCADModelwithNoUVLModel(@TempDir Path tempDir) {
        VirtualModel vsum = util.createDefaultVirtualModel(tempDir,additionalCPS);
        
        util.registerCADRootObjects(vsum, tempDir);

        Assertions.assertTrue(TestUtil.assertView(util.getDefaultView(vsum, List.of(UVLModel.class)), (View v) -> {
           UVLModel uvlModel = v.getRootObjects(UVLModel.class).iterator().next();
           return !uvlModel.getTree().getRoot().getFeature().get(0).getChildren().isEmpty() && uvlModel.getTree().getRoot().getFeature().get(0).getChildren().get(0).getFeature().get(0).getName().equals("DomainOneRoot");
        }));
    }


    @Test
    public void testRemoveCADModelandFeatureHasNoCorrespondence(@TempDir Path tempDir) {
    VirtualModel vsum = util.createDefaultVirtualModel(tempDir,additionalCPS);
    
    util.registerCADRootObjects(vsum, tempDir);

    Assertions.assertTrue(TestUtil.assertView(util.getDefaultView(vsum, List.of(UVLModel.class)), (View v) -> {
        UVLModel uvlModel = v.getRootObjects(UVLModel.class).iterator().next();
        return !uvlModel.getTree().getRoot().getFeature().get(0).getChildren().isEmpty() && uvlModel.getTree().getRoot().getFeature().get(0).getChildren().get(0).getFeature().get(0).getName().equals("DomainOneRoot");
    }));

    CommittableView view = util.getDefaultView(vsum, List.of(CAD_Model.class)).withChangeDerivingTrait();

    util.modifyView(view, (CommittableView v) -> {
        CAD_Model cadModel = v.getRootObjects(CAD_Model.class).iterator().next();
        EcoreUtil.delete(cadModel);
    });
    
    Assertions.assertThrows(NoSuchElementException.class, () -> {
        TestUtil.assertView(util.getDefaultView(vsum, List.of(CAD_Model.class)), (View v) -> {
            CAD_Model cad_Model = v.getRootObjects(CAD_Model.class).iterator().next();
            return cad_Model == null;
        });
    });


    Assertions.assertTrue(TestUtil.assertView(util.getDefaultView(vsum, List.of(UVLModel.class)), (View v) -> {
        UVLModel uvlModel = v.getRootObjects(UVLModel.class).iterator().next();
        return uvlModel.getTree().getRoot().getFeature().get(0).getChildren().isEmpty();
    }));
    
    }
    
}
