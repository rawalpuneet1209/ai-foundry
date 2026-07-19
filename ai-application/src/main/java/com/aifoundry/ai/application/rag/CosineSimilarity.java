package com.aifoundry.ai.application.rag;

import java.util.List;

public final class CosineSimilarity {
  public double calculate(List<Float> left, List<Float> right) {
    if (left.size() != right.size() || left.isEmpty()) {
      return 0;
    }
    double dotProduct = 0;
    double leftMagnitude = 0;
    double rightMagnitude = 0;
    for (int index = 0; index < left.size(); index++) {
      dotProduct += left.get(index) * right.get(index);
      leftMagnitude += left.get(index) * left.get(index);
      rightMagnitude += right.get(index) * right.get(index);
    }
    if (leftMagnitude == 0 || rightMagnitude == 0) {
      return 0;
    }
    return dotProduct / (Math.sqrt(leftMagnitude) * Math.sqrt(rightMagnitude));
  }
}
