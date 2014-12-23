package org.moflon.util.plugins;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.jar.Attributes.Name;

/**
 * This class mimics the behavior of {@link Manifest#write}, which fails to close its output stream.
 */
public class ManifestWriter
{

   /**
    * Copied from {@link Manifest#write(OutputStream)}
    * 
    * Bugfix: Output stream is closed properly
    */
   public void write(final Manifest manifest, final OutputStream out) throws IOException
   {
      DataOutputStream dos = new DataOutputStream(out);
      // Write out the main attributes for the manifest
      writeMain(manifest.getMainAttributes(), dos);
      // Now write out the pre-entry attributes
      Iterator<Map.Entry<String, Attributes>> it = manifest.getEntries().entrySet().iterator();
      while (it.hasNext())
      {
         Map.Entry<String, Attributes> e = it.next();
         StringBuffer buffer = new StringBuffer("Name: ");
         String value = e.getKey();
         if (value != null)
         {
            byte[] vb = value.getBytes("UTF8");
            value = new String(vb, 0, 0, vb.length);
         }
         buffer.append(value);
         buffer.append("\r\n");
         make72Safe(buffer);
         dos.writeBytes(buffer.toString());
         write(e.getValue(), dos);
      }
      dos.flush();
      dos.close();
   }

   /**
    * Copy of {@link Manifest#make72Safe(StringBuffer)}
    */
   private static void make72Safe(final StringBuffer line)
   {
      int length = line.length();
      if (length > 72)
      {
         int index = 70;
         while (index < length - 2)
         {
            line.insert(index, "\r\n ");
            index += 72;
            length += 3;
         }
      }
      return;
   }

   /**
    * Copy of {@link Attributes#writeMain}
    */
   private void writeMain(final Attributes attributes, final DataOutputStream out) throws IOException
   {
      // write out the *-Version header first, if it exists
      String vername = Name.MANIFEST_VERSION.toString();
      String version = attributes.getValue(vername);
      if (version == null)
      {
         vername = Name.SIGNATURE_VERSION.toString();
         version = attributes.getValue(vername);
      }

      if (version != null)
      {
         out.writeBytes(vername + ": " + version + "\r\n");
      }

      // write out all attributes except for the version
      // we wrote out earlier
      Iterator<Map.Entry<Object, Object>> it = attributes.entrySet().iterator();
      while (it.hasNext())
      {
         Map.Entry<Object, Object> e = it.next();
         String name = ((Name) e.getKey()).toString();
         if ((version != null) && !(name.equalsIgnoreCase(vername)))
         {

            StringBuffer buffer = new StringBuffer(name);
            buffer.append(": ");

            String value = (String) e.getValue();
            if (value != null)
            {
               byte[] vb = value.getBytes("UTF8");
               value = new String(vb, 0, 0, vb.length);
            }
            buffer.append(value);

            buffer.append("\r\n");
            make72Safe(buffer);
            out.writeBytes(buffer.toString());
         }
      }
      out.writeBytes("\r\n");
   }

   private void write(final Attributes attributes, final DataOutputStream os) throws IOException
   {
      Iterator<Map.Entry<Object, Object>> it = attributes.entrySet().iterator();
      while (it.hasNext())
      {
         Map.Entry<Object, Object> e = it.next();
         StringBuffer buffer = new StringBuffer(((Name) e.getKey()).toString());
         buffer.append(": ");

         String value = (String) e.getValue();
         if (value != null)
         {
            byte[] vb = value.getBytes("UTF8");
            value = new String(vb, 0, 0, vb.length);
         }
         buffer.append(value);

         buffer.append("\r\n");
         make72Safe(buffer);
         os.writeBytes(buffer.toString());
      }
      os.writeBytes("\r\n");
   }
}