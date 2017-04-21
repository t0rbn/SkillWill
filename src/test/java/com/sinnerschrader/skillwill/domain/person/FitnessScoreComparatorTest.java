package com.sinnerschrader.skillwill.domain.person;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Unit test for FitnessScoreComparator
 *
 * @author torree
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class FitnessScoreComparatorTest {

  private Person pGood;
  private Person pBad;
  private FitnessScoreComparator comparator;

  @Autowired
  private FitnessScoreProperties fitnessScoreProperties;

  @Before
  public void init() {
    pGood = new Person("foobar");
    pGood.addUpdateSkill("skill 1", 3, 2);
    pGood.addUpdateSkill("skill 2", 2, 3);

    pBad = new Person("barfoo");
    pBad.addUpdateSkill("skill 1", 0, 1);
    pBad.addUpdateSkill("skill 2", 1, 0);

    List<String> searchItems = new ArrayList<>();
    searchItems.add("skill 1");
    searchItems.add("skill 2");

    comparator = new FitnessScoreComparator(searchItems, fitnessScoreProperties);
  }

  @Test
  public void testKeepOrder() {
    List<Person> toSort = new ArrayList<>();
    toSort.add(pGood);
    toSort.add(pBad);

    // Double input order
    assertEquals(pGood, toSort.get(0));
    assertEquals(pBad, toSort.get(1));

    toSort.sort(comparator);

    assertEquals(pGood, toSort.get(0));
    assertEquals(pBad, toSort.get(1));
  }

  @Test
  public void testReorder() {
    List<Person> toSort = new ArrayList<>();
    toSort.add(pBad);
    toSort.add(pGood);

    // Check input order
    assertEquals(pBad, toSort.get(0));
    assertEquals(pGood, toSort.get(1));

    toSort.sort(comparator);

    assertEquals(pGood, toSort.get(0));
    assertEquals(pBad, toSort.get(1));
  }

}
