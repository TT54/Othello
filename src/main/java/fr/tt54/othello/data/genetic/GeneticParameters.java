package fr.tt54.othello.data.genetic;

public record GeneticParameters(float mutationProba, float crossOverProba, int evaluationGamesAmount, int[] evaluationDepth, int population) {
}
