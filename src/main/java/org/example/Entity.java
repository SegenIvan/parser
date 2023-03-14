package org.example;

import lombok.Data;

import java.io.Serializable;

@Data
public class Entity implements Serializable {
    private String first;
    private String second;
    private String third;
}
