package cz.cvut.fel.pjv;

/**
 * Implementation of the {@link Queue} backed by fixed size array.
 */
public class CircularArrayQueue implements Queue {

    private int capacity;
    private String[] values;
    private int len;
    private int head;

    /**
     * Creates the queue with capacity set to the value of 5.
     */
    public CircularArrayQueue() {
        this(5);
    }


    /**
     * Creates the queue with given {@code capacity}. The capacity represents maximal number of elements that the
     * queue is able to store.
     * @param capacity of the queue
     */
    public CircularArrayQueue(int capacity) {
        this.capacity = capacity;
        values = new String[capacity];
        len = 0;
        head = 0;
    }

    @Override
    public int size() {
        return len;
    }

    @Override
    public boolean isEmpty() {
        return len == 0;
    }

    @Override
    public boolean isFull() {
        return len == capacity;
    }

    @Override
    public boolean enqueue(String obj) {
        if((obj == null) || isFull())
            return false;

        values[getArrayIndex(len++)] = obj;
        return true;
    }

    /**
     * Returns the value {@code array} index of {@code queueIndex}-th element of the queue
     * @param queueIndex the position of the element in queue
     * @return the index of the element in {@code values[]}
     */
    private int getArrayIndex(int queueIndex){
        return (head + queueIndex) % capacity;
    }

    @Override
    public String dequeue() {
        if(isEmpty())
            return null;
        String ret = values[head];
        head = getArrayIndex(1);
        len--;
        return ret;
    }

    @Override
    public void printAllElements() {
        for(int i = 0; i < len; i++){
            System.out.println(values[getArrayIndex(i)]);
        }
    }
}
