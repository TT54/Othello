package fr.tt54.othello.neat;

import java.util.*;

public class Species {

    private final NEATEnvironment environment;
    private NeuronalNetworkIndividu topFitness;
    private final Set<NeuronalNetworkIndividu> individus; // Map contenant les individus associés à leur "adjusted fitness"
    private int timeWithoutEvolving = 0;

    public Species(NEATEnvironment environment, NeuronalNetworkIndividu topFitness) {
        this.environment = environment;
        this.topFitness = topFitness;
        this.individus = new HashSet<>();
        this.individus.add(topFitness);
    }

    public void construct(){
        this.findTopFitness();
    }

    private void findTopFitness(){
        for(NeuronalNetworkIndividu individu : this.individus){
            if(topFitness == null || individu.getFitness() > topFitness.getFitness()){
                this.topFitness = individu;
            }
        }
    }

    public NEATEnvironment getEnvironment() {
        return environment;
    }

    public NeuronalNetworkIndividu getTopFitness() {
        return topFitness;
    }

    public Set<NeuronalNetworkIndividu> getIndividus() {
        return individus;
    }

    public void addIndividu(NeuronalNetworkIndividu individu) {
        this.individus.add(individu);
    }

    public boolean isInSpecies(NeuronalNetworkIndividu individu){
        return compatibilityDistance(individu, this.topFitness) <= NEATParameters.INSTANCE.compatibilityThreshold();
    }

    public int getTimeWithoutEvolving() {
        return timeWithoutEvolving;
    }

    public void setTimeWithoutEvolving(int timeWithoutEvolving) {
        this.timeWithoutEvolving = timeWithoutEvolving;
    }

    private float getAdjustedFitness(NeuronalNetworkIndividu individu){
        int sharingSum = 0;
        for(NeuronalNetworkIndividu networkIndividu : this.individus){
            sharingSum += sharingFunction(individu, networkIndividu);
        }

        return individu.getFitness() / sharingSum;
    }

    public float getAdjustedFitnessSum(){
        float totalAdjustedFitness = 0;
        for(NeuronalNetworkIndividu individu : this.individus){
            totalAdjustedFitness += individu.getFitness() / this.individus.size();
        }
        return totalAdjustedFitness;
    }

    public void removeWorst(int amountToRemove) {
        List<NeuronalNetworkIndividu> individuList = this.individus.stream().sorted(Comparator.comparingDouble(NeuronalNetworkIndividu::getFitness)).toList();
        for(int i = 0; i < Math.min(amountToRemove, individuList.size()); i++){
            this.individus.remove(individuList.get(i));
        }
    }

    /**
     * Effectue la reproduction de l'espèce et vide la liste des individus déjà présents dans l'espèce
     */
    public Set<NeuronalNetworkIndividu> reproduce(int offspringSize){
        List<NeuronalNetworkIndividu> individuList = new ArrayList<>(this.individus);
        Set<NeuronalNetworkIndividu> nextIndividus = new HashSet<>();
        int individusAmount = individuList.size();

        Random random = NeuronalNetworkIndividu.random;
        for (int i = 0; i < offspringSize; i++) {
            NeuronalNetworkIndividu individu1 = individuList.get(random.nextInt(individusAmount));
            NeuronalNetworkIndividu individu2 = individuList.get(random.nextInt(individusAmount));

            nextIndividus.add(NeuronalNetworkIndividu.crossOver(individu1, individu2));
        }
        this.individus.clear();

        return nextIndividus;
    }

    public static float compatibilityDistance(NeuronalNetworkIndividu individu1, NeuronalNetworkIndividu individu2){
        int excessAndDisjointGenes = 0;
        int sameGenes = 0;
        float weightDifferenceAverage = 0f;
        for(Map.Entry<Integer, NeuronalConnection> entry : individu2.getConnections().entrySet()){
            NeuronalConnection individuConn = individu1.getConnection(entry.getKey());
            if(individuConn == null){
                excessAndDisjointGenes++;
            } else {
                NeuronalConnection topConn = entry.getValue();
                weightDifferenceAverage += Math.abs(topConn.getWeight() - individuConn.getWeight());
                sameGenes++;
            }
        }
        for(int i : individu1.getConnections().keySet()){
            if(individu2.getConnection(i) == null){
                excessAndDisjointGenes++;
            }
        }
        if(sameGenes > 0) { // Cette condition ne sert normalement à rien, car les gènes liant les entrées et les sorties du réseau de neurones sont censés être communs à tous
            weightDifferenceAverage /= sameGenes;
        }

        int N = excessAndDisjointGenes + sameGenes;

        return (excessAndDisjointGenes * NEATParameters.INSTANCE.excessAndDisjointFactor() / N) + NEATParameters.INSTANCE.weightFactor() * weightDifferenceAverage / sameGenes;
    }

    public static int sharingFunction(NeuronalNetworkIndividu individu1, NeuronalNetworkIndividu individu2){
        return compatibilityDistance(individu1, individu2) <= NEATParameters.INSTANCE.compatibilityThreshold() ? 1 : 0;
    }
}
