package org.moflon.util;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import org.eclipse.emf.compare.diff.metamodel.AttributeChange;
import org.eclipse.emf.compare.diff.metamodel.DiffElement;
import org.eclipse.emf.compare.diff.metamodel.DiffModel;
import org.eclipse.emf.compare.diff.metamodel.DifferenceKind;
import org.eclipse.emf.compare.diff.metamodel.ReferenceChange;
import org.eclipse.emf.compare.diff.service.DiffService;
import org.eclipse.emf.compare.match.metamodel.MatchModel;
import org.eclipse.emf.compare.match.service.MatchService;
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
   // Flags to control comparison
   public static boolean ignoreMove;

   public static boolean ignoreAdd;

   public static boolean ignoreDelete;

   public static boolean ignoreChange;

   public static boolean ignoreAttribute;

   public static boolean ignoreReference;

   /**
    * Compares actual to expected and returns a list of differences, filtered according to the filter flags.
    * 
    * @param actual
    * @param expected
    * @return List of filtered differences
    * @throws IOException
    * @throws InterruptedException
    */
   public static List<DiffElement> compareAndFilter(EObject actual, EObject expected) throws IOException, InterruptedException
   {
      Vector<DiffElement> filteredDifferences = new Vector<DiffElement>();

      // Attempt to match elements that have only changed
      MatchModel match = MatchService.doMatch(actual, expected, Collections.<String, Object> emptyMap());
      // Use match to derive delta
      DiffModel diff = DiffService.doDiff(match, false);

      // Filter according to flags
      for (DiffElement difference : diff.getDifferences())
         if (includeInResult(difference))
            filteredDifferences.addElement(difference);

      return filteredDifferences;
   }

   
   private static boolean includeInResult(DiffElement element)
   {
      if (ignoreMove && element.getKind().equals(DifferenceKind.MOVE))
         return false;
      
      if (ignoreAdd && element.getKind().equals(DifferenceKind.ADDITION))
         return false;

      if (ignoreDelete && element.getKind().equals(DifferenceKind.DELETION))
         return false;

      if (ignoreChange && element.getKind().equals(DifferenceKind.CHANGE))
         return false;

      if (ignoreAttribute && element instanceof AttributeChange)
         return false;

      if (ignoreReference && element instanceof ReferenceChange)
         return false;

      return true;
   }
}
