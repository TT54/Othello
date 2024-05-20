package fr.tt54.othello.neat;

public class NeuronalConnection {

    private final Neurone in;
    private final Neurone out;
    private final int innovationNumber;
    private float weight;
    private boolean enabled;

    public NeuronalConnection(Neurone in, Neurone out, float weight, int innovationNumber, boolean enabled) {
        this.in = in;
        this.out = out;
        this.weight = weight;
        this.innovationNumber = innovationNumber;
        this.enabled = enabled;
    }

    public Neurone getIn() {
        return in;
    }

    public Neurone getOut() {
        return out;
    }

    public float getWeight() {
        return weight;
    }

    public int getInnovationNumber() {
        return innovationNumber;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
