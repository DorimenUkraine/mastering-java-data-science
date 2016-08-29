package chapter04.cv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jsat.classifiers.CategoricalData;
import jsat.classifiers.ClassificationDataSet;
import jsat.classifiers.DataPoint;
import jsat.classifiers.DataPointPair;
import jsat.linear.DenseVector;

public class Dataset {
    
    private static long SEED = 1;

    private final double[][] X;
    private final double[] y;

    public Dataset(double[][] X, double[] y) {
        this.X = X;
        this.y = y;
    }

    public double[][] getX() {
        return X;
    }

    public double[] getY() {
        return y;
    }

    public int[] getYAsInt() {
        return Arrays.stream(y).mapToInt(d -> (int) d).toArray();
    }

    public int length() {
        return getX().length;
    }

    public List<Fold> shuffleKFold(int k) {
        return CV.kfold(this, k, true, SEED);
    }

    public List<Fold> kfold(int k) {
        return CV.kfold(this, k, false, SEED);
    }

    public Fold trainTestSplit(double testRatio) {
        return CV.trainTestSplit(this, testRatio, false, SEED);
    }

    public Fold shuffleSplit(double testRatio) {
        return CV.trainTestSplit(this, testRatio, true, SEED);
    }

    public ClassificationDataSet toJsatDataset() {
        // TODO: what if it's not binary?
        CategoricalData binary = new CategoricalData(2);

        List<DataPointPair<Integer>> data = new ArrayList<>(X.length);
        for (int i = 0; i < X.length; i++) {
            int target = (int) y[i];
            DataPoint row = new DataPoint(new DenseVector(X[i]));
            data.add(new DataPointPair<Integer>(row, target));
        }

        return new ClassificationDataSet(data, binary);
    }
}
