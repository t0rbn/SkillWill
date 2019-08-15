package com.sinnerschrader.skillwill.controllers;

import com.sinnerschrader.skillwill.domain.user.User;
import com.sinnerschrader.skillwill.repositories.UserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerIntegrationTest {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  UserRepository users;

  @Test
  public void shouldDeleteUser() throws Exception {

    var savedUser = users.save(new User("willi@somewhere.com", "willi"));

    mockMvc.perform(delete("/users/" + savedUser.getId()).with(user("any")).with(csrf())).andExpect(status().isOk());
  }
}
