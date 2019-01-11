package org.excelsi.nausicaa.ca;


import java.util.Random;
import java.io.DataOutputStream;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.IOException;
import com.google.gson.JsonElement;


public interface Initializer extends Humanizable {
    void init(Plane plane, Rule rule, Random random);
    void write(DataOutputStream dos) throws IOException;
    void write(PrintWriter w);
    JsonElement toJson();
}
