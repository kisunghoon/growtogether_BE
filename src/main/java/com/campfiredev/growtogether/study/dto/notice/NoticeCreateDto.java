package com.campfiredev.growtogether.study.dto.notice;

import com.campfiredev.growtogether.study.entity.Study;
import com.campfiredev.growtogether.study.entity.notice.NoticeEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class NoticeCreateDto {

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class Request{

    @NotBlank
    @Size(max = 30)
    private String title;

    @NotBlank
    private String content;

    public NoticeEntity toEntity(Study study){
      return NoticeEntity.builder()
          .title(title)
          .content(content)
          .study(study)
          .build();

    }
  }

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class Response{

    private Long id;

    private String title;

    private String content;

    public static Response fromEntity(NoticeEntity noticeEntity){
      return Response.builder()
          .id(noticeEntity.getId())
          .title(noticeEntity.getTitle())
          .content(noticeEntity.getContent())
          .build();
    }
  }

}
