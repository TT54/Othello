package fr.tt54.othello.neat;

import java.util.*;

public abstract class NEATEnvironment {

    private int timeWithoutEvolving = 0;
    private int maxInnovationNumber;
    private final Set<NeuronalNetworkIndividu> individus = new HashSet<>();
    private List<Species> species = new ArrayList<>();

    public NEATEnvironment() {
        this.maxInnovationNumber = NEATParameters.INSTANCE.inNeuronesAmount() * NEATParameters.INSTANCE.outNeuronesAmount() - 1;
    }

    public int getMaxInnovationNumber() {
        return maxInnovationNumber;
    }

    public int increaseMaxInnovationNumber(){
        return this.maxInnovationNumber++;
    }

    public abstract float[] evaluateIndividus(List<NeuronalNetworkIndividu> individus);

    public void addIndividu(NeuronalNetworkIndividu individu){
        individus.add(individu);
        for(Species species : this.species){
            if(species.isInSpecies(individu)){
                species.addIndividu(individu);
                return;
            }
        }

        Species species = new Species(this, individu);
        this.species.add(species);
    }

    private void fillIndividues(){

    }

    public NeuronalNetworkIndividu startTraining(){
        for(int i = 0; i < NEATParameters.INSTANCE.totalPopulation(); i++){
            addIndividu(NeuronalNetworkIndividu.generateDefaultIndividu(this, NEATParameters.INSTANCE.inNeuronesAmount(), NEATParameters.INSTANCE.outNeuronesAmount()));
        }

        List<NeuronalNetworkIndividu> individuList = new ArrayList<>(this.individus);
        float[] fitness = evaluateIndividus(individuList);

        System.out.println(Arrays.toString(fitness));

        for(int i = 0; i < individuList.size(); i++){
            individuList.get(i).setFitness(fitness[i]);
        }

        NeuronalNetworkIndividu topIndividu = null;
        for(Species species : this.species){
            species.construct();
            if(topIndividu == null || species.getTopFitness().getFitness() > topIndividu.getFitness()){
                topIndividu = species.getTopFitness();
            }
        }

        System.out.println(topIndividu.getFitness());
        if(topIndividu.getFitness() >= NEATParameters.INSTANCE.targetedFitness()){
            System.out.println("fini !");
            return topIndividu;
        }

        int generation = 1;

        while (topIndividu.getFitness() < NEATParameters.INSTANCE.targetedFitness()){
            if (generation == 1500)
                return null;

            float previousFitness = topIndividu.getFitness();
            topIndividu = this.reproduceIndividus(generation++);

            if(topIndividu.getFitness() == previousFitness){
                this.timeWithoutEvolving++;
            } else {
                this.timeWithoutEvolving = 0;
            }

            for(Species species : new ArrayList<>(this.species)){
                if(species.getTopFitness().getFitness() <= previousFitness){
                    species.setTimeWithoutEvolving(species.getTimeWithoutEvolving() + 1);
                } else {
                    species.setTimeWithoutEvolving(0);
                }

                if(species.getTimeWithoutEvolving() >= 20){
                    for(NeuronalNetworkIndividu individu : species.getIndividus()){
                        this.individus.remove(individu);
                    }
                    this.species.remove(species);
                }
            }


            /*if(timeWithoutEvolving >= 20){
                timeWithoutEvolving = 0;
                Species survivor = this.species.get(0);
                for(Species species : this.species){
                    if(species.getTopFitness() == topIndividu){
                        survivor = species;
                    }
                }
                this.individus.clear();
                this.species.clear();
                this.individus.addAll(survivor.getIndividus());
                System.out.println("look " + survivor.getTopFitness().getConnections().size());
                this.species.add(survivor);
            }*/


            System.out.println("fitness : " + topIndividu.getFitness());
            System.out.println(this.species.size());
            System.out.println(this.individus.size());
            System.out.println(topIndividu.getConnections().size());
            System.out.println(new ArrayList<>(this.individus).get(0).getConnections().size());
        }

        System.out.println("fini !");
        return topIndividu;
    }

    public NeuronalNetworkIndividu reproduceIndividus(int generation){
        System.out.println();
        System.out.println("## Generation " + generation);

        long totalTime = System.currentTimeMillis();

        List<NeuronalNetworkIndividu> topIndividus = new ArrayList<>();
        Set<NeuronalNetworkIndividu> individus = new HashSet<>();


        long t = System.currentTimeMillis();

        float[] speciesAdjustedFitness = new float[species.size()];
        float totalAdjustedFitness = 0;
        for(int i = 0; i < this.species.size(); i++){
            Species species = this.species.get(i);
            float adjustedFitness = species.getAdjustedFitnessSum();
            speciesAdjustedFitness[i] = adjustedFitness;
            totalAdjustedFitness += adjustedFitness;
        }

        //System.out.println("temps inter : " + (System.currentTimeMillis() - t) + " ms");


        long time = System.nanoTime();
        for(int i = 0; i < this.species.size(); i++){
            Species species = this.species.get(i);
            NeuronalNetworkIndividu topIndividu = species.getTopFitness();
            topIndividus.add(topIndividu);

            species.removeWorst((int) (NEATParameters.INSTANCE.amountToRemoveAtReproduction() * species.getIndividus().size() * (speciesAdjustedFitness[i] / totalAdjustedFitness) / NEATParameters.INSTANCE.totalPopulation()));
            Set<NeuronalNetworkIndividu> newIndividus = species.reproduce((int) (NEATParameters.INSTANCE.totalPopulation() * (speciesAdjustedFitness[i] / totalAdjustedFitness)));

            individus.addAll(newIndividus);
        }
        //System.out.println("reproduction time : " + (System.nanoTime() - time));


        List<Species> newSpecies = new ArrayList<>(topIndividus.size());
        for(NeuronalNetworkIndividu individu : topIndividus){
            newSpecies.add(new Species(this, individu));
        }

        this.species = newSpecies;
        this.individus.clear();
        this.individus.addAll(topIndividus);

        for(NeuronalNetworkIndividu individu : individus){
            addIndividu(individu);
        }

        List<NeuronalNetworkIndividu> individuList = new ArrayList<>(this.individus);

        float[] fitness = evaluateIndividus(individuList);

        for(int i = 0; i < individuList.size(); i++){
            individuList.get(i).setFitness(fitness[i]);
        }

        NeuronalNetworkIndividu topIndividu = null;
        for(Species species : this.species){
            species.construct();
            if(topIndividu == null || species.getTopFitness().getFitness() > topIndividu.getFitness()){
                topIndividu = species.getTopFitness();
            }
        }

        //System.out.println("temps total : " + (System.currentTimeMillis() - totalTime) + " ms");

        return topIndividu;
    }
}
