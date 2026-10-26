package mir.routines.combinedUVLToCAD;

import mir.routines.feature2cad.Feature2cadRoutinesFacade;
import mir.routines.featureRoot2cad.FeatureRoot2cadRoutinesFacade;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutinesFacade;
import tools.vitruv.dsls.reactions.runtime.routines.RoutinesFacadesProvider;
import tools.vitruv.dsls.reactions.runtime.structure.ReactionsImportPath;

@SuppressWarnings("all")
public class CombinedUVLToCADRoutinesFacade extends AbstractRoutinesFacade {
  public CombinedUVLToCADRoutinesFacade(final RoutinesFacadesProvider routinesFacadesProvider, final ReactionsImportPath reactionsImportPath) {
    super(routinesFacadesProvider, reactionsImportPath);
    this.feature2cad = this.getRoutinesFacadesProvider().getRoutinesFacade(this.getReactionsImportPath().append(ReactionsImportPath.fromPathString("feature2cad")));
    this.featureRoot2cad = this.getRoutinesFacadesProvider().getRoutinesFacade(this.getReactionsImportPath().append(ReactionsImportPath.fromPathString("featureRoot2cad")));
  }

  public final Feature2cadRoutinesFacade feature2cad;

  public final FeatureRoot2cadRoutinesFacade featureRoot2cad;
}
