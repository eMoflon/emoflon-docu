package org.moflon.util;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

/**
 * Simple utility methods for working with EMF models.
 * 
 * @author anjorin
 * @author (last editor) $Author$
 * @version $Revision$ $Date$
 */
public class EMFPersistenceUtil
{
   private static final Logger logger = Logger.getLogger(EMFPersistenceUtil.class);

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

      // Obtain a new resource set if necessary
      if (resourceSet == null)
         resourceSet = new ResourceSetImpl();

      // Get the resource
      Resource resource = resourceSet.getResource(createFileURI(path, true), true);

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
}
