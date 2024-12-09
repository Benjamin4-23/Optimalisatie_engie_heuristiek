package org.example.search.framework;

import java.util.Random;

public class RandomGenerator {
    private final Random random;

    public RandomGenerator(long seed) {
        this.random = new Random(seed);
    }

    public Random getRandom() {
        return random;
    }
}
