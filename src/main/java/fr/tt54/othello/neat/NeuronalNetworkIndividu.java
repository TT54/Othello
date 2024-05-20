package fr.tt54.othello.neat;

import java.util.*;

public class NeuronalNetworkIndividu {

    public static final Random random = new Random();

    private final int inNeuronesAmount;
    private final int outNeuronesAmount;
    private float fitness = 0;
    private final NEATEnvironment environment;
    private Map<Integer, NeuronalConnection> connections;
    private List<Neurone> neurones;

    public NeuronalNetworkIndividu(int inNeuronesAmount, int outNeuronesAmount, NEATEnvironment environment, Map<Integer, NeuronalConnection> connections, List<Neurone> neurones) {
        this.inNeuronesAmount = inNeuronesAmount;
        this.outNeuronesAmount = outNeuronesAmount;
        this.environment = environment;
        this.connections = connections;
        this.neurones = neurones;
    }

    public int getInNeuronesAmount() {
        return inNeuronesAmount;
    }

    public int getOutNeuronesAmount() {
        return outNeuronesAmount;
    }

    public NEATEnvironment getEnvironment() {
        return environment;
    }

    public Map<Integer, NeuronalConnection> getConnections() {
        return connections;
    }

    public List<Neurone> getNeurones() {
        return neurones;
    }

    public Neurone getNeurone(int id){
        return this.neurones.get(id);
    }

    public float getFitness() {
        return fitness;
    }

    public void setFitness(float fitness) {
        this.fitness = fitness;
    }

    public float[] evaluate(float[] input){
        for(int i = 0; i < this.neurones.size(); i++){
            this.getNeurone(i).setValue(0);
        }

        for(int i = 0; i < NEATParameters.INSTANCE.inNeuronesAmount(); i++){
            this.getNeurone(i).setValue(input[i]);
        }

        List<Neurone> neurones = this.neurones.subList(NEATParameters.INSTANCE.inNeuronesAmount() + NEATParameters.INSTANCE.outNeuronesAmount(), this.neurones.size()).stream().sorted(Comparator.comparingInt(Neurone::getDepth)).toList();
        for(int i = 0; i < neurones.size(); i++){
            neurones.get(i).evaluate();
        }

        float[] result = new float[NEATParameters.INSTANCE.outNeuronesAmount()];
        for(int i = 0; i < NEATParameters.INSTANCE.outNeuronesAmount(); i++){
            result[i] = this.getNeurone(NEATParameters.INSTANCE.inNeuronesAmount() + i).evaluate();
        }
        return result;
    }

    public void addNeuronalConnection(int in, int out, float weight, boolean enabled, int innovationNumber){
        Neurone inNeurone = this.getNeurone(in);
        Neurone outNeurone = this.getNeurone(out);

        NeuronalConnection connection = new NeuronalConnection(inNeurone, outNeurone, weight, innovationNumber, enabled);
        inNeurone.addConnection(connection);
        outNeurone.addConnection(connection);

        this.connections.put(connection.getInnovationNumber(), connection);
    }

    public void generateNeuronalConnection(int in, int out, float weight) {
        Neurone inNeurone = this.getNeurone(in);
        Neurone outNeurone = this.getNeurone(out);

        if((in < this.inNeuronesAmount || in >= this.inNeuronesAmount + this.outNeuronesAmount)                 // On vérifie que le neurone1 n'est pas un neurone final
                && out >= this.inNeuronesAmount                                                                          // On vérifie que le neurone2 n'est pas un neurone initial
                && (inNeurone.getDepth() < outNeurone.getDepth() || out < this.inNeuronesAmount + this.outNeuronesAmount)   // On vérifie que le neurone1 a une profondeur plus faible que
                // le neurone2 ou que le neurone2 est final
                && !outNeurone.isNeuroneIncoming(in))                                                                     // On vérifie que le neurone1 n'a pas déjà une connexion vers le neurone2
        {
            NeuronalConnection connection = new NeuronalConnection(inNeurone, outNeurone, weight, this.environment.increaseMaxInnovationNumber(), true);
            inNeurone.addConnection(connection);
            outNeurone.addConnection(connection);

            this.connections.put(connection.getInnovationNumber(), connection);
        }
    }

    public void mutate(){
        if(random.nextFloat() < NEATParameters.INSTANCE.changeWeightChance()){
            float uniformChance = 2 * random.nextFloat() - 1f;
            for(NeuronalConnection connection : this.connections.values()){
                if(connection != null){
                    if(random.nextFloat() < NEATParameters.INSTANCE.randomWeightPerturbationChance()){
                        connection.setWeight(4 * random.nextFloat() - 2);
                    } else {
                        connection.setWeight(connection.getWeight() + uniformChance * 0.1f);
                    }
                }
            }
        }

        if(random.nextFloat() < NEATParameters.INSTANCE.addConnectionChance()){
            int neurone1Id = random.nextInt(this.neurones.size());
            int neurone2Id = random.nextInt(this.neurones.size());
            Neurone neurone1 = this.getNeurone(neurone1Id);
            Neurone neurone2 = this.getNeurone(neurone2Id);

            if((neurone1Id < this.inNeuronesAmount || neurone1Id >= this.inNeuronesAmount + this.outNeuronesAmount)                 // On vérifie que le neurone1 n'est pas un neurone final
                    && neurone2Id >= this.inNeuronesAmount                                                                          // On vérifie que le neurone2 n'est pas un neurone initial
                    && (neurone1.getDepth() < neurone2.getDepth() || neurone2Id < this.inNeuronesAmount + this.outNeuronesAmount)   // On vérifie que le neurone1 a une profondeur plus faible que
                                                                                                                                    // le neurone2 ou que le neurone2 est final
                    && !neurone2.isNeuroneIncoming(neurone1Id))                                                                     // On vérifie que le neurone1 n'a pas déjà une connexion vers le neurone2
            {
                this.generateNeuronalConnection(neurone1Id, neurone2Id, 4 * random.nextFloat() - 2);
            }
        }

        if(random.nextFloat() < NEATParameters.INSTANCE.addNeuroneChance()){
            List<NeuronalConnection> connectionList = new ArrayList<>(this.connections.values());
            NeuronalConnection connectionToBreak = connectionList.get(random.nextInt(connectionList.size()));

            if(connectionToBreak.isEnabled()) {
                Neurone in = connectionToBreak.getIn();
                Neurone out = connectionToBreak.getOut();

                Neurone neurone = new Neurone(this.neurones.size(), -1, Neurone.Type.HIDDEN, 0);
                this.neurones.add(neurone);

                connectionToBreak.setEnabled(false);
                this.generateNeuronalConnection(in.getId(), neurone.getId(), 1f);
                this.generateNeuronalConnection(neurone.getId(), out.getId(), connectionToBreak.getWeight());
            }
        }

        if(random.nextFloat() < NEATParameters.INSTANCE.disableMutationChance()){
            NeuronalConnection connection = new ArrayList<>(this.connections.values()).get(random.nextInt(this.connections.size()));
            connection.setEnabled(false);
        }

        if(random.nextFloat() < NEATParameters.INSTANCE.enableMutationChance()){
            NeuronalConnection connection = new ArrayList<>(this.connections.values()).get(random.nextInt(this.connections.size()));
            connection.setEnabled(true);
        }
    }

    public NeuronalConnection getConnection(int innovationNumber){
        return this.connections.get(innovationNumber);
    }

    public List<Neurone> copyNeurones() {
        List<Neurone> neuroneList = new ArrayList<>();
        for(Neurone neurone : this.neurones){
            neuroneList.add(neurone.clone());
        }
        return neuroneList;
    }

    public static NeuronalNetworkIndividu crossOver(NeuronalNetworkIndividu individu1, NeuronalNetworkIndividu individu2){
        if(individu1.neurones.size() < individu2.neurones.size()){
            NeuronalNetworkIndividu tempo = individu1;
            individu1 = individu2;
            individu2 = tempo;
        }

        NeuronalNetworkIndividu individu = new NeuronalNetworkIndividu(individu1.inNeuronesAmount, individu1.outNeuronesAmount, individu1.environment, new HashMap<>(), individu1.copyNeurones());

        for(int i : individu1.connections.keySet()){
            if(individu2.connections.containsKey(i)){
                NeuronalConnection connection;
                if(random.nextBoolean()){
                    connection = individu1.getConnection(i);
                } else {
                    connection = individu2.getConnection(i);
                }
                boolean enabled = (individu1.getConnection(i).isEnabled() && individu2.getConnection(i).isEnabled()) || random.nextFloat() > NEATParameters.INSTANCE.disableConnectionGeneTransmitChance();
                individu.addNeuronalConnection(connection.getIn().getId(), connection.getOut().getId(), connection.getWeight(), enabled, connection.getInnovationNumber());
            } else {
                NeuronalConnection connection = individu1.getConnection(i);
                boolean enabled = connection.isEnabled() || random.nextFloat() > NEATParameters.INSTANCE.disableConnectionGeneTransmitChance();
                individu.addNeuronalConnection(connection.getIn().getId(), connection.getOut().getId(), connection.getWeight(), enabled, connection.getInnovationNumber());
            }
        }

        for(int i : individu2.connections.keySet()){
            if(!individu1.connections.containsKey(i)){
                NeuronalConnection connection = individu2.getConnection(i);
                boolean enabled = connection.isEnabled() || random.nextFloat() > NEATParameters.INSTANCE.disableConnectionGeneTransmitChance();
                individu.addNeuronalConnection(connection.getIn().getId(), connection.getOut().getId(), connection.getWeight(), enabled, connection.getInnovationNumber());
            }
        }

        int p = individu.getConnections().size();


        individu.mutate();


/*        if(p < individu.getConnections().size()){
            System.out.println(p + " -  " + individu.getConnections().size());
        }*/

        return individu;
    }

    public static NeuronalNetworkIndividu generateDefaultIndividu(NEATEnvironment environment, int inNeuronesAmount, int outNeuronesAmount){
        List<Neurone> neurones = new ArrayList<>();
        for(int i = 0; i < inNeuronesAmount; i++){
            neurones.add(new Neurone(i, 0, Neurone.Type.IN, 0));
        }
        for(int i = inNeuronesAmount; i < inNeuronesAmount + outNeuronesAmount; i++){
            neurones.add(new Neurone(i, 1, Neurone.Type.OUT, 0));
        }

        NeuronalNetworkIndividu individu = new NeuronalNetworkIndividu(inNeuronesAmount, outNeuronesAmount, environment, new HashMap<>(), neurones);
        int innovationNumber = 0;
        for(int i = 0; i < inNeuronesAmount; i++){
            for(int j = inNeuronesAmount; j < inNeuronesAmount + outNeuronesAmount; j++){
                individu.addNeuronalConnection(i, j, 4 * random.nextFloat() - 2f, true, innovationNumber);
                innovationNumber++;
            }
        }

        return individu;
    }
}
