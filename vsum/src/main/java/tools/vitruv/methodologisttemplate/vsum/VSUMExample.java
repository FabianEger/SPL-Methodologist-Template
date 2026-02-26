package tools.vitruv.methodologisttemplate.vsum;

import tools.vitruv.framework.vsum.VirtualModelBuilder;
import UVLPackage.uvlFactory;

import java.nio.file.Path;
import java.util.function.Consumer;
import mir.reactions.feature2config.Feature2configChangePropagationSpecification;
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
    VirtualModel vsum = createDefaultVirtualModel();
    CommittableView view = getDefaultView(vsum).withChangeDerivingTrait();
    modifyView(view, (CommittableView v) -> {
      v.getRootObjects().add(uvlFactory.eINSTANCE.createUVLModel());
    });
  }

  private static VirtualModel createDefaultVirtualModel() {
    return new VirtualModelBuilder()
        .withStorageFolder(Path.of("vsumexample"))
        .withUserInteractorForResultProvider(new TestUserInteraction.ResultProvider(new TestUserInteraction()))
        .withChangePropagationSpecifications(new Feature2configChangePropagationSpecification())
        .buildAndInitialize();
  }

  private static View getDefaultView(VirtualModel vsum) {
    var selector = vsum.createSelector(ViewTypeFactory.createIdentityMappingViewType("default"));
    selector.getSelectableElements().forEach(it -> selector.setSelected(it, true));
    return selector.createView();
  }

  private static void modifyView(CommittableView view, Consumer<CommittableView> modificationFunction) {
    modificationFunction.accept(view);
    view.commitChanges();
  }

}
