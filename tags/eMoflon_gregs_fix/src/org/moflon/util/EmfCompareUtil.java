package org.moflon.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.eclipse.emf.compare.diff.metamodel.DiffElement;
import org.eclipse.emf.compare.diff.metamodel.DiffGroup;
import org.eclipse.emf.compare.diff.metamodel.DiffModel;
import org.eclipse.emf.compare.diff.metamodel.DiffPackage;
import org.eclipse.emf.compare.diff.service.DiffService;
import org.eclipse.emf.compare.match.metamodel.MatchModel;
import org.eclipse.emf.compare.match.service.MatchService;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;

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
	 * Iterate over the given differences and remove all {@link DiffElement}s which are of the type or subtype as
	 * specified in <code>diffTypes</code>. If there are empty {@link DiffGroup}s left, they will be erased as well.
	 * 
	 * @param diff
	 *            Differences.
	 * @param diffTypes
	 *            The types which shall be removed.
 * @return 
	 * @return A list of all removed {@link DiffElement}s.
	 */
	public static List<DiffElement> removeDiffElementOfType(DiffModel diff, Set<EClass> diffTypes) {
		final List<DiffElement> removed = new ArrayList<DiffElement>();

		// don't ever delete diff groups explicitly
		if (diffTypes.contains(DiffPackage.Literals.DIFF_GROUP))
			throw new IllegalArgumentException("DiffGroups are not supported! This would erase all differences!");

		// collect and iterate over all elements to be removed
		final List<EObject> diffElementsToRemove = collectTypedElements(diff.getDifferences(), diffTypes, true);
		for (EObject obj : diffElementsToRemove) {
			EObject parent = obj.eContainer();

			// remove element and store in result!
			EcoreUtil.remove(obj);
			removed.add((DiffElement) obj);

			// if parent is empty group, remove it!
			while (parent instanceof DiffElement && ((DiffElement) parent).getSubDiffElements().isEmpty()) {
				final EObject newParent = parent.eContainer();
				EcoreUtil.remove(parent);
				parent = newParent;
			}
		}
		return removed;
	}
   
	/**
	 * Return all elements in a flat list which have the type given in <code>types</code>. The entire model tree is
	 * searched, i.e. it is a deep search.
	 * 
	 * @param elements
	 *            A set of elements.
	 * @param types
	 *            The types which should be returned.
	 * @param includeSubtypes
	 *            If <code>true</code>, then also subtypes of the given types are included in the result.
	 * @return A list of all elements which are of a type that is given in <code>types</code>.
	 */
	private static List<EObject> collectTypedElements(final List<? extends EObject> elements, final Set<EClass> types,
			boolean includeSubtypes) {
		final List<EObject> result = new ArrayList<EObject>();
		final Queue<EObject> queue = new LinkedList<EObject>();
		queue.addAll(elements);
		while (!queue.isEmpty()) {
			final EObject element = queue.poll();
			if (includeSubtypes) {
				for (EClass eClass : types) {
					if (eClass.isSuperTypeOf(element.eClass())) {
						result.add(element);
						break;
					}
				}
			} else {
				if (types.contains(element.eClass()))
					result.add(element);
			}
			queue.addAll(element.eContents());
		}
		return result;
	}
	
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
   public static List<DiffElement> compareAndFilter(EObject actual, EObject expected, boolean ignoreReferenceOrder) throws InterruptedException
   {
      DiffModel diff = createDiffModel(actual, expected, ignoreReferenceOrder);
      return diff.getDifferences();
   }
   
   
   /**
    * Compares actual to expected and returns an instance of DiffModel, filtered according to the filter flag.
    * 
    * @param actual An EObject.
    * @param expected The reference EObject to which "actual" gets compared to.
    * @param ignoreReferenceOrder Flag that controls whether changes to the order of references should be considered a difference. 
    * 
    * @return The DiffModel instance.
    * 
    * @throws InterruptedException
    */
   public static DiffModel createDiffModel(EObject actual, EObject expected, boolean ignoreReferenceOrder) throws InterruptedException
   {
      // Attempt to match elements that have only changed
      MatchModel match = MatchService.doMatch(actual, expected, Collections.<String, Object> emptyMap());
      // Use match to derive delta
      DiffModel diff = DiffService.doDiff(match, false);

      if (ignoreReferenceOrder)
         removeDiffElementOfType(diff, Collections.singleton(DiffPackage.Literals.REFERENCE_ORDER_CHANGE));
      
      return diff;
   }
}
