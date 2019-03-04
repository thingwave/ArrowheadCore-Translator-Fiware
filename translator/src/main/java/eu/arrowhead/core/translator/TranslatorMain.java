/*
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.core.translator;

import eu.arrowhead.common.ArrowheadMain;
import eu.arrowhead.common.misc.CoreSystem;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class TranslatorMain extends ArrowheadMain {

private TranslatorMain(String[] args) {
	Set<Class<?>> classes = new HashSet<>(Collections.singleton(TranslatorResource.class));
        classes.addAll(Collections.singleton(FiwareResource.class));
        classes.addAll(Collections.singleton(FiwareVersionResource.class));
	String[] packages = {"eu.arrowhead.common", "eu.arrowhead.core.translator"};
	init(CoreSystem.TRANSLATOR, args, classes, packages);
	listenForInput();
}

public static void main(String[] args) {
    new TranslatorMain(args);
  }
}
