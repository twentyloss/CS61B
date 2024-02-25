package tester;

import static org.junit.Assert.*;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import student.StudentArrayDeque;

public class TestArrayDequeEC {

    @Test
    public void randomTestArrayDeque(){
        StudentArrayDeque<Integer> student = new StudentArrayDeque<>();
        ArrayDequeSolution<Integer> solution = new ArrayDequeSolution<>();

        int N = 5000;
        ArrayDequeSolution<String> failureSeq = new ArrayDequeSolution<>();
        Integer expect = 0, actual = 0;
        for(int i = 0; i < N; i++){
           int r = StdRandom.uniform(0, 5);
           int num = StdRandom.uniform(0, 1000);
            switch (r){
               case 0:
                   failureSeq.addLast("addFirst(" + num + ")");
                   student.addFirst(num);
                   solution.addFirst(num);
                   break;
               case 1:
                   failureSeq.addLast("addLast(" + num + ")");
                   student.addLast(num);
                   solution.addLast(num);
                   break;
                case 2:
                   if (solution.size() == 0 || student.size() == 0){
                       break;
                   }
                   failureSeq.addLast("removeFirst()");
                   actual = student.removeFirst();
                   expect = solution.removeFirst();
                   break;
                case 3:
                    if (solution.size() == 0 || student.size() == 0){
                        break;
                    }
                    failureSeq.addLast("removeLast()");
                    actual = student.removeLast();
                    expect = solution.removeLast();
                    break;
                case 4:
                    failureSeq.addLast("size()");
                    actual = student.size();
                    expect = solution.size();
                    break;
           }
           assertEquals(String.join("\n", failureSeq), expect, actual);



        }
    }

}
