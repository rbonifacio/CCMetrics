package br.unb.cic.metrics.pp;

import br.unb.cic.metrics.model.Component;

import java.util.HashMap;
import java.util.Set;

public interface PrettyPrinter {
    void exportComponentChanges(HashMap<String, Set<String>> changeSet);
    void exportComponentDependencies(HashMap<String, Component> components, int minSupportCount);
}
