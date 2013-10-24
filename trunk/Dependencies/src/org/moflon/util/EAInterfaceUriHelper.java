package org.moflon.util;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EParameter;

public class EAInterfaceUriHelper {

	   public final static String DELIM = "/"; 
	   
	   public static String getObjVarString(String objectVariableName) {
		   return (objectVariableName + ":ObjectVariable");
	   }
	   
	   public static String getLinkVarString(String targetName, String sourceName) {
		   return ("source:" + targetName + "_target:" + sourceName + ":LinkVariable");
	   }
	   
	   public static String getStoryNodeString(String storyNodeName) {
		   return (storyNodeName + ":StoryNode");
	   }
	   
	   public static String getStopNodeString(String stopNodeName) {
		   return (stopNodeName + ":StopNode");
	   }
	   
	   public static String getStartNodeString(String startNodeName) {
		   return (startNodeName + ":StartNode");
	   }
	   
	   public static String getActivityEdgeString(String activityEdgeSourceName, String activityEdgeTargetName) {
		   return ("source:" + activityEdgeSourceName + "_target:" + activityEdgeTargetName + ":ActivityEdge");
	   }

	   
	   public static String getActivityString() {
		   return "Activity:Activity";
	   }
	   
	   public static String getEOperationString(String eOperationName) {
		   return (eOperationName + ":EOperation");
	   }
	   
	   public static String getEParameterString(EList<EParameter> eParameters)
		{
			String eParamString = "(";
			for(EParameter eParam : eParameters)
			{
				eParamString += eParam.getName() + ":" + eParam.getEType().getName() + ",";
			}
			if(eParameters.size() > 0 )
			{
				eParamString = eParamString.substring(0,eParamString.length() - 1);
			}
			eParamString += ")";
			return eParamString;
			
		}
	   
	   public static String getEClassString(String eClassName) {
		   return (eClassName + ":EClass");
	   }
	   
	   public static String getEPackageString(String ePackageName) {
		   return (ePackageName + ":EPackage");
	   }
}