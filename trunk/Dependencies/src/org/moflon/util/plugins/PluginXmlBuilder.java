package org.moflon.util.plugins;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
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
import org.moflon.util.MoflonUtil;
import org.moflon.util.WorkspaceHelper;
import org.moflon.util.eMoflonEMFUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class PluginXmlBuilder
{

   // TODO@rkluge: Set correctly when class has moved
   private static final String PLUGIN_ID = "org.moflon.ide.core";

   /**
    * Minimal content of a plugin.xml file
    */
   public static final String DEFAULT_PLUGIN_XML_CONTENT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<?eclipse version=\"3.0\"?><plugin></plugin>";

   public void updatePluginXml(final IProject currentProject, final IProgressMonitor monitor) throws CoreException
   {
      monitor.beginTask("Create/update plugin.xml", 1);

      IFile projectGenModelFile = WorkspaceHelper.getProjectGenmodelFile(currentProject);
      String pathToGenmodel = projectGenModelFile.getRawLocation().toOSString();
      GenModel genmodel = (GenModel) eMoflonEMFUtil.loadAndInitModel(EcorePackage.eINSTANCE, pathToGenmodel);

      updateGeneratedPackagesInPluginXml(currentProject, WorkspaceHelper.createSubmonitorWith1Tick(monitor), genmodel);

      monitor.worked(1);
      monitor.done();

   }

   public void updateGeneratedPackagesInPluginXml(final IProject currentProject, final IProgressMonitor monitor, final GenModel genmodel) throws CoreException
   {
      try
      {
         monitor.beginTask("Updating plugin.xml from Genmodel", 2);
         IFile pluginXmlFile = getPluginXml(currentProject);
         String content = "";
         if (pluginXmlFile.exists())
         {
            content = IOUtils.toString(pluginXmlFile.getContents());
         } else
         {
            content = DEFAULT_PLUGIN_XML_CONTENT;
         }
         monitor.worked(1);

         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         DocumentBuilder builder = factory.newDocumentBuilder();
         Document doc = builder.parse(new ByteArrayInputStream(content.getBytes()));

         NodeList extensionPoints = PluginXmlBuilder.getGeneratedPackageExtensionPoints(doc);
         for (int n = 0; n < extensionPoints.getLength(); ++n)
         {
            Node extensionElement = extensionPoints.item(n);
            extensionElement.getParentNode().removeChild(extensionElement);
         }

         Node pluginElement = doc.getElementsByTagName("plugin").item(0);

         final List<GenPackage> ePackages = genmodel.getAllGenPackagesWithClassifiers();
         for (final GenPackage genPackage : ePackages)
         {
            String fullyQualifiedPackage = genPackage.getInterfacePackageName();
            String packageClassName = genPackage.getPackageInterfaceName();
            String fullyQualifiedPackageClassName = fullyQualifiedPackage + "." + packageClassName;

            Element extensionElement = doc.createElement("extension");
            extensionElement.setAttribute("point", "org.eclipse.emf.ecore.generated_package");
            Element packageElement = doc.createElement("package");
            packageElement.setAttribute("uri", genPackage.getNSURI());
            packageElement.setAttribute("class", fullyQualifiedPackageClassName);
            packageElement.setAttribute("genModel", WorkspaceHelper.getProjectGenmodelFile(currentProject).getProjectRelativePath().toString());
            extensionElement.appendChild(packageElement);
            pluginElement.appendChild(extensionElement);
         }

         String output = formatXmlString(doc, monitor);

         writeContentToFile(output, pluginXmlFile, WorkspaceHelper.createSubmonitorWith1Tick(monitor));

      } catch (ParserConfigurationException | SAXException | XPathExpressionException e)
      {
         throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID, "Error parsing plugin.xml for project " + currentProject.getName() + ": "
               + e.getMessage(), e));
      } catch (IOException | TransformerFactoryConfigurationError | TransformerException e)
      {
         throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID, "Error reading/writing plugin.xml for project " + currentProject.getName() + ": "
               + e.getMessage(), e));
      } finally
      {
         monitor.worked(1);
         monitor.done();
      }
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

   public IFile getPluginXml(final IProject currentProject)
   {
      return currentProject.getFile("plugin.xml");
   }

   public void dropGeneratedPackageExtensionPoints(final IProject project) throws CoreException
   {
      IFile file = getPluginXml(project);
      try
      {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         DocumentBuilder builder = factory.newDocumentBuilder();
         Document doc = builder.parse(file.getContents());

         // Element pluginElement = (Element) doc.getChildNodes().item(0);

         NodeList extensionPoints = PluginXmlBuilder.getGeneratedPackageExtensionPoints(doc);
         for (int n = 0; n < extensionPoints.getLength(); ++n)
         {
            Node extensionElement = extensionPoints.item(n);
            extensionElement.getParentNode().removeChild(extensionElement);
         }

         // writeContentToFile(output, pluginXmlFile, monitor);
      } catch (IOException | SAXException | ParserConfigurationException | XPathExpressionException e)
      {
         throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID, "Error removing generated packages from plugin.xml for project " + project.getName()
               + ": " + e.getMessage(), e));
      }

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
