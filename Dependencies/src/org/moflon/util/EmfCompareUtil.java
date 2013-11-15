package org.moflon.util;

import java.io.IOException;
import java.util.List;

import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.Diff;
import org.eclipse.emf.compare.EMFCompare;
import org.eclipse.emf.compare.scope.IComparisonScope;
import org.eclipse.emf.ecore.EObject;

/**
 * Collection of useful methods for EMFCompare
 *
 * @author anjorin
 * @author (last editor) $Author$
 * @version $Revision$ $Date$
 */
public class EmfCompareUtil
{
   	
   /**
    * Compares actual to expected and returns a list of differences, filtered according to the filter flags.
    * 
    * @param actual
    * @param expected
    * 
    * @return <b>Unmodifiable</b> list of filtered differences.
    * 
    * @throws IOException
    * @throws InterruptedException
    */
   public static List<Diff> compareAndFilter(EObject actual, EObject expected, boolean ignoreReferenceOrder) throws InterruptedException
   {
	   IComparisonScope scope = EMFCompare.createDefaultScope(actual, expected);
	   Comparison comparison = EMFCompare.builder().build().compare(scope);
	 
	   return comparison.getDifferences();
   }
}
