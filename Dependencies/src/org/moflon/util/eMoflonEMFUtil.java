package org.moflon.util;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EStructuralFeature.Setting;
import org.eclipse.emf.ecore.InternalEObject;
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

   private static Random random = new SecureRandom();

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

   public static final void installCrossReferencers(final ResourceSet resourceSet)
   {
      // Add adapter for reverse navigation along unidirectional links
      ECrossReferenceAdapter adapter = ECrossReferenceAdapter.getCrossReferenceAdapter(resourceSet);
      if (adapter == null)
      {
         try
         {
            resourceSet.eAdapters().add(new ECrossReferenceAdapter());
         } catch (Exception e)
         {
            e.printStackTrace();
         }
      }
   }

   /**
    * Use this method to initialize the given EPackage. This is required before loading/saving or working with the
    * package. In a plugin context, this might be automatically carried out via an appropriate extension point.
    */
   public static void init(final EPackage metamodel)
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
   public static EObject loadModel(final String pathToXMIFile)
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
   public static EObject loadModelWithDependencies(final String pathToXMIFile, final ResourceSet dependencies)
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
   public static EObject loadAndInitModel(final EPackage metamodel, final String pathToXMIFile)
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
   public static EObject loadAndInitModelWithDependencies(final EPackage metamodel, final String pathToXMIFile, final ResourceSet dependencies)
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
   public static EObject loadModelWithDependenciesAndCrossReferencer(final URI uriToModelResource, ResourceSet dependencies)
   {
      registerXMIFactoryAsDefault();

      // Obtain a new resource set if necessary
      if (dependencies == null)
         dependencies = new ResourceSetImpl();

      // Get the resource (load on demand)
      dependencies.createResource(uriToModelResource);
      Resource resource = dependencies.getResource(uriToModelResource, true);

      // Add adapter for reverse navigation along unidirectional links
      ECrossReferenceAdapter adapter = ECrossReferenceAdapter.getCrossReferenceAdapter(dependencies);
      if (adapter == null)
      {
         try
         {
            dependencies.eAdapters().add(new ECrossReferenceAdapter());
         } catch (Exception e)
         {
            e.printStackTrace();
            logger.debug(Arrays.toString(e.getStackTrace()));
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
   public static EObject loadModelAndAddUriMapping(final String pathToXMIFile, final ResourceSet dependencies) throws IOException
   {

      File file = new File(pathToXMIFile);
      URI resourceURI = createFileURI(pathToXMIFile, true);

      try
      {
         // Retrieve package URI from XMI file (this must be done before
         // loading!)
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
   static public void saveModel(final EObject rootElementOfModel, final String pathToXMIFile)
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
   static public void saveModelWithDependencies(final EObject rootElementOfModel, final String pathToXMIFile, final ResourceSet dependencies)
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
   static public void saveModel(final EObject rootElementOfModel, final URI uriToModelResource, final ResourceSet dependencies)
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
   static public URI createFileURI(final String pathToXMIFile, final boolean mustExist)
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
    * @return a list of all opposite objects
    */
   public static List<?> getOppositeReference(final EObject target, final Class<?> sourceType, final String targetRoleName)
   {
      Collection<Setting> settings = getInverseReferences(target);

      List<EObject> returnList = new ArrayList<EObject>();
      for (Setting setting : settings)
      {
         EObject candidate = getCandidateObject(sourceType, targetRoleName, setting);
         if (candidate != null)
            returnList.add(candidate);
      }

      EObject eContainer = target.eContainer();
      if (eContainer != null)
      {
         Setting setting = (((InternalEObject) eContainer).eSetting(target.eContainmentFeature()));
         EObject candidate = getCandidateObject(sourceType, targetRoleName, setting);
         if (candidate != null)
            returnList.add(candidate);
      }

      return returnList;
   }

   private static EObject getCandidateObject(final Class<?> sourceType, final String targetRoleName, final Setting setting)
   {
      if (setting.getEStructuralFeature().getName().equals(targetRoleName))
      {
         EClassifier clazz = setting.getEObject().eClass();
         String clazzName = getClazzNameWithPackagePrefix(clazz);

         if (clazzName.equals(sourceType.getName()) || checkInheritance(sourceType, clazz))
            return setting.getEObject();
      }

      return null;
   }

   private static Collection<Setting> getInverseReferences(final EObject target)
   {
      ECrossReferenceAdapter adapter = getCRAdapter(target);
      return adapter.getNonNavigableInverseReferences(target, true);
   }

   public static String getClazzNameWithPackagePrefix(final EClassifier clazz)
   {
      String clazzName = clazzNames.get(clazz);

      if (clazzName == null)
      {
         clazzName = clazz.getInstanceClass().getPackage().getName() + "." + clazz.getName();
         clazzNames.put(clazz, clazzName);
      }
      return clazzName;
   }

   public static boolean checkInheritance(final Class<?> superclass, final EClassifier subclass)
   {
      for (EClass sup : ((EClass) subclass).getEAllSuperTypes())
      {
         String clazzName = getClazzNameWithPackagePrefix(sup);
         if (clazzName.equals(superclass.getName()))
            return true;
      }
      return false;
   }

   private static ECrossReferenceAdapter getCRAdapter(final EObject target)
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

   public static void remove(final EObject object)
   {
      EcoreUtil.delete(object, true);
   }

   /*
    * This method is thought to be a more efficient way to delete objects from a model than remove(EObject)
    */
   public static void unsetAllReferences(final EObject object)
   {
      for (EStructuralFeature feature : getAllReferences(object))
      {
         object.eUnset(feature);
      }

      ArrayList<Setting> settings = new ArrayList<>();
      settings.addAll(getInverseReferences(object));
      for (Setting setting : settings)
      {
         EStructuralFeature feature = setting.getEStructuralFeature();
         removeOppositeReference(setting.getEObject(), object, feature.getName());
      }

      if (object.eContainer() != null)
      {
         removeOppositeReference(object.eContainer(), object, object.eContainmentFeature().getName());
      }

   }

   @SuppressWarnings({ "unchecked"})
   public static void addOppositeReference(final EObject source, final EObject target, final String targetRole)
   {
      EStructuralFeature reference = source.eClass().getEStructuralFeature(targetRole);
      if (!reference.isMany())
      {
         source.eSet(reference, target);
      } else
         ((Collection<EObject>) source.eGet(reference)).add(target);
   }

   public static void removeOppositeReference(final EObject source, final EObject target, final String targetRole)
   {
      removeEdge(source, target, targetRole);
   }
   
   public static void removeEdge(final EObject source, final EObject target, final String targetRole){
      EStructuralFeature reference = source.eClass().getEStructuralFeature(targetRole);
      if (!reference.isMany())
      {
         source.eSet(reference, null);
      } else
      {
         ((Collection<?>) source.eGet(reference)).remove(target);
      }      
   }

   public static EStructuralFeature getReference(final EObject obj, final String name)
   {
      for (EContentsEList.FeatureIterator<?> featureIterator = (EContentsEList.FeatureIterator<?>) obj.eCrossReferences().iterator(); featureIterator.hasNext();)
      {
         featureIterator.next();
         EReference eReference = (EReference) featureIterator.feature();
         if (eReference.getName().equals(name))
            return eReference;
      }

      return null;
   }

   public static EStructuralFeature getContainment(final EObject container, final String name)
   {
      for (EContentsEList.FeatureIterator<?> featureIterator = (EContentsEList.FeatureIterator<?>) container.eContents().iterator(); featureIterator.hasNext();)
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
   public static Set<EStructuralFeature> getAllReferences(final EObject object)
   {
      EList<EStructuralFeature> references = new BasicEList<EStructuralFeature>();
      for (EContentsEList.FeatureIterator<?> featureIterator = (EContentsEList.FeatureIterator<?>) object.eCrossReferences().iterator(); featureIterator
            .hasNext();)
      {
         featureIterator.next();
         references.add(featureIterator.feature());
      }
      for (EContentsEList.FeatureIterator<?> featureIterator = (EContentsEList.FeatureIterator<?>) object.eContents().iterator(); featureIterator.hasNext();)
      {
         featureIterator.next();
         references.add(featureIterator.feature());
      }
      return new HashSet<EStructuralFeature>(references);
   }

   public static String getName(final EObject child)
   {
      Object name = "";

      EStructuralFeature nameFeature = child.eClass().getEStructuralFeature("name");

      if (nameFeature != null)
         name = child.eGet(nameFeature);

      return (String) (name instanceof String && !((String) name).equals("") ? name : child.toString());
   }

   /**
    * Builds an identifier String for the given EObject. This identifier starts with
    * <ul>
    * <li>the attribute of the EObject as a String, if the EObject does only have one attribute.</li>
    * <li>the attribute called 'name' of the EObject, if it has such an attribute</li>
    * <li>any attribute of the EObject, but String attributes are preferred</li>
    * </ul>
    * The identifier ends with " : " followed by the type of the EObject. <br>
    * Example: A MocaTree Node with the name "foo" will result in "foo : Node" <br>
    * If the EObject does not have any attributes or all attributes have the value null, this function will only return
    * the type of the EObject.
    */
   public static String getIdentifier(final EObject eObject)
   {
      boolean success = false;
      List<EAttribute> attributes = eObject.eClass().getEAllAttributes();
      StringBuilder identifier = new StringBuilder();

      success = tryGetSingleAttribute(eObject, attributes, identifier);

      if (!success)
         success = tryGetNameAttribute(eObject, attributes, identifier);

      if (!success)
         success = tryGetAnyAttribute(eObject, attributes, identifier);

      if (success)
         identifier.append(" : ");

      identifier.append(eObject.eClass().getName());

      return identifier.toString();
   }

   /**
    * @param name
    *           Use an empty StringBuilder as input. If this function returns true, this parameter has been filled, if
    *           it returns false, nothing happened.
    * @return Indicates the success of this function and if the last parameter contains output.
    */
   private static boolean tryGetSingleAttribute(final EObject eObject, final List<EAttribute> attributes, final StringBuilder name)
   {
      boolean success = false;
      if (attributes.size() == 1)
      {
         Object obj = eObject.eGet(attributes.get(0));
         if (obj != null)
         {
            name.append(obj.toString());
            success = true;
         }
      }
      return success;
   }

   /**
    * @param name
    *           Use an empty StringBuilder as input. If this function returns true, this parameter has been filled, if
    *           it returns false, nothing happened.
    * @return Indicates the success of this function and if the last parameter contains output.
    */
   private static boolean tryGetNameAttribute(final EObject eObject, final List<EAttribute> attributes, final StringBuilder name)
   {
      boolean success = false;
      for (EAttribute feature : attributes)
      {
         if (feature.getName().equals("name"))
         {
            Object obj = eObject.eGet(feature);
            if (obj != null)
            {
               name.append(obj.toString());
               success = true;
               break;
            }
         }
      }
      return success;
   }

   /**
    * @param name
    *           Use an empty StringBuilder as input. If this function returns true, this parameter has been filled, if
    *           it returns false, nothing happened.
    * @return Indicates the success of this function and if the last parameter contains output.
    */
   private static boolean tryGetAnyAttribute(final EObject eObject, final List<EAttribute> attributes, final StringBuilder name)
   {
      boolean success = false;
      String nonStringName = null;
      String stringName = null;
      for (EAttribute feature : attributes)
      {
         Object obj = eObject.eGet(feature);
         if (obj == null)
            continue;
         if (obj instanceof String)
         {
            stringName = (String) obj;
            break;
         } else
         {
            nonStringName = obj.toString();
         }
      }
      if (stringName != null && !stringName.equals("null"))
      {
         name.append(stringName);
         success = true;
      } else if (nonStringName != null && !nonStringName.equals("null"))
      {
         name.append(nonStringName);
         success = true;
      }
      return success;
   }

   public static Resource addToResourceSet(final ResourceSet set, final EObject object)
   {
      Resource resource = object.eResource();
      if (resource == null)
      {
         resource = new ResourceImpl();
         resource.setURI(URI.createURI(object.eClass().getEPackage().getNsURI()));
      }

      resource.getContents().add(object);
      set.getResources().add(resource);

      return resource;
   }

}
