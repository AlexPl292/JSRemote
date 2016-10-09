package com.tsystems.javaschool.services;

import com.tsystems.javaschool.entities.Backer;

/**
 * Created by alex on 08.10.16.
 */
public interface Service {
    Boolean setUp(Backer backer);
    String generate(Backer backer);
}
