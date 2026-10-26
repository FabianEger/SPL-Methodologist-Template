package mir.routines.combinedUVLToCAD;

import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutinesFacade;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutinesFacadesProvider;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.ReactionsImportPath;

@SuppressWarnings("all")
public class CombinedUVLToCADRoutinesFacadesProvider extends AbstractRoutinesFacadesProvider {
  public CombinedUVLToCADRoutinesFacadesProvider(final ReactionExecutionState executionState) {
    super(executionState);
  }

  public AbstractRoutinesFacade createRoutinesFacade(final ReactionsImportPath reactionsImportPath) {
    switch(reactionsImportPath.getPathString()) {
    	case "combinedUVLToCAD": {
    		return new mir.routines.combinedUVLToCAD.CombinedUVLToCADRoutinesFacade(this, reactionsImportPath);
    	}
    	case "combinedUVLToCAD.feature2cad": {
    		return new mir.routines.feature2cad.Feature2cadRoutinesFacade(this, reactionsImportPath);
    	}
    	case "combinedUVLToCAD.featureRoot2cad": {
    		return new mir.routines.featureRoot2cad.FeatureRoot2cadRoutinesFacade(this, reactionsImportPath);
    	}
    	default: {
    		throw new IllegalArgumentException("Unexpected import path: " + reactionsImportPath.getPathString());
    	}
    }
  }
}
