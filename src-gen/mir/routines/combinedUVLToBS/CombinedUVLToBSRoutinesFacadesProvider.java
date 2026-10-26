package mir.routines.combinedUVLToBS;

import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutinesFacade;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutinesFacadesProvider;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.ReactionsImportPath;

@SuppressWarnings("all")
public class CombinedUVLToBSRoutinesFacadesProvider extends AbstractRoutinesFacadesProvider {
  public CombinedUVLToBSRoutinesFacadesProvider(final ReactionExecutionState executionState) {
    super(executionState);
  }

  public AbstractRoutinesFacade createRoutinesFacade(final ReactionsImportPath reactionsImportPath) {
    switch(reactionsImportPath.getPathString()) {
    	case "combinedUVLToBS": {
    		return new mir.routines.combinedUVLToBS.CombinedUVLToBSRoutinesFacade(this, reactionsImportPath);
    	}
    	case "combinedUVLToBS.feature2brakesystem": {
    		return new mir.routines.feature2brakesystem.Feature2brakesystemRoutinesFacade(this, reactionsImportPath);
    	}
    	case "combinedUVLToBS.featureRoot2brakesystem": {
    		return new mir.routines.featureRoot2brakesystem.FeatureRoot2brakesystemRoutinesFacade(this, reactionsImportPath);
    	}
    	default: {
    		throw new IllegalArgumentException("Unexpected import path: " + reactionsImportPath.getPathString());
    	}
    }
  }
}
