package edu.osu.slate.relatedness.swwr.data.category;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

import edu.osu.slate.relatedness.swwr.data.category.CategoryGraph;

/**
 * Breadth/Depth-first search nodes.
 * <p>
 * Used in the {@link CategoryGraph} class.
 * 
 * @author weale
 *
 */
public class VisitNode
{

  /* Array of category IDs of the parents of the node */
  private int[] parentIndices;

  /* Current category index */
  private int currIndex;

  /* Current child position */
  private int currChild;
  
  private int depth;

  /**
   * Creates a new {@link VisitNode} with given parents and the current {@link CategoryTitleNode}.
   * 
   * @param cn Current {@link CategoryNode} index
   * @param p indices of the parents of the current category
   */
  public VisitNode(int cn, int[] p, int d)
  {
    currIndex = cn;
    parentIndices = p;
    currChild = 0;
    depth = d;
  }

 /**
  * Returns the ID of the current category.
  * <p>
  * Uses the {@link CategoryNode} class.
  * 
  * @return ID of the current category.
  */
  public int getCurrentCategoryIndex()
  {
    return currIndex;
  }

 /**
  * Determines if there is another child to visit.
  * 
  * @param arr Array of {@link CategoryNode} objects.
  * @return Boolean determination of whether there's more children to visit.
  */
  public boolean hasNextChild(CategoryNode[] graph)
  {
    if(graph[currIndex].getChildrenCategories() == null)
    {
      return false;
    }
    else
    {
      return (currChild < graph[currIndex].getChildrenCategories().length);
    }
  }//end: hasNextChild(CategoryNode[])

 /**
  * Constructs the children of the current VisitNode for further search.
  * <p>
  * If a child is found to have already been visited, the edge is removed from
  * the graph, as a cycle has been detected.
  * 
  * @return Array of children VisitNodes
  */
  public VisitNode getNextChild(CategoryNode[] graph)
  {
    /* Get all children in of the current node */
    CategoryNode tmp = graph[currIndex].getChildrenCategories()[currChild];
    currChild++;

    // Get the next child
    int childID = tmp.getCategoryID();

    // Check if a cycle would be created by expanding this child
    boolean validChild = true;
    for(int j = 0; parentIndices != null && j < parentIndices.length; j++)
    {
      /* If we've already visited the child, we have a cycle */
      if(graph[parentIndices[j]].getCategoryID() == childID)
      {
        validChild = false; // Cycle detected!
      }
    }//end: for(j)

    // Child not found in parent list, therefore eligible for expansion.
    if(validChild)
    {
      // Add current node to the child's parent list
      int[] newParents = new int[parentIndices.length+1];
      System.arraycopy(parentIndices, 0, newParents, 0, parentIndices.length);
      newParents[parentIndices.length] = currIndex;

      Arrays.sort(newParents);
      
      // Add the new VisitNode to the LinkedList
      return new VisitNode(Arrays.binarySearch(graph, tmp), newParents, depth+1);
    }
    else
    {
      // Current edge causes a cycle. Remove it.
      graph[currIndex].removeChild(childID);
      return null;
    }

  }//end: makeChildrenVisitNodes()

  /**
   * Constructs the children of the current VisitNode for further search.
   * <p>
   * If a child is found to have already been visited, the edge is removed from
   * the graph, as a cycle has been detected.
   * 
   * @return Array of children VisitNodes
   */
  public VisitNode[] makeChildrenVisitNodes(CategoryNode[] graph)
  {
    /* Get all children of the current node */
    CategoryNode[] children = graph[currIndex].getChildrenCategories();
    LinkedList<VisitNode> ll = new LinkedList<VisitNode>();

    for(int i = 0; children != null && i < children.length; i++)
    {
      // Get the next child
      //int childID = children[i].getCategoryID();
      System.out.println(this.depth + "\t" + children[i].getCategoryID());
      
      int childIndex = Arrays.binarySearch(graph, children[i]);

      // Check if a cycle would be created by expanding this child
      boolean validChild = (Arrays.binarySearch(parentIndices, childIndex) < 0);
      
//      for(int j = 0; parents != null && j < parents.length; j++)
//      {
//        /* If we've already visited the child, we have a cycle */
//        if(arr[parents[j]].getCategoryID() == childID )
//        {
//          validChild = false; // Cycle detected!
//        }
//      }//end: for(j)

      // Child not found in parent list, therefore eligible for expansion.
      if(validChild)
      {
        // Add current node to the child's parent list
        int[] newParents = new int[parentIndices.length+1];
        System.arraycopy(parentIndices, 0, newParents, 0, parentIndices.length);
        newParents[parentIndices.length] = currIndex;

        // Sort the array of parents
        Arrays.sort(newParents);
        
        // Add the new VisitNode to the LinkedList
        ll.add(new VisitNode(Arrays.binarySearch(graph, children[i]), newParents, depth+1));
      }
      else
      {
        // Current edge causes a cycle. Remove it.
        graph[currIndex].removeChild(children[i].getCategoryID());
      }
    }//end: for(i)

    // Convert the LinkedList into a more manageable array.
    VisitNode[] vna = new VisitNode[ll.size()];
    Iterator<VisitNode> it = ll.iterator();
    for(int i = 0; i < vna.length; i++)
    {
      vna[i] = it.next();
    }
    ll.clear();

    return vna;
  }//end: makeChildrenVisitNodes()
}