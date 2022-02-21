package cz.cvut.fel.pjv;

public class TreeImpl implements Tree{

    private NodeImpl root;

    public TreeImpl(){
        root = null;
    }

    @Override
    public void setTree(int[] values) {
        if (values != null && values.length > 0) {
            root = new NodeImpl();
            root.setSubTree(values, values.length, 0);
        }
    }

    @Override
    public Node getRoot() {
        return root;
    }

    @Override
    public String toString(){
        if(root == null){
            return "";
        }
        return root.printSubTree(0);
    }
}
