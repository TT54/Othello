package fr.tt54.othello.neat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Neurone {

    private int id;
    private int depth;
    private final Map<Integer, NeuronalConnection> incomingNeurones;
    private final Map<Integer, NeuronalConnection> outgoingNeurones;
    private final Type type;
    private float value = 0;

    public Neurone(int id, int depth, Type type, float value) {
        this.id = id;
        this.depth = depth;
        this.outgoingNeurones = new HashMap<>();
        this.incomingNeurones = new HashMap<>();
        this.type = type;
        this.value = value;
    }

    private void addIncomingNeurone(NeuronalConnection connection){
        incomingNeurones.put(connection.getIn().id, connection);
        this.updateDepth(connection.getIn().getDepth() + 1);
    }

    private void addOutgoingNeurone(NeuronalConnection connection){
        outgoingNeurones.put(connection.getOut().id, connection);
    }

    public void addConnection(NeuronalConnection connection) {
        if(connection.getIn() == this){
            this.addOutgoingNeurone(connection);
        } else {
            this.addIncomingNeurone(connection);
        }
    }

    public float evaluate(){
        List<NeuronalConnection> incoming = incomingNeurones.values().stream().filter(NeuronalConnection::isEnabled).toList();
        for(NeuronalConnection connection : incoming){
            this.value += connection.getIn().getValue() * connection.getWeight();
        }
        //this.value /= totalWeight;
        this.value = sigmoid(this.value);
        return this.value;
    }

    private static float sigmoid(float x) {
        return (float) (1 / (1 + Math.exp(-4.9 * x)));
    }

    public void setValue(float value) {
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public int getDepth() {
        return depth;
    }

    private void updateDepth(int newDepth){
        if(this.depth < newDepth){
            this.depth = newDepth;

            for(NeuronalConnection connection : this.outgoingNeurones.values()){
                connection.getOut().updateDepth(depth + 1);
            }
        }
    }

    public Map<Integer, NeuronalConnection> getIncomingNeurones() {
        return incomingNeurones;
    }

    public Map<Integer, NeuronalConnection> getOutgoingNeurones() {
        return outgoingNeurones;
    }

    public Type getType() {
        return type;
    }

    public float getValue() {
        return value;
    }

    public boolean isNeuroneIncoming(int neuroneId) {
        return this.incomingNeurones.containsKey(neuroneId);
    }

    public void increaseId() {
        this.id++;
    }

    @Override
    public Neurone clone() {
        return new Neurone(this.id, depth, this.type, this.value);
    }

    public enum Type{

        IN,
        HIDDEN,
        OUT;

    }

}
