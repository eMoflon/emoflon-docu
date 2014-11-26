package org.moflon.util;

import org.eclipse.core.runtime.CoreException;

/**
 * This class wraps a {@link CoreException}, which is a checked exception that cannot be thrown in some contexts.
 */
public class UncheckedCoreException extends RuntimeException
{
   public UncheckedCoreException(final CoreException wrappedException) {
      super(wrappedException);
   }
   
   public CoreException getWrappedException() {
      return (CoreException)this.getCause();
   }
   

   private static final long serialVersionUID = 424344754161787168L;

}
