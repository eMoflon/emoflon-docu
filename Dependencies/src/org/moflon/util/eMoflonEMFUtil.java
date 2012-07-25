package org.moflon.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EStructuralFeature.Setting;
import org.eclipse.emf.ecore.impl.EPackageImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EContentsEList;
import org.eclipse.emf.ecore.util.ECrossReferenceAdapter;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.PackageNotFoundException;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

public class eMoflonEMFUtil
{
   private static final Logger logger = Logger.getLogger(eMoflonEMFUtil.class);
   
   private static Map<EClassifier, String> clazzNames = new HashMap<EClassifier, String>();
   
   public final static String SDM_SOURCE_KEY = "SDM";
   public final static String SDM_ANNOTATION_KEY = "XMI";
   
   /**
    * Simple utility method to be used for testing. Loads a model from path.
    * 
    * @param ePackage
    *           Package of metamodel (for initialization)
    * @param path
    *           Local file path to model to be loaded
    * @param resourceSet
    *           Can be null if resources are not connected
    * 
    * @return Loaded model
    */
  static public EObject loadModel(EPackage ePackage, String path, ResourceSet resourceSet)
   {
      initEMF(ePackage);

      return loadModel(createFileURI(path, true), resourceSet);
   }
   
   public static EObject loadModel(URI uri, ResourceSet resourceSet)
   {
      // Obtain a new resource set if necessary
      if (resourceSet == null)
         resourceSet = new ResourceSetImpl();

      // Get the resource
      Resource resource = resourceSet.getResource(uri, true);

      // Add adapter for reverse navigation along unidirectional links
      ECrossReferenceAdapter adapter = ECrossReferenceAdapter.getCrossReferenceAdapter(resourceSet);
      if(adapter == null)
         resourceSet.eAdapters().add(new ECrossReferenceAdapter());
      
      // Return root model element
      return resource.getContents().get(0);
   }

  /**
    * Simple utility method for testing, saves model to file.
    * 
    * @param ePackage
    *           Package of metamodel (for initialization)
    * @param root
    *           Model to be saved
    * @param path
    *           Local destination for xmi file
    * @return
    */
   static public boolean saveModel(EPackage ePackage, EObject root, String path)
   {
      initEMF(ePackage);

      return saveModel(root, createFileURI(path, false));
   }
   
   static public boolean saveModel(EObject root, String path, ResourceSet resourceSet)
   {
      return saveModel(root, createFileURI(path, false),resourceSet);
   }
   
   static public boolean saveModel(EObject root, URI path){
	  if(root == null)
		  throw new IllegalArgumentException("The model to be saved cannot be null");
	   
      // Obtain a new resource set
      ResourceSet resourceSet = new ResourceSetImpl();

      return saveModel(root, path, resourceSet);
   }
   
   static public boolean saveModel(EObject root, URI path, ResourceSet resourceSet){
      // Create a resource and add model
      Resource resource = resourceSet.createResource(path);
      resource.getContents().add(root);

      // Save model to file
      try
      {
         resource.save(null);
         return true;
      } catch (IOException e)
      {
         logger.error("Unable to save model to " + path + ". Error:" + e.getMessage());
         e.printStackTrace();
         return false;
      }

   }

  
   static public URI createFileURI(String path, boolean mustExist)
   {
      File filePath = new File(path);
      if (!filePath.exists() && mustExist)
         throw new IllegalArgumentException(path + " does not exist.");

      return URI.createFileURI(filePath.getAbsolutePath());
   }

   static private void initEMF(EPackage ePackage)
   {
      // Initialize the model
      logger.debug("Initializing " + ePackage.getName());

      registerXMIFactoryAsDefault();
   }

   public static void registerXMIFactoryAsDefault()
   {
      // Add XMI factory to registry
      Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
      Map<String, Object> m = reg.getExtensionToFactoryMap();
      m.put("*", new XMIResourceFactoryImpl());
   }

   /**
    * This method only works when you registered an appropriate adapter right after loading your model! Further
    * documentation can be found here: http://sdqweb.ipd.kit.edu/wiki/EMF_Reverse_Lookup_/
    * _navigating_unidirectional_references_bidirectional
    * 
    * @param target
    *           the target of this reference
    * @param sourceType
    *           the type of the opposite objects you are looking for
    * @return a collection of all opposite objects
    */
   public static Collection<?> getOppositeReference(EObject target, @SuppressWarnings("rawtypes") Class sourceType, String targetRoleName)
   {
      ECrossReferenceAdapter adapter = getCRAdapter(target);

      Collection<EObject> returnList = new ArrayList<EObject>();

      Collection<Setting> settings = adapter.getInverseReferences(target, true);
      for (Setting setting : settings)
      {
         if (setting.getEStructuralFeature().getName().equals(targetRoleName))
         {
            EClassifier clazz = setting.getEObject().eClass();
            String clazzName = getClazzNameWithPackagePrefix(clazz);

            if (clazzName.equals(sourceType.getName()) || checkInheritance(sourceType, clazz))
               returnList.add(setting.getEObject());
         }
      }

      return returnList;
   }

	public static String getClazzNameWithPackagePrefix(EClassifier clazz) {
		String clazzName = clazzNames.get(clazz);

		if (clazzName == null) {
			clazzName = clazz.getInstanceClass().getPackage().getName() + "." + clazz.getName();
			clazzNames.put(clazz, clazzName);
		}
		return clazzName;
	}

	public static boolean checkInheritance(Class superclass, EClassifier subclass) {
		for (EClass sup : ((EClass)subclass).getEAllSuperTypes()) {
			String clazzName = getClazzNameWithPackagePrefix(sup);
			if(clazzName.equals(superclass.getName()))
				return true;
		}
		return false;		
	}

   private static ECrossReferenceAdapter getCRAdapter(EObject target)
   {
      // Determine context
      Notifier context = null;
      
      EObject root = EcoreUtil.getRootContainer(target, true);
      Resource resource = root.eResource();
      
      if (resource != null)
      {
         ResourceSet resourceSet = resource.getResourceSet();
         if(resourceSet != null)
            context = resourceSet;
         else
            context = resource;
      }else
         context = root;
      
      // Retrieve adapter and create+add on demand
      ECrossReferenceAdapter adapter = ECrossReferenceAdapter.getCrossReferenceAdapter(context);
      if(adapter == null){
         adapter = new ECrossReferenceAdapter();
         context.eAdapters().add(adapter);
      }
      
      return adapter;
   }

   public static void remove(EObject object)
   {
      EcoreUtil.delete(object, true);
   }

   @SuppressWarnings({ "unchecked", "rawtypes" })
   public static void addOppositeReference(EObject source, EObject target, String targetRole)
   {
      EStructuralFeature reference = source.eClass().getEStructuralFeature(targetRole);
      if (!reference.isMany())
      {
         source.eSet(reference, target);
      } else
         ((Collection) source.eGet(reference)).add(target);
   }

   @SuppressWarnings({ "rawtypes" })
   public static void removeOppositeReference(EObject source, EObject target, String targetRole)
   {
      EStructuralFeature reference = source.eClass().getEStructuralFeature(targetRole);
      if (!reference.isMany())
      {
         source.eSet(reference, null);
      } else
         ((Collection) source.eGet(reference)).remove(target);

   }
   
   public static EStructuralFeature getReference(EObject obj, String name){
      for (EContentsEList.FeatureIterator featureIterator = (EContentsEList.FeatureIterator) obj.eCrossReferences().iterator(); featureIterator.hasNext();)
      {
         featureIterator.next();
         EReference eReference = (EReference) featureIterator.feature();
         if (eReference.getName().equals(name))
            return eReference;
      }

      return null;
   }
   
   public static EStructuralFeature getContainment(EObject container, String name){
      for (EContentsEList.FeatureIterator featureIterator = (EContentsEList.FeatureIterator) container.eContents().iterator(); featureIterator.hasNext();)
      {
         featureIterator.next();
         EReference eReference = (EReference) featureIterator.feature();
         if (eReference.getName().equals(name))
            return eReference;
      }

      return null;
   }
   
   /**
    * Calculates the set of all outgoing references of a given EObject. 
    * @param object
    * @return set of references
    */
   public static Set<EStructuralFeature> getAllReferences(EObject object){
	   EList<EStructuralFeature> references = new BasicEList<EStructuralFeature>();
	   for (EContentsEList.FeatureIterator featureIterator = (EContentsEList.FeatureIterator) object.eCrossReferences().iterator(); featureIterator.hasNext();)
	   {
		   featureIterator.next();
		   references.add(featureIterator.feature());
	   }
	   for (EContentsEList.FeatureIterator featureIterator = (EContentsEList.FeatureIterator) object.eContents().iterator(); featureIterator.hasNext();)
	   {
		   featureIterator.next();
		   references.add(featureIterator.feature());
	   }
	   return new HashSet<EStructuralFeature>(references);
   }

	public static String getName(EObject child) {		
		Object name = "";
		
		EStructuralFeature nameFeature = (EStructuralFeature) child.eClass().getEStructuralFeature("name");		
				
		if(nameFeature != null)
			name = child.eGet(nameFeature);
		
		return (String) (name instanceof String && !((String) name).equals("") ? name : child.toString());
	}
	
	public static void addToResourceSet(ResourceSet set, EObject object) {
		Resource resource = new ResourceImpl();
		resource.setURI(URI.createURI(object.eClass().getEPackage().getNsURI()));
		resource.getContents().add(object);
		set.getResources().add(resource);
	}
	
   public static void embedSDMInEAnnotation(EObject sdm, EAnnotation eAnnotation)
   {
      ResourceSet resourceSet = new ResourceSetImpl();
      embedSDMInEAnnotation(sdm, eAnnotation, resourceSet);
   }

   public static void embedSDMInEAnnotation(EObject sdm, EAnnotation eAnnotation, ResourceSet resourceSet)
   {
      Resource resource = resourceSet.createResource(URI.createURI(""));
      resource.getContents().add(sdm);

      StringWriter writer = new StringWriter();
      OutputStream out = new URIConverter.WriteableOutputStream(writer, "UTF-8");
      try
      {
         resource.save(out, null);
      } catch (IOException e)
      {
         e.printStackTrace();
      }

      eAnnotation.getDetails().put(SDM_ANNOTATION_KEY, writer.toString());
   }
   
   /**
    * This method extracts the XMI-representation of a SDM form an EAnnotation. It returns the Activity (from SDMLanguage)
    * as a generic EObject due to technical access restrictions. Callers should cast the result to Activity if needed. 
    * 
    * @param annotation The annotation that wraps an XMI document (for key="XMI") which describes an SDM activity/diagram.
    * @param resourceSet A ResourceSet that contains/describes/references all necessary EMF models (typically e.g. referenced by the SDM transformation + SDM itself)
    * @param operationName The name of the operation that was implemented with SDM (used to construct a temporary URI). 
    * @return EObject representation of the Activity (from SDMLanguage) 
    */
   private static EObject getActivityFromAnnotation(EAnnotation annotation, ResourceSet resourceSet, String operationName) {
	   if (resourceSet == null)
		   throw new IllegalArgumentException("Parameter 'resourceSet' may not be null!");
	      
	   EObject result = null;
	   
	   if (!SDM_SOURCE_KEY.equals(annotation.getSource()))
		   return null;
	   
	   EMap<String,String> details = annotation.getDetails();
	   if (details == null)
		   return null;
				   
	   String xmiDoc = details.get(SDM_ANNOTATION_KEY);
	   Resource r = resourceSet.createResource(URI.createURI(operationName + ".sdm"));
	   try {
		   registerXMIFactoryAsDefault();
		   // try to load the resource - it might be, that the user did not remember to initialize SDMLanguage properly
		   // the following code thus attempts to load the resource once and tries to dynamically load SDMLanguage on demand
		   // if this latter step succeeds, then the resource is tried to be loaded again  
		   try {
			   r.load(new ByteArrayInputStream(xmiDoc.getBytes()), null);
			   try {
				   result = (EObject) r.getContents().get(0);
			   } catch (Exception e) {
				   throw new Exception("Unable to retrieve a valid EObject instance from the xmi-document wrapped in the given annotation!", e);
			   }
			   if (result == null) {
				   throw new Exception("Unable to retrieve a valid EObject instance from the xmi-document wrapped in the given annotation!");
			   } else {
				   return result;
			   }
		   } catch (Exception e) {
			   if (e instanceof Resource.IOWrappedException) {
				   Throwable cause = e.getCause();
				   if (cause instanceof PackageNotFoundException) {
					   PackageNotFoundException e2 = (PackageNotFoundException) cause;
					   if (e2.getMessage().contains("http://www.moflon.org/SDMLanguage.activities")) {
						   // now initialize the SDMLanguagePackage properly
						   // (this is done by java reflection to prevent an explicit dependency from eMoflonEMTUtil to SDMLanguage)
						   // Warning! Be careful, the following lines depend on the correct naming of SDMLanguage and might break in case of renaming/refactoring. 
						   ClassLoader classLoader = eMoflonEMFUtil.class.getClassLoader();
						   Class<EPackageImpl> smdLanguagePackageImpl = (Class<EPackageImpl>) classLoader.loadClass("SDMLanguage.impl.SDMLanguagePackageImpl");
						   Method method = smdLanguagePackageImpl.getMethod("init", (Class<?>[]) null);						   
						   method.invoke(smdLanguagePackageImpl, (Object[]) null);
					   }
				   }
			   } else {
				   throw e;
			   }
		   }		   
		   // try to load the resource after the SDMLanguagePackage has been (reflectively) initialized (see above)
		   r = resourceSet.createResource(URI.createURI(operationName + ".sdm"));
		   r.load(new ByteArrayInputStream(xmiDoc.getBytes()), null);
		   try {
			   result = (EObject) r.getContents().get(0);
		   } catch (Exception e) {
			   throw new Exception("Unable to retrieve a valid EObject instance from the xmi-document wrapped in the given annotation!", e);
		   }
		   if (result == null) {
			   throw new Exception("Unable to retrieve a valid EObject instance from the xmi-document wrapped in the given annotation!");
		   }
	   } catch (Exception e) {
		   if (e instanceof Resource.IOWrappedException) {
			   Throwable cause = e.getCause();
			   if (cause instanceof PackageNotFoundException) {
				   PackageNotFoundException e2 = (PackageNotFoundException) cause;
				   throw new IllegalArgumentException("Error during XMI loading due to missing package information! Remeber that you should ensure that \"SDMLanguage\" is visible and previously initialized before attempting to retrieve an Activity from an eOperation-Annotation.", e);
			   }
		   } else { 
			   throw new IllegalArgumentException("Unable to interpret the given XMI document!", e);
		   }
	   }
	   return result;
   }
   
   /**
    * Convenience method to retrieve the SDM diagram (as Activity from SDMLanguage) of an operation, whose implementation
    * was specified by an SDM. 
    * 
    * @param op EOperation which carries an Annotation with details.key="XMI" and details.value=<XMI document of SDM specification> 
    * @param resourceSet A ResourceSet that contains/describes/references all necessary EMF models (typically e.g. referenced by the SDM transformation + SDM itself)
    * @return EObject representation of the Activity (from SDMLanguage)
    */   
   public static EObject getActivityFromEOperation(EOperation op, ResourceSet resourceSet) {
	   EAnnotation annotation = null;
	   
	   for (EAnnotation temp : op.getEAnnotations()) {
		   if (SDM_SOURCE_KEY.equals(temp.getSource()) && temp.getDetails().keySet().contains(SDM_ANNOTATION_KEY)) {
			   annotation = temp;
			   break;
		   }
	   }
	   
	   if (annotation != null)
		   return getActivityFromAnnotation(annotation, resourceSet, op.getName());
	   
	   return null;
   }

}
