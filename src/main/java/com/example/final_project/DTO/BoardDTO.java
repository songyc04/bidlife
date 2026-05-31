package com.example.final_project.DTO;

import lombok.*;

@Getter
@Setter
@ToString
public class BoardDTO {
   private Long id;
   private String boardWriter;
   private String boardPass;
   private String boardTitle;
   private String boardContent;
   private Integer boardHits;
   private String boardCreatedAt;
}
