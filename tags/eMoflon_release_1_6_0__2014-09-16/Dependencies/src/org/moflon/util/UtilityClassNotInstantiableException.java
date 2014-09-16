package org.moflon.util;

public class UtilityClassNotInstantiableException extends RuntimeException
{
   private static final long serialVersionUID = -4283010951140137990L;

   public UtilityClassNotInstantiableException() {
      super("Invalid trial to instantiate a utility class.");
   }
}
