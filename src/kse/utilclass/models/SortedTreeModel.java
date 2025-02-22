package kse.utilclass.models;

/*
*  File: SortedTreeModel.java
* 
*  Project JUtilClasses
*  @author Wolfgang Keller
*  Created 
* 
*  Copyright (c) 2022 by Wolfgang Keller, Munich, Germany
* 
This program is not public domain software but copyright protected to the 
author(s) stated above. However, you can use, redistribute and/or modify it 
under the terms of the GNU Library or Lesser General Public License as 
published by the Free Software Foundation, version 3.0 of the License.

This program is distributed in the hope that it will be useful, but WITHOUT
ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the License along with this program; if not,
write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, 
Boston, MA 02111-1307, USA, or go to http://www.gnu.org/copyleft/gpl.html.
*/

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import javax.swing.JFrame;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import kse.utilclass.sets.SortedArraySet;

/**
 * A {@code TreeModel} which sorts the elements of its nodes along the natural
 * sorting of the supplied user objects. User objects are compared with their
 * {@code toString()} results. If instances of this model shall be
 * serialised, user objects have to comply to the {@code java.io.Serializable}
 * interface.
 * 
 * <p>The {@code SortedTreeModel} does not serialise its listeners 
 * (TreeModelListener).
 * 
 */
public class SortedTreeModel implements TreeModel, java.io.Serializable {
	
    private static final long serialVersionUID = -517560356477421857L;

	transient LinkedList<TreeModelListener> listeners = new LinkedList<>();
	Hashtable<TreePath, TNode> hmap = new Hashtable<>(16);
	TNode root;
	
	public static class TNode extends SortedArraySet<TNode> implements Comparable<TNode> {
	    private static final long serialVersionUID = -547907442991784191L;
		private TNode parent;
		private Object object;
		
		/** Creates a new {@code TNode} without a parent node, holding the given
		 * user-object. Defines this node as a root node. 
		 * 
		 * @param userObject Object
		 */
		public TNode (Object userObject) {
			Objects.requireNonNull(userObject);
			object = userObject;
		}

		/** Creates a new {@code TNode} under a parent node, holding the given
		 * user-object. parent == null defines this node as a root node.
		 * 
		 * @param parent {@code TNode} parent node, may be null
		 * @param userObject Object
		 */
		public TNode (TNode parent, Object userObject) {
			this(userObject);
			setParent(parent);
		}

		public Object getUserObject() {
			return object;
		}

		public void setUserObject(Object object) {
			this.object = object;
		}

		public TNode getParent() {
			return parent;
		}

		public void setParent (TNode parent) {
			this.parent = parent;
		}

		public boolean isLeaf () {
			return isEmpty();
		}
		
		/** Returns the {@code TreePath} identifying this node. This 
		 * expression returns at least one element.
		 * 
		 * @return {@code TreePath}
		 */
		protected TreePath getPath () {
			ArrayList<TNode> list = new ArrayList<>();
			TNode n = this;
			while (n != null) {
				list.add(0, n);
				n = n.getParent();
			}
			return new TreePath(list.toArray());
		}

		/** Returns the textual representation of this node in the tree display.
		 * 
		 *  @return String 
		 */
		@Override
		public String toString() {
			return object.toString();
		}

		@Override
		public int compareTo (TNode node) {
			Objects.requireNonNull(node);
			
			// priority sorting for containers
			int c;
			if (isLeaf() == node.isLeaf()) {
				c = object.toString().compareTo(node.object.toString());
			} else {
				c = isLeaf() ? 1 : -1;
			}
			return c;
		}
	}
	
	public SortedTreeModel () {
	}

	/** Tests whether the given object is an instance of {@code TNode} and
	 * returns the same object as the node type.
	 * 
	 * @param o Object candidate object
	 * @return {@code TNode} object
	 * @throws IllegalArgumentException if object is not a {@code TNode}
	 */
	private TNode requireTNode (Object o) {
		if (!(o instanceof TNode))
			throw new IllegalArgumentException("illegal type: \"SortedTreeModel.TNode\" required");
		return (TNode) o;
	}
	
	@Override
	public TNode getRoot () {
		return root;
	}

	@Override
	public TNode getChild (Object parent, int index) {
		TNode p = requireTNode(parent);
		if (index < 0 | p.size() <= index)
			return null;
		return p.getElement(index);
	}

	@Override
	public int getChildCount (Object parent) {
		TNode p = requireTNode(parent);
		return p.size();
	}

	@Override
	public boolean isLeaf (Object node) {
		return requireTNode(node).isLeaf();
	}

	@Override
	public void valueForPathChanged (TreePath path, Object newValue) {
		// identify node
		TNode n = hmap.get(path);
		
		// if node exists and the user object has changed
		if (n != null && n.getUserObject() != newValue) {
			n.setUserObject(newValue);
			fireTreeNodesChanged(n.getParent(), new TNode[] {n});
		}
	}

	@Override
	public int getIndexOfChild (Object parent, Object child) {
		if (parent == null || !(parent instanceof TNode) ||
			child == null || !(child instanceof TNode)) {
			return -1;
		}
		return ((TNode)parent).indexOf(child);
	}

	@Override
	public void addTreeModelListener (TreeModelListener l) {
		synchronized(listeners) {
			if (!listeners.contains(l)) {
				listeners.add(l);
			}
		}
	}

	@Override
	public void removeTreeModelListener (TreeModelListener l) {
		synchronized(listeners) {
			listeners.remove(l);
		}
	}
	
	public void insertObject (Object parent, Object userObject) {
		TNode p = parent == null ? null : requireTNode(parent);
		boolean rootSetup = p == null;
		
		// verify parent node
		if (!rootSetup && !hmap.containsKey(p.getPath())) {
			throw new IllegalArgumentException("parent node unknown to model: " + p);
		}
		
		// create new node with user object
		TNode node = new TNode(p, userObject);
		
		// insert a root
		if (rootSetup) {
			root = node;
			hmap.clear();

		// insert node into parent
		} else {
			p.add(node);
		}

		// update node map
		hmap.put(node.getPath(), node);
		
		// inform listeners
		TreeModelEvent evt;
		if (rootSetup) {
			evt = new TreeModelEvent(this, node.getPath());
			for (TreeModelListener li : listenerCopy()) {
				li.treeStructureChanged(evt);
			}
			
		} else {
			int index = p.indexOf(node);
			evt = new TreeModelEvent(this, p.getPath(), new int[] {index}, new TNode[] {node});
			for (TreeModelListener li : listenerCopy()) {
				li.treeNodesInserted(evt);
			}
		}
	}
	
	/** Returns a shallow clone of the model listener list.
	 *   
	 * @return {@code List<TreeModelListener>}
	 */
	@SuppressWarnings("unchecked")
	protected final List<TreeModelListener> listenerCopy () {
		synchronized(listeners) {
			return (List<TreeModelListener>) listeners.clone();
		}
	}
	
	/** Fires a TreeModelEvent for "nodes-changed". This should take place
	 * when user objects under the given nodes have changed (display related) 
	 * content.
	 * <p>{@code parent} == null indicates the root node changed; in this case 
	 * {@code children} is irrelevant. 
	 *  
	 * @param parent {@code TNode} parent of modified nodes
	 * @param children {@code TNode[]} child objects modified
	 */
	@SuppressWarnings("unchecked")
	protected void fireTreeNodesChanged (TNode parent, TNode[] children) {
		TreeModelEvent evt;
		
		// the root node changed
		if (parent == null) {
			evt = new TreeModelEvent(this, new TreePath(new Object[]{}), null, null);

		// other nodes changed
		} else {
			int[] ic = new int[children.length];
			for (int i = 0; i < ic.length; i++) {
				ic[i] = getIndexOfChild(parent, children[i]);
				if (ic[i] == -1) {
					throw new IllegalStateException("child node invalid: " + children[i]);
				}
			}
			evt = new TreeModelEvent(this, parent.getPath(), ic, children);
		}
		
		// distribute tree-change event
		for (TreeModelListener li : listenerCopy()) {
			li.treeNodesChanged(evt);
		}
	}
    
	public static void main (String[] args) {
		SortedTreeModel model = new SortedTreeModel();
		JTree jTree = new JTree(model);
		
		JFrame frame = new JFrame("SortedTreeModel TEST");
		frame.setPreferredSize(new Dimension(400, 400));
		frame.add(jTree);
		frame.pack();
		frame.setVisible(true);
		
		// insert a root node
		model.insertObject(null, "TEST-BAUM (root)");
		jTree.expandPath(model.getRoot().getPath());
		
		model.insertObject(model.getRoot(), "BBB Eintrag");
		model.insertObject(model.getRoot(), "AAA Eintrag");
	}
}
