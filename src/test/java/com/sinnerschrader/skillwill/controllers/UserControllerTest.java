package com.sinnerschrader.skillwill.controllers;

import com.sinnerschrader.skillwill.misc.OAuthHelper;
import com.sinnerschrader.skillwill.services.SkillService;
import com.sinnerschrader.skillwill.services.UserService;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

public class UserControllerTest {

  @Rule
  public MockitoRule mockito = MockitoJUnit.rule();

  @Mock
  UserService userService;

  @Mock
  SkillService skillService;

  @Mock
  OAuthHelper oAuthHelper;

  @InjectMocks
  UserController userController;

  @Test
  public void shouldDeleteUser() {
    String givenUserId = UUID.randomUUID().toString();

    userController.deleteUserById(givenUserId);

    verify(userService).deleteUserById(givenUserId);
  }

  @Test
  public void shouldReturnResponseOnDelete() {
    //given
    String givenUserId = UUID.randomUUID().toString();

    //when
    ResponseEntity response = userController.deleteUserById(givenUserId);

    //then
    assertThat(response).isInstanceOf(ResponseEntity.class);
    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

  }


}
