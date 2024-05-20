package fr.tt54.othello.neat;

public record NEATParameters(int inNeuronesAmount, int outNeuronesAmount, // Paramètres du réseau de neurones
                             float mutationChance, float addNeuroneChance, float changeWeightChance, float addConnectionChance, float randomWeightPerturbationChance, float disableConnectionGeneTransmitChance, // Coefficients de mutations
                             float excessAndDisjointFactor, float weightFactor, float compatibilityThreshold, // Coefficients relatifs aux espèces
                             int amountToRemoveAtReproduction, int totalPopulation, float targetedFitness,
                             float disableMutationChance, float enableMutationChance
) {

    public static final NEATParameters INSTANCE = new NEATParameters(
            2,
            1,
            0.9f,
            0.2f,
            0.8f,
            0.2f,
            0.25f,
            0.75f,
            1f,
            0.4f,
            1f,
            150,
            300,
            15f,
            0.1f,
            0.2f
    );

}
