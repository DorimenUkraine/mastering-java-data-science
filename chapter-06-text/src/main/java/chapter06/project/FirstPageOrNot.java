package chapter06.project;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.SerializationUtils;

import chapter06.cv.Dataset;
import chapter06.ml.LibLinear;
import chapter06.ml.Metrics;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.SolverType;
import joinery.DataFrame;
import smile.classification.RandomForest;
import smile.classification.SoftClassifier;
import smile.classification.DecisionTree.SplitRule;

public class FirstPageOrNot {

    public static void main(String[] args) throws IOException {
        DataFrame<Number> trainFeatures = load("data/project-train-features.bin");
        DataFrame<Number> testFeatures = load("data/project-test-features.bin");

        LibLinear.mute();
        Dataset trainDataset = toDataset(trainFeatures);
        Dataset testDataset = toDataset(testFeatures);

        calculateBaselines(testFeatures, testDataset.getY());

        RandomForest rf = new RandomForest.Trainer(100)
                .setMaxNodes(128)
                .setNumRandomFeatures(6)
                .setSamplingRates(0.6)
                .setSplitRule(SplitRule.GINI)
                .train(trainDataset.getX(), trainDataset.getYAsInt());

        double auc = auc(rf, testDataset);
        System.out.println(auc);
    }

    public static double auc(SoftClassifier<double[]> model, Dataset dataset) {
        double[] probability = predict(model, dataset);
        return Metrics.auc(dataset.getY(), probability);
    }

    public static double[] predict(SoftClassifier<double[]> model, Dataset dataset) {
        double[][] X = dataset.getX();
        double[] result = new double[X.length];

        double[] probs = new double[2];
        for (int i = 0; i < X.length; i++) {
            model.predict(X[i], probs);
            result[i] = probs[1];
        }

        return result;
    }

    private static void calculateBaselines(DataFrame<Number> df, double[] y) {
        df = df.drop("relevance");

        Set<Object> columns = df.columns();
        for (Object columnName : columns) {
            List<Number> col = df.col(columnName);
            double[] baseline = col.stream().mapToDouble(i -> i.doubleValue()).toArray();
            double baselineAuc = Metrics.auc(y, baseline);
            System.out.printf("%s: %.4f%n", String.valueOf(columnName), baselineAuc);
        }
    }

    private static Dataset toDataset(DataFrame<Number> df) {
        double[] y = df.col("relevance").stream().mapToDouble(i -> i.doubleValue()).toArray();
        df = df.drop("relevance");
        double[][] X = df.toModelMatrix(0.0);
        return new Dataset(X, y);
    }

    private static DataFrame<Number> load(String filepath) throws IOException {
        Path path = Paths.get(filepath);
        try (InputStream is = Files.newInputStream(path)) {
            try (BufferedInputStream bis = new BufferedInputStream(is)) {
                DfHolder<Number> holder = SerializationUtils.deserialize(bis);
                return holder.toDf();
            }
        }
    }

}