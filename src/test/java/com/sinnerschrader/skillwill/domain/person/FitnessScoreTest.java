package com.sinnerschrader.skillwill.domain.person;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
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
  public void onlySearchedTest() {
    Person person = new Person("foobar");
    person.addUpdateSkill("Java", 3, 3);
    person.addUpdateSkill("AEM", 3, 3);

    List<String> searchItems = new ArrayList<>();
    searchItems.add("Java");
    searchItems.add("AEM");
    assertTrue(1.0 == new FitnessScore(person, searchItems, fitnessScoreProperties).getValue());
  }

  @Test
  public void noSearchedTest() {
    Person person = new Person("foobar");
    person.addUpdateSkill("Java", 3, 3);
    person.addUpdateSkill("AEM", 3, 3);

    List<String> searchItems = new ArrayList<>();
    searchItems.add("Ruby");
    assertTrue(0.0 == new FitnessScore(person, searchItems, fitnessScoreProperties).getValue());
  }

  @Test
  public void maximalScoreText() {
    Person person = new Person("foobar");
    person.addUpdateSkill("Java", 3, 3);
    person.addUpdateSkill("AEM", 3, 3);
    person.addUpdateSkill("Foo", 0, 0);
    person.addUpdateSkill("Bar", 0, 0);

    List<String> searchItems = new ArrayList<>();
    searchItems.add("Java");
    searchItems.add("AEM");
    assertTrue(1.0 == new FitnessScore(person, searchItems, fitnessScoreProperties).getValue());
  }

  @Test
  public void minimalScoreText() {
    Person person = new Person("foobar");
    person.addUpdateSkill("Java", 0, 0);
    person.addUpdateSkill("AEM", 0, 0);
    person.addUpdateSkill("Foo", 3, 3);
    person.addUpdateSkill("Bar", 3, 3);

    List<String> searchItems = new ArrayList<>();
    searchItems.add("Java");
    searchItems.add("AEM");
    assertTrue(0.0 == new FitnessScore(person, searchItems, fitnessScoreProperties).getValue());
  }

  @Test
  public void realisticTest() {
    Person person = new Person("foobar");
    person.addUpdateSkill("Java", 2, 3);
    person.addUpdateSkill("AEM", 2, 2);
    person.addUpdateSkill("Foo", 1, 3);
    person.addUpdateSkill("Bar", 1, 0);

    List<String> searchItems = new ArrayList<>();
    searchItems.add("Java");
    searchItems.add("AEM");
    searchItems.add("Bar");
    assertTrue(0.5278 == new FitnessScore(person, searchItems, fitnessScoreProperties).getValue());
  }

}
