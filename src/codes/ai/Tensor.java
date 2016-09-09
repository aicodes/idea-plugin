package codes.ai;

import com.intellij.psi.PsiMethod;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** @author xuy. Copyright (c) Ai.codes */
class Tensor {
  public static Tensor EMPTY = new Tensor();

  private boolean empty = true;
  float[] values;

  private Tensor() {}

  private Tensor(float[] values) {
    this.values = Arrays.copyOf(values, values.length);
    this.empty = false;
  }

  /* Assuming tensor is normalized, cosine similarity is just dot product*/
  float similarity(Tensor other) {
    if (empty || other.empty) return 0.0f;
    float r = 0.0f;
    for (int i = 0; i < values.length; ++i) {
      r += values[i] * other.values[i];
    }
    return r;
  }
}

/**
 * class TensorQuiver { ExecutorService tensorFetchService = Executors.newFixedThreadPool(16);
 * ConcurrentHashMap<String, Tensor> tensorCache;
 *
 * <p>public void fetchTensor(String name) { Future<Tensor> future =
 * tensorFetchService.submit((Callable<Tensor>) () -> {
 *
 * <p>// Add to gateway hashmap. // Fetch things // Save back to cache // Remove from HashMap }); }
 *
 * <p>Tensor getTensor(PsiMethod method) { String name = method.getName(); return getTensor(name); }
 *
 * <p>Tensor getTensor(String name) { if (tensorCache.containsKey(name)) { return
 * tensorCache.get(name); } else { fetchTensor(name); return Tensor.EMPTY; } } }
 */
