package Restaurant_Manager;

class DequeNode<T> {

    T data;
    DequeNode<T> next;
    DequeNode<T> prev;
    int insertionOrder;

    DequeNode(T data, int insertionOrder) {
        this.data = data;
        this.insertionOrder = insertionOrder;
        next = null;
        prev = null;
    }
}

public class LinkedDeque<T> {

    private DequeNode<T> front;
    private DequeNode<T> rear;
    private int size;
    private int nextInsertionOrder;

    // Constructor
    public LinkedDeque() {
        front = null;
        rear = null;
        size = 0;
        nextInsertionOrder = 0;
    }

    // Add an item to the front
    public void addFront(T item) {
        DequeNode<T> newNode =
                new DequeNode<>(item, nextInsertionOrder++);

        if (isEmpty()) {
            front = newNode;
            rear = newNode;
        } else {
            newNode.next = front;
            front.prev = newNode;
            front = newNode;
        }

        size++;
    }
    public void addRear(T item) {
        DequeNode<T> newNode =
                new DequeNode<>(item, nextInsertionOrder++);

        if (isEmpty()) {
            front = newNode;
            rear = newNode;
        } else {
            rear.next = newNode;
            newNode.prev = rear;
            rear = newNode;
        }

        size++;
    }

    public T removeFront() {
        if (isEmpty()) {
            return null;
        }

        T data = front.data;
        front = front.next;

        if (front == null) {
            rear = null;
        }
        else {
            front.prev = null;
        }

        size--;
        return data;
    }

    public T removeRear() {
        if (isEmpty()) {
            return null;
        }

        T data = rear.data;
        rear = rear.prev;

        if (rear == null) {
            front = null;
        }
        else {
            rear.next = null;
        }

        size--;
        return data;
    }

    public T removeNewest() {
        if (isEmpty()) {
            return null;
        }

        if (front.insertionOrder > rear.insertionOrder) {
            return removeFront();
        }

        return removeRear();
    }

    // Get an item by its position
    public T get(int index) {
        if (index < 0 || index >= size) {
            return null;
        }

        DequeNode<T> current = front;

        for (int i = 0; i < index; i++) {
            current = current.next;
        }

        return current.data;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }
}
