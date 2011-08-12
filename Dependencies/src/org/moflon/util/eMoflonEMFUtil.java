package org.moflon.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EStructuralFeature.Setting;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.ECrossReferenceAdapter;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

public class eMoflonEMFUtil
{
   public static ECrossReferenceAdapter adapter = new ECrossReferenceAdapter();

   private static final Logger logger = Logger.getLogger(eMoflonEMFUtil.class);

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
   
   public static EObject loadModel(URI fileURI, ResourceSet resourceSet)
   {
      // Obtain a new resource set if necessary
      if (resourceSet == null)
         resourceSet = new ResourceSetImpl();

      // Get the resource
      Resource resource = resourceSet.getResource(fileURI, true);

      // Add adapter for reverse navigation along unidirectional links 
      if(!resourceSet.eAdapters().contains(eMoflonEMFUtil.adapter))
         resourceSet.eAdapters().add(eMoflonEMFUtil.adapter);
      
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

      // Obtain a new resource set
      ResourceSet resourceSet = new ResourceSetImpl();

      // Create a resource and add model
      Resource resource = resourceSet.createResource(createFileURI(path, false));
      resource.getContents().add(root);

      // Save model to file
      try
      {
         resource.save(null);
         return true;
      } catch (IOException e)
      {
         logger.error("Unable to save model to " + path);
         return false;
      }
   }
   
   static private URI createFileURI(String path, boolean mustExist)
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
   public static Collection<?> getOppositeReference(EObject target, @SuppressWarnings("rawtypes") Class sourceType)
   {
      ensureCRAdapterIsUsed(target);
      
      Collection<EObject> returnList = new ArrayList<EObject>();

      Collection<Setting> settings = adapter.getInverseReferences(target, true);
      for (Setting setting : settings)
      {
         EClassifier clazz = setting.getEObject().eClass();
         String clazzName = clazz.getInstanceClass().getPackage().getName() + "." + clazz.getName();

         if (clazzName.equals(sourceType.getName()))
         {
            returnList.add(setting.getEObject());
         }
      }
      return returnList;
   }

   private static void ensureCRAdapterIsUsed(EObject target)
   {
      if (target.eResource() != null)
      {
         ResourceSet resourceSet = target.eResource().getResourceSet();
         if (resourceSet != null && !resourceSet.eAdapters().contains(eMoflonEMFUtil.adapter))
            logger.error("The model element " + target
                  + " might not have been loaded with a crossreferencing adapter!  Navigation along non-navigable links is probably not possible.");
      }
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
}
