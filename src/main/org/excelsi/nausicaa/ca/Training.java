package org.excelsi.nausicaa.ca;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Function;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.stream.Collectors;


public final class Training {
    public static List<Initializer> of(Initializer... initializers) {
        return Arrays.asList(initializers);
    }

    public static List<Initializer> file(String filename, Function<String,Initializer> read) throws IOException {
        return Files.lines(Paths.get(filename)).map(read).collect(Collectors.toList());
    }

    private Training() {}
}
