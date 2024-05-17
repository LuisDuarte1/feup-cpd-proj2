package feup.cpd.server.collections;

import java.io.Serializable;

public record Pair<T, V>(T first, V second) implements Serializable { }
