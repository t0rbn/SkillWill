package com.sinnerschrader.skillwill.services;

import com.sinnerschrader.skillwill.domain.user.FitnessScoreProperties;
import com.sinnerschrader.skillwill.exceptions.EmptyArgumentException;
import com.sinnerschrader.skillwill.exceptions.UserNotFoundException;
import com.sinnerschrader.skillwill.repositories.SkillRepository;
import com.sinnerschrader.skillwill.repositories.UserRepository;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

public class UserServiceTest {

  @Rule
  public MockitoRule mockito = MockitoJUnit.rule();

  @Mock
  UserRepository userRepository;

  @Mock
  SkillRepository skillRepository;

  @Mock
  FitnessScoreProperties fitnessProperties;

  @InjectMocks
  UserService userService;

  @Test
  public void shouldDeleteUser() {
    //given
    String givenId = "someId";

    //when
    userService.deleteUserById(givenId);

    //then
    verify(userRepository).deleteById(givenId);
  }

  @Test(expected = EmptyArgumentException.class)
  public void shouldRejectNullId() {
    userService.deleteUserById(null);
  }

  @Test(expected = EmptyArgumentException.class)
  public void shouldRejectEmptyId() {
    userService.deleteUserById("");
  }

  @Test(expected = UserNotFoundException.class)
  public void shouldRejectUnknownUser() {
    given(userRepository.findById("foobar")).willReturn(Optional.empty());
    userService.deleteUserById("foobar");
  }

}
