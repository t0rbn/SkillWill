package com.sinnerschrader.skillwill.domain.person;

import com.sinnerschrader.skillwill.domain.user.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class UserTest {

  private User user;

  @Before
  public void init() {
    user = new User("foobar");
    user.addUpdateSkill("skillname", 2, 3, false);
  }

  @Test
  public void testAddUpdateNewSkill() {
    user.addUpdateSkill("new skill", 2, 3, false);
    assertEquals(2, user.getSkills().size());
  }

  @Test
  public void testAddUpdateKnownSkill() {
    user.addUpdateSkill("skillname", 0, 1, false);
    assertEquals(1, user.getSkills().size());
    assertEquals(0, user.getSkills().get(0).getSkillLevel());
    assertEquals(1, user.getSkills().get(0).getWillLevel());
  }

}
