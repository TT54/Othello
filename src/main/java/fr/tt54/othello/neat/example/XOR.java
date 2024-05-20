package fr.tt54.othello.neat.example;

import fr.tt54.othello.neat.NEATEnvironment;
import fr.tt54.othello.neat.NeuronalNetworkIndividu;

import java.util.List;

public class XOR extends NEATEnvironment {

    @Override
    public float[] evaluateIndividus(List<NeuronalNetworkIndividu> individus) {
        long time = System.nanoTime();
        float[] fitnessArray = new float[individus.size()];

        for(int k = 0; k < individus.size(); k++){
            float fitness = 0;
            for (int i = 0; i < 2; i++)
                for (int j = 0; j < 2; j++) {
                    float inputs[] = {i, j};
                    float output[] = individus.get(k).evaluate(inputs);
                    output[0] = Math.min(1, Math.abs(output[0]));
                    int expected = i^j;
                                      /*System.out.println("Inputs are " + inputs[0] +" " + inputs[1] + " output " + output[0] + " Answer : " + (i ^ j));*/
                    //if (output[0] == (i ^ j))
                    fitness +=  (1 - Math.abs(expected - output[0]));
                }
            fitness = fitness * fitness;

            fitnessArray[k] = fitness;
        }

        System.out.println("eval time : " + (System.nanoTime() - time));

        return fitnessArray;
    }

    public static void main(String arg0[]){
/*        XOR xor = new XOR();
        NeuronalNetworkIndividu individu = xor.startTraining();
        System.out.println(Math.abs(individu.evaluate(new float[]{1,1})[0]));
        System.out.println(Math.abs(individu.evaluate(new float[]{1,0})[0]));
        System.out.println(Math.abs(individu.evaluate(new float[]{0,1})[0]));
        System.out.println(Math.abs(individu.evaluate(new float[]{0,0})[0]));*/

        XOR xor = new XOR();
        xor.startTraining();

        /*List<Neurone> neurones = new ArrayList<>();
        neurones.add(new Neurone(0, 0, Neurone.Type.IN, 0));
        neurones.add(new Neurone(1, 0, Neurone.Type.IN, 0));
        neurones.add(new Neurone(2, 0, Neurone.Type.OUT, 0));
        neurones.add(new Neurone(3, 0, Neurone.Type.HIDDEN, 0));
        neurones.add(new Neurone(4, 0, Neurone.Type.HIDDEN, 0));
        neurones.add(new Neurone(5, 0, Neurone.Type.HIDDEN, 0));



        NeuronalNetworkIndividu individu = new NeuronalNetworkIndividu(2, 1, null,
                new HashMap<>(), neurones);

        individu.addNeuronalConnection(4, 2, 4.8216553f, true, 1);
        individu.addNeuronalConnection(3, 5, 1.1308562f, true, 2);
        individu.addNeuronalConnection(5, 2, -1.2468098f, true, 3);
        individu.addNeuronalConnection(0, 2, -2.3794425f, true, 4);
        individu.addNeuronalConnection(0, 4, 2.1223752f, true, 5);
        individu.addNeuronalConnection(4, 5, -0.53875256f, true, 6);
        individu.addNeuronalConnection(3, 4, -1.0919795f, true, 7);
        individu.addNeuronalConnection(3, 2, -1.2673889f, false, 8);
        individu.addNeuronalConnection(1, 2, -2.325692f, true, 9);
        individu.addNeuronalConnection(1, 4, 2.2977395f, true, 10);

        System.out.println(individu.evaluate(new float[] {1, 0})[0]);
        System.out.println(individu.evaluate(new float[] {1, 1})[0]);
        System.out.println(individu.evaluate(new float[] {0, 1})[0]);
        System.out.println(individu.evaluate(new float[] {0, 0})[0]);*/
    }


}
