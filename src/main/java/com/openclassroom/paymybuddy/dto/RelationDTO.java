package com.openclassroom.paymybuddy.dto;

import lombok.Getter;

@Getter
public class RelationDTO {
    private Long id;
    private String name;

    public RelationDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

}