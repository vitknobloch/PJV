package cz.cvut.fel.pjv;

public class Start {
    public static void main(String[] args) {
        for(int i = 1; i < 11; i++){
            TreeImpl tree = new TreeImpl();
            int[] values = new int[i];
            for(int j = 0; j < i; j++){
                values[j] = j + 1;
            }
            tree.setTree(values);
            System.out.println(tree.toString());
        }

        TreeImpl tree = new TreeImpl();
        tree.setTree(null);
        System.out.println("Empty tree: '" + tree.toString() + "'");
    }
}
