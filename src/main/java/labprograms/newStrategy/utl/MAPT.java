package labprograms.newStrategy.utl;

import labprograms.gethardkilledmutants.MT4ACMS;
import labprograms.gethardkilledmutants.MT4CUBS;
import labprograms.gethardkilledmutants.MT4ERS;
import labprograms.gethardkilledmutants.MT4MOS;
import labprograms.testCase.TestCase4ACMS;
import labprograms.testCase.TestCase4CUBS;
import labprograms.testCase.TestCase4ERS;
import labprograms.testCase.TestCase4MOS;

import java.util.Random;

/**
 * @Description:
 * @auther phantom
 * @create 2019-09-23 上午11:33
 */
public class MAPT {

    /**
     * the test profile of MAPT
     */
    private double[][] MAPT;

    private double MAPT_gamma = 0.1;

    private double MAPT_tau = 0.1;

    /**
     * initialize the test profile of RAPT
     *
     * @param numberOfPartitions the number of partitions
     */
    public void initializeMAPT(int numberOfPartitions) {
        MAPT = new double[numberOfPartitions][numberOfPartitions];
        for (int i = 0; i < numberOfPartitions; i++) {
            for (int j = 0; j < numberOfPartitions; j++) {
                MAPT[i][j] = 1.0 / numberOfPartitions;
            }
        }
    }

    /**
     * get a index of partition
     * Note that the first number of partitions is 0
     *
     * @return the index
     */
    public int nextPartition4MAPT(int formerPartitionNumber) {
        double[] tempArray = new double[MAPT.length];
        for (int i = 0; i < tempArray.length; i++) {
            tempArray[i] = MAPT[formerPartitionNumber][i];
        }
        int index = -1;
        double randomNumber = new Random().nextDouble();
        double sum = 0;
        do {
            index++;
            sum += tempArray[index];
        } while (randomNumber >= sum && index < tempArray.length - 1);
        return index;
    }

    /**
     * adjust the test profile for MAPT testing
     *
     * @param formerSourcePartitionIndex
     * @param formerfollowUpPartitionIndex
     * @param isKilledMutans
     */
    public void adjustMAPT(int formerSourcePartitionIndex,
                            int formerfollowUpPartitionIndex,
                            boolean isKilledMutans) {
        //the source test case and follow-up test case belong to the same partition
        double old_i = MAPT[formerSourcePartitionIndex][formerSourcePartitionIndex];
        double old_f = MAPT[formerSourcePartitionIndex][formerfollowUpPartitionIndex];


        if (formerSourcePartitionIndex == formerfollowUpPartitionIndex) {
            // the test case killed a mutant
            if (isKilledMutans) { //same partition and killed a mutant
                double sum = 0;
                double threshold = MAPT_gamma * old_i / (MAPT.length - 1);
                for (int i = 0; i < MAPT.length; i++) {
                    if (i != formerSourcePartitionIndex) {
                        if (MAPT[formerSourcePartitionIndex][i] > threshold) {
                            MAPT[formerSourcePartitionIndex][i] -= threshold;
                        }
                    }
                    sum += MAPT[formerSourcePartitionIndex][i];
                }
                MAPT[formerSourcePartitionIndex][formerSourcePartitionIndex] = 1 - sum;
            } else { // same partition and do not kill a mutant
                double threshod = MAPT_tau * (1 - old_i) / (MAPT.length - 1);
                for (int i = 0; i < MAPT.length; i++) {
                    if (i != formerSourcePartitionIndex) {
                        if (MAPT[formerSourcePartitionIndex][formerSourcePartitionIndex] > threshod) {
                            MAPT[formerSourcePartitionIndex][i] +=
                                    MAPT_tau * MAPT[formerSourcePartitionIndex][i] / (MAPT.length - 1);
                        }
                    } else {
                        if (MAPT[formerSourcePartitionIndex][formerSourcePartitionIndex] > threshod) {
                            MAPT[i][i] -= MAPT_tau * (1 - MAPT[i][i]) / (MAPT.length - 1);
                        }
                    }
                }
            }
        } else { //source test case and follow-up test case do not belong to same partition
            // the test case do not kill a mutant
            if (isKilledMutans) {
                double sum = 0;
                double threshold = MAPT_gamma * (old_i + old_f) / (MAPT.length - 2);

                for (int i = 0; i < MAPT.length; i++) {
                    if (i != formerSourcePartitionIndex && i != formerfollowUpPartitionIndex) {
                        if (MAPT[formerSourcePartitionIndex][i] > threshold) {
                            MAPT[formerSourcePartitionIndex][i] -= threshold;
                        }
                    }
                    sum += MAPT[formerSourcePartitionIndex][i];
                }

                MAPT[formerSourcePartitionIndex][formerSourcePartitionIndex] = old_i +
                        ((1 - sum) - old_i - old_f) / 2;
                MAPT[formerSourcePartitionIndex][formerfollowUpPartitionIndex] = old_f +
                        ((1 - sum) - old_i - old_f) / 2;
            } else { // source test case and follow-up test case are not belonging to the same partition and do not reveal a mutant

                double threshold = (MAPT_tau * (1 - old_i - old_f)) / (MAPT.length - 2);
                for (int i = 0; i < MAPT.length; i++) {
                    if (i != formerSourcePartitionIndex && i != formerfollowUpPartitionIndex) {
                        if (old_i > threshold || old_f > threshold) {
                            MAPT[formerSourcePartitionIndex][i] += MAPT_tau *
                                    MAPT[formerSourcePartitionIndex][i] / (MAPT.length - 2);
                        }
                    }
                }
                if (old_i > threshold) {
                    MAPT[formerSourcePartitionIndex][formerSourcePartitionIndex] -= threshold;
                }
                if (old_f > threshold) {
                    MAPT[formerSourcePartitionIndex][formerfollowUpPartitionIndex] -= threshold;
                }

            }
        }
    }
}
