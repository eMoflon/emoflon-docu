package org.moflon.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EStructuralFeature.Setting;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EContentsEList;
import org.eclipse.emf.ecore.util.ECrossReferenceAdapter;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class eMoflonEMFUtil
{
   private static final Logger logger = Logger.getLogger(eMoflonEMFUtil.class);

   private static Map<EClassifier, String> clazzNames = new HashMap<EClassifier, String>();

   /*
    * 
    * 
    * 
    * Initialization Methods
    */

   /**
    * If using EMF from plain Java (e.g. JUnit Tests), invoke to register EMF defaults as necessary. In a plugin
    * context, this is not necessary. Note that this method is called on demand from {@link #init(EPackage)} and
    * loading/saving methods.
    */
   public static void registerXMIFactoryAsDefault()
   {
      // Add XMI factory to registry
      Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
      Map<String, Object> m = reg.getExtensionToFactoryMap();
      m.put("*", new XMIResourceFactoryImpl());
   }

   /**
    * Use this method to initialize the given EPackage. This is required before loading/saving or working with the
    * package. In a plugin context, this might be automatically carried out via an appropriate extension point.
    */
   public static void init(EPackage metamodel)
   {
      registerXMIFactoryAsDefault();
      metamodel.getName();
   }

   /* Methods for loading models */

   /**
    * Use to load a model if its metamodel has been initialized already and there are no dependencies to other models.
    * 
    * @param pathToXMIFile
    *           Absolute or relative path to XMI file.
    * @return the root element of the loaded model
    */
   public static EObject loadModel(String pathToXMIFile)
   {
      return loadModelWithDependencies(pathToXMIFile, null);
   }

   /**
    * Use to load a model if its metamodel has been initialized already and dependencies to other models are to be
    * resolved using the supplied resourceSet.
    * 
    * @param pathToXMIFile
    *           Absolute or relative path to XMI file.
    * @param dependencies
    *           Contains other models to resolve dependencies.
    * @return the root element of the loaded model with resolved dependencies.
    */
   public static EObject loadModelWithDependencies(String pathToXMIFile, ResourceSet dependencies)
   {
      return loadModelWithDependenciesAndCrossReferencer(createFileURI(pathToXMIFile, true), dependencies);
   }

   /**
    * Use to load a model and initialize its metamodel
    * 
    * @param metamodel
    *           Metamodel (for initialization)
    * @param pathToXMIFile
    *           Absolute or relative path to XMI file
    * 
    * @return the root element of the loaded model
    */
   public static EObject loadAndInitModel(EPackage metamodel, String pathToXMIFile)
   {
      init(metamodel);
      return loadModelWithDependencies(pathToXMIFile, null);
   }

   /**
    * Use to load a model, initialize its metamodel, and resolve dependencies
    * 
    * @param metamodel
    *           Metamodel (for initialization)
    * @param pathToXMIFile
    *           Absolute or relative path to XMI file
    * @param dependencies
    *           Contains other models to resolve dependencies.
    * @return the root element of the loaded model
    */
   public static EObject loadAndInitModelWithDependencies(EPackage metamodel, String pathToXMIFile, ResourceSet dependencies)
   {
      init(metamodel);
      return loadModelWithDependencies(pathToXMIFile, dependencies);
   }

   /**
    * Use this method directly only if you know what you are doing! The corresponding metamodel must be initialized
    * already. The model is loaded with all dependencies, and a cross reference adapter is added to enable inverse
    * navigation.
    * 
    * @param uriToModelResource
    *           URI of resource containing model
    * @param dependencies
    *           Contains other models to resolve dependencies
    * @return the root element of the loaded model
    */
   public static EObject loadModelWithDependenciesAndCrossReferencer(URI uriToModelResource, ResourceSet dependencies)
   {
	  registerXMIFactoryAsDefault(); 
	   
      // Obtain a new resource set if necessary
      if (dependencies == null)
         dependencies = new ResourceSetImpl();

      // Get the resource (load on demand)
      Resource resource = dependencies.getResource(uriToModelResource, true);

      // Add adapter for reverse navigation along unidirectional links
      ECrossReferenceAdapter adapter = ECrossReferenceAdapter.getCrossReferenceAdapter(dependencies);
      if (adapter == null){
         try {         
            dependencies.eAdapters().add(new ECrossReferenceAdapter());
         } catch(Exception e){
            e.printStackTrace();
         }
      }

      // Return root model element
      return resource.getContents().get(0);
   }

   /**
    * Use this method directly only if you know what you are doing! This method not only loads a model but also adds a
    * URIMap-entry, mapping the model's default "nsURI" to the URI from loading the resource in the supplied file. The
    * corresponding metamodel must be initialized already.
    * 
    * @param pathToXMIFile
    *           Absolute or relative path to XMI file
    * @param dependencies
    *           Contains other models to resolve dependencies
    * @return the root element of the loaded model
    * @throws IOException
    *            if uri does not point to a valid/loadable xmi document
    * @throws IllegalStateException
    *            if something else goes wrong (e.g. xmi document could not be parsed correctly)
    */
   public static EObject loadModelAndAddUriMapping(String pathToXMIFile, ResourceSet dependencies) throws IOException
   {

      File file = new File(pathToXMIFile);
      URI resourceURI = createFileURI(pathToXMIFile, true);

      try
      {
         // Retrieve package URI from XMI file (this must be done before loading!)
         DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
         DocumentBuilder docBuilder = dbf.newDocumentBuilder();
         Document doc = docBuilder.parse(file);
         doc.getDocumentElement().normalize();
         NodeList packageElements = doc.getElementsByTagName("ecore:EPackage");
         Node epackageNode = packageElements.item(0);
         NamedNodeMap attributes = epackageNode.getAttributes();
         Node uriAttribute = attributes.getNamedItem("nsURI");
         String nsUriAsString = uriAttribute.getNodeValue();

         URI packageURI = URI.createURI(nsUriAsString);

         // Create mapping
         dependencies.getURIConverter().getURIMap().put(packageURI, resourceURI);
         logger.debug(String.format("Adding URI mapping: %1$s -> %2$s", packageURI, resourceURI));
      } catch (ParserConfigurationException e)
      {
         throw new IllegalStateException(e);
      } catch (SAXException e)
      {
         throw new IllegalStateException(e);
      }

      return loadModelWithDependenciesAndCrossReferencer(resourceURI, dependencies);
   }

   /*
    * 
    * 
    * 
    * Methods for saving models
    */

   /**
    * Use to save a model to the given XMI file path.
    * 
    * @param rootElementOfModel
    * @param pathToXMIFile
    *           Absolute or relative path to XMI file in which to save the model.
    */
   static public void saveModel(EObject rootElementOfModel, String pathToXMIFile)
   {
      saveModel(rootElementOfModel, createFileURI(pathToXMIFile, false), new ResourceSetImpl());
   }

   /**
    * Use to save a model with dependencies to other models to the given XMI file path.
    * 
    * @param rootElementOfModel
    * @param pathToXMIFile
    *           Absolute or relative path to XMI file in which to save the model.
    * @param dependencies
    *           Contains other models to resolve dependencies.
    */
   static public void saveModelWithDependencies(EObject rootElementOfModel, String pathToXMIFile, ResourceSet dependencies)
   {
      saveModel(rootElementOfModel, createFileURI(pathToXMIFile, false), dependencies);
   }

   /**
    * Use this method directly only if you know what you are doing! Use to save a model with dependencies to the given
    * resource URI.
    * 
    * @param rootElementOfModel
    * @param uriToModelResource
    * @param dependencies
    *           Contains other models to resolve dependencies.
    */
   static public void saveModel(EObject rootElementOfModel, URI uriToModelResource, ResourceSet dependencies)
   {
      if (rootElementOfModel == null)
         throw new IllegalArgumentException("The model to be saved cannot be null");

      registerXMIFactoryAsDefault();
      // Create a resource and add model
      Resource resource = dependencies.createResource(uriToModelResource);
      resource.getContents().add(rootElementOfModel);

      // Save model to file
      try
      {
         resource.save(null);
      } catch (IOException e)
      {
         String errorMessage = "Unable to save model to " + uriToModelResource + ". Error:" + e.getMessage();
         logger.error(errorMessage);
         throw new IllegalStateException(errorMessage, e);
      }
   }

   /*
    * 
    * 
    * 
    * EMF Helper Methods
    */

   /**
    * Create and return a file URI for the given path.
    * 
    * @param pathToXMIFile
    * @param mustExist
    *           Set true when loading (the file must exist) and false when saving (file can be newly created).
    * @return
    */
   static public URI createFileURI(String pathToXMIFile, boolean mustExist)
   {
      File filePath = new File(pathToXMIFile);
      if (!filePath.exists() && mustExist)
         throw new IllegalArgumentException(pathToXMIFile + " does not exist.");

      return URI.createFileURI(filePath.getAbsolutePath());
   }

   /**
    * This method only works when you have registered an appropriate adapter right after loading your model! Further
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

   public static String getClazzNameWithPackagePrefix(EClassifier clazz)
   {
      String clazzName = clazzNames.get(clazz);

      if (clazzName == null)
      {
         clazzName = clazz.getInstanceClass().getPackage().getName() + "." + clazz.getName();
         clazzNames.put(clazz, clazzName);
      }
      return clazzName;
   }

   public static boolean checkInheritance(Class superclass, EClassifier subclass)
   {
      for (EClass sup : ((EClass) subclass).getEAllSuperTypes())
      {
         String clazzName = getClazzNameWithPackagePrefix(sup);
         if (clazzName.equals(superclass.getName()))
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
         if (resourceSet != null)
            context = resourceSet;
         else
            context = resource;
      } else
         context = root;

      // Retrieve adapter and create+add on demand
      ECrossReferenceAdapter adapter = ECrossReferenceAdapter.getCrossReferenceAdapter(context);
      if (adapter == null)
      {
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

   public static EStructuralFeature getReference(EObject obj, String name)
   {
      for (EContentsEList.FeatureIterator featureIterator = (EContentsEList.FeatureIterator) obj.eCrossReferences().iterator(); featureIterator.hasNext();)
      {
         featureIterator.next();
         EReference eReference = (EReference) featureIterator.feature();
         if (eReference.getName().equals(name))
            return eReference;
      }

      return null;
   }

   public static EStructuralFeature getContainment(EObject container, String name)
   {
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
    * 
    * @param object
    * @return set of references
    */
   public static Set<EStructuralFeature> getAllReferences(EObject object)
   {
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

   public static String getName(EObject child)
   {
      Object name = "";

      EStructuralFeature nameFeature = (EStructuralFeature) child.eClass().getEStructuralFeature("name");

      if (nameFeature != null)
         name = child.eGet(nameFeature);

      return (String) (name instanceof String && !((String) name).equals("") ? name : child.toString());
   }

   public static void addToResourceSet(ResourceSet set, EObject object)
   {
      Resource resource = object.eResource();
      if (resource == null)
      {
         resource = new ResourceImpl();
         resource.setURI(URI.createURI(object.eClass().getEPackage().getNsURI()));
      }

      resource.getContents().add(object);
      set.getResources().add(resource);
   }
}
