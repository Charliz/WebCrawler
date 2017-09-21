package com.myproject.wordscounter;


import org.apache.ignite.IgniteException;
import org.apache.ignite.Ignition;

public class NodeStartup {

    public static void main(String[] args) throws IgniteException {
        // Start 'Server' Node
        Ignition.start("config/example-ignite.xml");
    }
}
