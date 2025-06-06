package com.campfiredev.growtogether.chat.controller;

import com.campfiredev.growtogether.chat.ChatManager;
import com.campfiredev.growtogether.chat.dto.ChatMessageDto;
import com.campfiredev.growtogether.chat.dto.SliceMessageDto;
import com.campfiredev.growtogether.chat.service.ChatService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ChatController {

  private final ChatService chatService;
  private final RedisTemplate<String, Object> redisTemplate;
  private final ObjectMapper objectMapper;
  private final ChatManager chatManager;
  private final SimpMessagingTemplate messagingTemplate;

  @MessageMapping("/study/{studyId}/send")
  public void sendMessage(@DestinationVariable String studyId, @Payload ChatMessageDto chatMessageDto) {
    chatMessageDto.setDate(LocalDateTime.now());

    try {
      if(chatMessageDto.getTo() == null || chatMessageDto.getTo().isEmpty()) {
        redisTemplate.opsForList()
            .leftPush("chat" + studyId, objectMapper.writeValueAsString(chatMessageDto));
      }
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    if (chatMessageDto.getTo() != null && !chatMessageDto.getTo().isEmpty()) {
      Set<String> recipients = new HashSet<>(chatMessageDto.getTo());
      recipients.add(chatMessageDto.getSender());

      for (String recipient : recipients) {
        sendDirectMessage(recipient, studyId, chatMessageDto);
      }
    } else {
      messagingTemplate.convertAndSend("/topic/study/" + studyId, chatMessageDto);
    }
  }


  public void sendDirectMessage(String username, String studyId, ChatMessageDto message) {
    List<String> sessionIds = chatManager.getSessionIdsByUsername(username);
    for (String sessionId : sessionIds) {
      SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
      headerAccessor.setSessionId(sessionId);
      headerAccessor.setLeaveMutable(true);
      messagingTemplate.convertAndSendToUser(sessionId, "/queue/private/" + studyId, message,headerAccessor.getMessageHeaders());
    }
  }

  @MessageMapping("/study/{studyId}/participants")
  public void sendParticipants(@DestinationVariable String studyId) {
    Set<String> participants = chatManager.getParticipants(studyId);
    messagingTemplate.convertAndSend("/topic/study/" + studyId + "/participants", participants);
  }


  @GetMapping("/study/{studyId}/chat")
  public SliceMessageDto getChatMessages(@PathVariable Long studyId,
      @RequestParam(required = false) Integer lastIndex,
      @RequestParam(required = false) Integer size) {
    return chatService.getChatMessage(studyId, lastIndex, LocalDateTime.now(), size);
  }

}
