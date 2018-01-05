package kvverti.enim.abiescript;

import java.util.Arrays;

/** Stack for primitive doubles. I promise this time. */
class Stack {

    private double[] data;
    private int size;

    public Stack(int cap) {

        data = new double[cap];
    }

    public void push(double d) {

        if(size == data.length)
            data = Arrays.copyOf(data, size * 2);
        data[size++] = d;
    }

    public double pop() {

        return data[--size];
    }

    public int size() { return size; }
}