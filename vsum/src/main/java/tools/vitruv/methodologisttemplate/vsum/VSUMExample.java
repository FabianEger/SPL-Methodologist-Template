package tools.vitruv.methodologisttemplate.vsum;

import tools.vitruv.framework.vsum.VirtualModelBuilder;
import UVLPackage.Feature;
import UVLPackage.FeatureTree;
import UVLPackage.Mandatory;
import UVLPackage.UVLModel;
import UVLPackage.uvlFactory;
import configurationPackage.ConfigurationModel;
import configurationPackage.ConfigurationPackageFactory;
import configurationPackage.Configuration;

import java.io.ObjectInputFilter.Config;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;


import org.eclipse.emf.common.util.URI;

import mir.reactions.feature2config.Feature2configChangePropagationSpecification;
import tools.vitruv.change.propagation.ChangePropagationMode;
import tools.vitruv.change.testutils.TestUserInteraction;
import tools.vitruv.framework.views.CommittableView;
import tools.vitruv.framework.views.View;
import tools.vitruv.framework.views.ViewTypeFactory;
import tools.vitruv.framework.vsum.VirtualModel;

/**
 * This class provides an example how to define and use a VSUM.
 */
public class VSUMExample {


 
  public static void main(String[] args) {
    Path storagePath = Path.of("vsumexample");
    VirtualModel vsum = createDefaultVirtualModel(storagePath);
    vsum.setChangePropagationMode(ChangePropagationMode.TRANSITIVE_CYCLIC); 
    registerRootObjects(vsum, storagePath);
    CommittableView view1 = getDefaultView(vsum).withChangeRecordingTrait();
    CommittableView view = getDefaultView(vsum).withChangeRecordingTrait();

    modifyView(view1, (CommittableView v) -> {

      ConfigurationModel configModel = (ConfigurationModel) v.getRootObjects(ConfigurationModel.class).iterator().next();
      Configuration config = ConfigurationPackageFactory.eINSTANCE.createConfiguration();
      config.setPLVersion(1);
      configModel.getConfiguration().add(config);
    });


    modifyView(view, (CommittableView v) -> {
      
     
      UVLModel uvlModel = (UVLModel) v.getRootObjects().iterator().next();
      FeatureTree featureTree = uvlFactory.eINSTANCE.createFeatureTree();
      uvlModel.setTree(featureTree);

      Feature root = uvlFactory.eINSTANCE.createFeature();
      Mandatory Rootgroup = uvlFactory.eINSTANCE.createMandatory();
      root.setGroup(Rootgroup);
      root.setName("RootFeature");
      Rootgroup.getFeature().add(root);
      featureTree.setRoot(Rootgroup);

    });

    


  }

  public static void registerRootObjects(VirtualModel virtualModel, Path filePath) {
          CommittableView view = getDefaultView(virtualModel,
                  List.of(UVLModel.class))
                  .withChangeRecordingTrait();
          modifyView(view, (CommittableView v) -> {
              v.registerRoot(
                      uvlFactory.eINSTANCE.createUVLModel(),
                      URI.createFileURI(filePath.toString() + "/brake.uvl"));
          });

  }

  private static VirtualModel createDefaultVirtualModel(Path storagePath) {
    return new VirtualModelBuilder()
        .withStorageFolder(storagePath)
        .withUserInteractorForResultProvider(new TestUserInteraction.ResultProvider(new TestUserInteraction()))
        .withChangePropagationSpecifications(new Feature2configChangePropagationSpecification())
        .buildAndInitialize();
  }

  private static View getDefaultView(VirtualModel vsum) {
    var selector = vsum.createSelector(ViewTypeFactory.createIdentityMappingViewType("default"));
    selector.getSelectableElements().forEach(it -> selector.setSelected(it, true));
    return selector.createView();
  }

  public static View getDefaultView(VirtualModel vsum, Collection<Class<?>> rootTypes) {
        var selector = vsum.createSelector(ViewTypeFactory.createIdentityMappingViewType("default"));
        selector.getSelectableElements().stream()
                .filter(element -> rootTypes.stream().anyMatch(it -> it.isInstance(element)))
                .forEach(it -> selector.setSelected(it, true));
        return selector.createView();
    }

  private static void modifyView(CommittableView view, Consumer<CommittableView> modificationFunction) {
    modificationFunction.accept(view);
    view.commitChanges();
  }

}
