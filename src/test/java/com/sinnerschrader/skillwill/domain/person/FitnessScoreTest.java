package com.sinnerschrader.skillwill.domain.person;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.sinnerschrader.skillwill.domain.skills.KnownSkill;
import com.sinnerschrader.skillwill.domain.user.FitnessScore;
import com.sinnerschrader.skillwill.domain.user.FitnessScoreProperties;
import com.sinnerschrader.skillwill.domain.user.User;
import java.util.ArrayList;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Partial unit tests for FitnessScore
 *
 * @author torree
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class FitnessScoreTest {

  @Autowired
  private FitnessScoreProperties fitnessScoreProperties;

  @Test
  public void testSearchedSkillsOnly() {
    User user = new User("foobar");
    user.addUpdateSkill("Java", 3, 3, false, false);
    user.addUpdateSkill("AEM", 3, 3, false, false);

    Collection<KnownSkill> searchItems = new ArrayList<>();
    searchItems.add(new KnownSkill("Java"));
    searchItems.add(new KnownSkill("AEM"));
    assertEquals(1.0, new FitnessScore(user, searchItems, fitnessScoreProperties).getValue(), 0);
  }

  @Test
  public void testNoSearchedSkills() {
    User user = new User("foobar");
    user.addUpdateSkill("Java", 3, 3, false, false);
    user.addUpdateSkill("AEM", 3, 3, false, false);

    Collection<KnownSkill> searchItems = new ArrayList<>();
    searchItems.add(new KnownSkill("Ruby"));
    assertEquals(0.0, new FitnessScore(user, searchItems, fitnessScoreProperties).getValue(), 0);
  }

  @Test
  public void testMaximumScore() {
    User user = new User("foobar");
    user.addUpdateSkill("Java", 3, 3, false, false);
    user.addUpdateSkill("AEM", 3, 3, false, false);
    user.addUpdateSkill("Foo", 0, 0, false, false);
    user.addUpdateSkill("Bar", 0, 0, false, false);

    Collection<KnownSkill> searchItems = new ArrayList<>();
    searchItems.add(new KnownSkill("Java"));
    searchItems.add(new KnownSkill("AEM"));
    assertEquals(1.0, new FitnessScore(user, searchItems, fitnessScoreProperties).getValue(), 0);
  }

  @Test
  public void testMinimalScore() {
    User user = new User("foobar");
    user.addUpdateSkill("Java", 0, 0, false, false);
    user.addUpdateSkill("AEM", 0, 0, false, false);
    user.addUpdateSkill("Foo", 3, 3, false, false);
    user.addUpdateSkill("Bar", 3, 3, false, false);

    Collection<KnownSkill> searchItems = new ArrayList<>();
    searchItems.add(new KnownSkill("Java"));
    searchItems.add(new KnownSkill("AEM"));
    assertEquals(0.0, new FitnessScore(user, searchItems, fitnessScoreProperties).getValue(), 0);
  }

  @Test
  public void testRealisticScore() {
    User user = new User("foobar");
    user.addUpdateSkill("Java", 2, 3, false, false);
    user.addUpdateSkill("AEM", 2, 2, false, false);
    user.addUpdateSkill("Foo", 1, 3, false, false);
    user.addUpdateSkill("Bar", 1, 0, false, false);

    Collection<KnownSkill> searchItems = new ArrayList<>();
    searchItems.add(new KnownSkill("Java"));
    searchItems.add(new KnownSkill("AEM"));
    searchItems.add(new KnownSkill("Bar"));
    assertEquals(0.5278, new FitnessScore(user, searchItems, fitnessScoreProperties).getValue(), 0);
  }

}
