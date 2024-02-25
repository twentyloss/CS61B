package gh2;

//import deque.Deque;
//import deque.LinkedListDeque;
import edu.princeton.cs.algs4.StdAudio;
import edu.princeton.cs.algs4.StdDraw;

public class GuitarHero {

    public static void main(String[] args) {
        String keyboard = "q2we4r5ty7u8i9op-[=zxdcfvgbnjmk,.;/' ";
        int keysCnt = keyboard.length();
        double[] frequecies = new double[keysCnt];
        GuitarString[] strings = new GuitarString[keysCnt];
        for (int i = 0; i < keysCnt; i++) {
            frequecies[i] = 440 * Math.pow(2, ((i - 24) / (double) 12));
            strings[i] = new GuitarString(frequecies[i]);
        }


        while (true) {

            /* check if the user has typed a key; if so, process it */
            if (StdDraw.hasNextKeyTyped()) {
                char key =  StdDraw.nextKeyTyped();
                int index = keyboard.indexOf(key);
                if (index != -1) {
                    strings[index].pluck();
                }
            }

            /* compute the superposition of samples */
            double sample = 0;
            for (int i = 0; i < keysCnt; i++) {
                /* compute the superposition of samples */
                sample += strings[i].sample();
                /* advance the simulation of each guitar string by one step */
                strings[i].tic();
            }
            /* play the sample on standard audio */
            StdAudio.play(sample);

        }
    }
}
