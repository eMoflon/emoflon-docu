package org.moflon.util.plugins;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.codegen.ecore.genmodel.GenModel;
import org.eclipse.emf.codegen.ecore.genmodel.GenPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.moflon.util.WorkspaceHelper;
import org.moflon.util.eMoflonEMFUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class updates plugin.xml, e.g., with information from the genmodel.
 */
public class PluginXmlUpdater
{
   private static class GeneratedPackageEntry
   {
      private String uri;

      private String className;

      private String genmodelFile;

      public GeneratedPackageEntry(final String uri, final String className, final String genmodelFile)
      {
         this.uri = uri;
         this.className = className;
         this.genmodelFile = genmodelFile;
      }

   }

   // TODO@rkluge: Set correctly when class has moved
   private static final String PLUGIN_ID = "org.moflon.ide.core";

   /**
    * Minimal content of a plugin.xml file
    */
   public static final String DEFAULT_PLUGIN_XML_CONTENT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<?eclipse version=\"3.0\"?>\n<plugin>\n</plugin>";

   /**
    * Updates plugin.xml from the information in the given project. The genmodel is fetched from the default path.
    * 
    * @see WorkspaceHelper#getProjectGenmodelFile(IProject)
    */
   public void updatePluginXml(final IProject currentProject, final IProgressMonitor monitor) throws CoreException
   {
      monitor.beginTask("Create/update plugin.xml", 1);

      IFile projectGenModelFile = WorkspaceHelper.getProjectGenmodelFile(currentProject);
      String pathToGenmodel = projectGenModelFile.getRawLocation().toOSString();
      GenModel genmodel = (GenModel) eMoflonEMFUtil.loadAndInitModel(EcorePackage.eINSTANCE, pathToGenmodel);

      updatePluginXml(currentProject, genmodel, WorkspaceHelper.createSubmonitorWith1Tick(monitor));

      monitor.worked(1);
      monitor.done();

   }

   /**
    * Updates plugin.xml from the information in the given project and the given genmodel.
    */
   public void updatePluginXml(final IProject project, final GenModel genmodel, final IProgressMonitor monitor) throws CoreException
   {
      try
      {
         monitor.beginTask("Updating plugin.xml from Genmodel", 2);
         final String content = readOrGetDefaultPluginXmlContent(project);
         final Document doc = parseXmlModel(content);
         monitor.worked(1);

         removeExtensionPointsForGeneratedPackages(doc);

         final List<Element> extensionElements = createListOfGeneratedPackageExtensions(doc, project, genmodel);
         final Node pluginRootElement = doc.getElementsByTagName("plugin").item(0);
         extensionElements.forEach(element -> pluginRootElement.appendChild(element));

         String output = formatXmlString(doc, monitor);

         writeContentToFile(output, getPluginXml(project), WorkspaceHelper.createSubmonitorWith1Tick(monitor));

      } catch (ParserConfigurationException | SAXException | XPathExpressionException e)
      {
         throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID, "Error parsing plugin.xml for project " + project.getName() + ": " + e.getMessage(), e));
      } catch (IOException | TransformerFactoryConfigurationError | TransformerException e)
      {
         throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID, "Error reading/writing plugin.xml for project " + project.getName() + ": "
               + e.getMessage(), e));
      } finally
      {
         monitor.worked(1);
         monitor.done();
      }
   }

   private void removeExtensionPointsForGeneratedPackages(final Document doc) throws XPathExpressionException
   {
      NodeList extensionPoints = PluginXmlUpdater.getGeneratedPackageExtensionPoints(doc);
      for (int n = 0; n < extensionPoints.getLength(); ++n)
      {
         extensionPoints.item(n).getParentNode().removeChild(extensionPoints.item(n));
      }
   }

   private Document parseXmlModel(final String content) throws ParserConfigurationException, SAXException, IOException
   {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.parse(new ByteArrayInputStream(content.getBytes()));
      return doc;
   }

   private String readOrGetDefaultPluginXmlContent(final IProject project) throws IOException, CoreException
   {
      IFile pluginXmlFile = getPluginXml(project);
      String content = "";
      if (pluginXmlFile.exists())
      {
         content = IOUtils.toString(pluginXmlFile.getContents());
      } else
      {
         content = DEFAULT_PLUGIN_XML_CONTENT;
      }
      return content;
   }

   private List<Element> createListOfGeneratedPackageExtensions(final Document doc, final IProject project, final GenModel genmodel)
   {
      final List<GeneratedPackageEntry> entries = extractGeneratedPackageEntries(project, genmodel);
      final List<Element> extensionElements = new ArrayList<>();

      entries.forEach(entry -> {
         Element extensionElement = doc.createElement("extension");
         extensionElement.setAttribute("point", "org.eclipse.emf.ecore.generated_package");
         Element packageElement = doc.createElement("package");
         packageElement.setAttribute("uri", entry.uri);
         packageElement.setAttribute("class", entry.className);
         packageElement.setAttribute("genModel", entry.genmodelFile);
         extensionElement.appendChild(packageElement);
         extensionElements.add(extensionElement);
      });
      return extensionElements;
   }

   private List<GeneratedPackageEntry> extractGeneratedPackageEntries(final IProject project, final GenModel genmodel)
   {
      String genmodelFile = WorkspaceHelper.getProjectGenmodelFile(project).getProjectRelativePath().toString();
      final List<GeneratedPackageEntry> entries = new ArrayList<>();
      final List<GenPackage> ePackages = genmodel.getAllGenPackagesWithClassifiers();
      for (final GenPackage genPackage : ePackages)
      {
         final String fullyQualifiedPackageClassName = genPackage.getInterfacePackageName() + "." + genPackage.getPackageInterfaceName();

         entries.add(new GeneratedPackageEntry(genPackage.getNSURI(), fullyQualifiedPackageClassName, genmodelFile));
      }
      return entries;
   }

   private String formatXmlString(final Document doc, final IProgressMonitor monitor) throws TransformerConfigurationException,
         TransformerFactoryConfigurationError, TransformerException
   {
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      StreamResult result = new StreamResult(new StringWriter());
      DOMSource source = new DOMSource(doc);
      transformer.transform(source, result);
      String output = result.getWriter().toString();

      monitor.worked(1);
      return output;
   }

   private void writeContentToFile(final String content, final IFile file, final IProgressMonitor monitor) throws CoreException
   {
      if (!file.exists())
      {
         file.create(new ByteArrayInputStream(content.getBytes()), true, WorkspaceHelper.createSubmonitorWith1Tick(monitor));
      } else
      {
         file.setContents(new ByteArrayInputStream(content.getBytes()), true, true, WorkspaceHelper.createSubmonitorWith1Tick(monitor));
      }
   }

   private IFile getPluginXml(final IProject currentProject)
   {
      return currentProject.getFile("plugin.xml");
   }

   private static NodeList getGeneratedPackageExtensionPoints(final Document doc) throws XPathExpressionException
   {
      XPathFactory xPathfactory = XPathFactory.newInstance();
      XPath xpath = xPathfactory.newXPath();
      XPathExpression expr = xpath.compile("/plugin/extension[@point='org.eclipse.emf.ecore.generated_package']");
      NodeList extensionPoints = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
      return extensionPoints;
   }

}
