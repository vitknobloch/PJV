package cz.cvut.fel.pjv;

public class NodeImpl implements Node {
    
    private int value;
    private NodeImpl left;
    private NodeImpl right;
    
    public NodeImpl(){
        value =  0;
        left = null;
        right = null;
    }
    
    public void setSubTree(int[] values, int len, int startIndex){
        final int curIndex = len / 2;
        this.value = values[startIndex + curIndex];
        
        if(curIndex > 0){
            left = new NodeImpl();
            left.setSubTree(values, curIndex, startIndex);
        }
        
        if(len - 1 - curIndex > 0){
            right = new NodeImpl();
            right.setSubTree(values, len - 1 - curIndex,startIndex + curIndex + 1);
        }
    }

    public String printSubTree(int depth){
        String ret = "";
        for (int i = 0; i < depth; i++){
            ret += ' ';
        }
        ret += "- ";
        ret += Integer.toString(value);
        ret += "\n";
        if(left != null)
            ret += left.printSubTree(depth + 1);
        if(right != null){
            ret += right.printSubTree(depth + 1);
        }
        return ret;
    }
    
    @Override
    public Node getLeft() {
        return this.left;
    }

    @Override
    public Node getRight() {
        return this.right;
    }

    @Override
    public int getValue() {
        return value;
    }
}
