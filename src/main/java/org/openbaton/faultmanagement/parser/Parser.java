package org.openbaton.faultmanagement.parser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by mob on 28.10.15.
 */
public class Parser {
    private static Gson mapper= new GsonBuilder().setPrettyPrinting().create();

    public static Gson getMapper(){
        return mapper;
    }
}
