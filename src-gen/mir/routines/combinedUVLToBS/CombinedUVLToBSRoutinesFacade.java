package mir.routines.combinedUVLToBS;

import mir.routines.feature2brakesystem.Feature2brakesystemRoutinesFacade;
import mir.routines.featureRoot2brakesystem.FeatureRoot2brakesystemRoutinesFacade;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutinesFacade;
import tools.vitruv.dsls.reactions.runtime.routines.RoutinesFacadesProvider;
import tools.vitruv.dsls.reactions.runtime.structure.ReactionsImportPath;

@SuppressWarnings("all")
public class CombinedUVLToBSRoutinesFacade extends AbstractRoutinesFacade {
  public CombinedUVLToBSRoutinesFacade(final RoutinesFacadesProvider routinesFacadesProvider, final ReactionsImportPath reactionsImportPath) {
    super(routinesFacadesProvider, reactionsImportPath);
    this.feature2brakesystem = this.getRoutinesFacadesProvider().getRoutinesFacade(this.getReactionsImportPath().append(ReactionsImportPath.fromPathString("feature2brakesystem")));
    this.featureRoot2brakesystem = this.getRoutinesFacadesProvider().getRoutinesFacade(this.getReactionsImportPath().append(ReactionsImportPath.fromPathString("featureRoot2brakesystem")));
  }

  public final Feature2brakesystemRoutinesFacade feature2brakesystem;

  public final FeatureRoot2brakesystemRoutinesFacade featureRoot2brakesystem;
}
