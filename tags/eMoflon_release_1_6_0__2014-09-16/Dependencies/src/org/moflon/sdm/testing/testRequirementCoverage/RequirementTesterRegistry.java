package org.moflon.sdm.testing.testRequirementCoverage;

import java.util.HashMap;
import java.util.Map;

public abstract class RequirementTesterRegistry<T> {	
	
	Map<String, T> fullyQualifiedClassName2TesterMap = new HashMap<String, T>();
	
	public RequirementTesterRegistry(Map<String, T> initialMappings) {
		if (initialMappings != null)
			fullyQualifiedClassName2TesterMap.putAll(initialMappings);
		map.put(this.getClass().getCanonicalName(), this);
	}
	
	public void put(String fullyQualifiedClassName, T testerInstance) {
		fullyQualifiedClassName2TesterMap.put(fullyQualifiedClassName, testerInstance);
	}
	
	public void putAll(Map<String, T> additionalMappings) {
		if (additionalMappings != null)
			fullyQualifiedClassName2TesterMap.putAll(additionalMappings);
	}
	
	public T getTester(String fullyQualifiedClassName) {
		return fullyQualifiedClassName2TesterMap.get(fullyQualifiedClassName);
	}
	
	private static Map<String, RequirementTesterRegistry<?>> map = new HashMap<String, RequirementTesterRegistry<?>>();
	
	public static RequirementTesterRegistry<?> getInstance(String s) {
		return map.get(s);
	}
}
