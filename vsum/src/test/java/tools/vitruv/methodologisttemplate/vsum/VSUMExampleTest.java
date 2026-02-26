package tools.vitruv.methodologisttemplate.vsum;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import UVLPackage.UVLModel;
import UVLPackage.uvlFactory;
import configurationPackage.ConfigurationModel;
import mir.reactions.feature2config.Feature2configChangePropagationSpecification;
import tools.vitruv.change.propagation.ChangePropagationMode;
import tools.vitruv.change.testutils.TestUserInteraction;
import tools.vitruv.framework.views.CommittableView;
import tools.vitruv.framework.views.View;
import tools.vitruv.framework.views.ViewTypeFactory;
import tools.vitruv.framework.vsum.VirtualModel;
import tools.vitruv.framework.vsum.VirtualModelBuilder;
import tools.vitruv.framework.vsum.internal.InternalVirtualModel;

/**
 * This class provides an example how to define and use a VSUM.
 */
public class VSUMExampleTest {

  @BeforeAll
  static void setup() {
    Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
  }

  @Test
  void reloadEmptyVirtualModel(@TempDir Path tempDir) {
    InternalVirtualModel vsum = createDefaultVirtualModel(tempDir);
    vsum.dispose();
    vsum = createDefaultVirtualModel(tempDir);
  }

  @Test
  void reloadFilledVirtualModel(@TempDir Path tempDir) {
    InternalVirtualModel vsum = createDefaultVirtualModel(tempDir);
    addUVLModel(vsum, tempDir);
    vsum.dispose();
    vsum = createDefaultVirtualModel(tempDir);
    // Assert that the reloaded virtual model contains the changes we made before disposing it
    Assertions.assertEquals(1, getDefaultView(vsum, List.of(UVLModel.class)).getRootObjects().size());
    Assertions.assertEquals(1, getDefaultView(vsum, List.of(ConfigurationModel.class)).getRootObjects().size());
  }

  @Test
  void uvlInsertionAndPropagationTest(@TempDir Path tempDir) {
    VirtualModel vsum = createDefaultVirtualModel(tempDir);
    addUVLModel(vsum, tempDir);
    // assert that the inserted UVLModel is present
    Assertions.assertEquals(1, getDefaultView(vsum, List.of(UVLModel.class)).getRootObjects().size());
    // as well as the ConfigurationModel that should be created by the Reactions
    Assertions.assertEquals(1, getDefaultView(vsum, List.of(ConfigurationModel.class)).getRootObjects().size());
  }

  private void addUVLModel(VirtualModel vsum, Path projectPath) {
    CommittableView view = getDefaultView(vsum, List.of(UVLModel.class)).withChangeDerivingTrait();
    modifyView(view, (CommittableView v) -> {
      v.registerRoot(
          uvlFactory.eINSTANCE.createUVLModel(),
          URI.createFileURI(projectPath.toString() + "/example.uvl"));
    });

  }

  private InternalVirtualModel createDefaultVirtualModel(Path projectPath) {
    InternalVirtualModel model = new VirtualModelBuilder()
        .withStorageFolder(projectPath)
        .withUserInteractorForResultProvider(new TestUserInteraction.ResultProvider(new TestUserInteraction()))
        .withChangePropagationSpecifications(new Feature2configChangePropagationSpecification())
        .buildAndInitialize();
    model.setChangePropagationMode(ChangePropagationMode.TRANSITIVE_CYCLIC);
    return model;
  }

  // See https://github.com/vitruv-tools/Vitruv/issues/717 for more information
  // about the rootTypes
  private View getDefaultView(VirtualModel vsum, Collection<Class<?>> rootTypes) {
    var selector = vsum.createSelector(ViewTypeFactory.createIdentityMappingViewType("default"));
    selector.getSelectableElements().stream()
        .filter(element -> rootTypes.stream().anyMatch(it -> it.isInstance(element)))
        .forEach(it -> selector.setSelected(it, true));
    return selector.createView();
  }

  // These functions are only for convience, as they make the code a bit better
  // readable
  private void modifyView(CommittableView view, Consumer<CommittableView> modificationFunction) {
    modificationFunction.accept(view);
    view.commitChanges();
  }

}
