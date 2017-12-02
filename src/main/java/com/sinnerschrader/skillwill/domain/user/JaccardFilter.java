package com.sinnerschrader.skillwill.domain.user;

import com.sinnerschrader.skillwill.domain.skills.UserSkill;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Get n Persons that are similar to the reference person
 * similar = high jaccard index when comparing skill sets
 *
 * @author torree
 */
public class JaccardFilter {

  private final User reference;

  public JaccardFilter(User reference) {
    this.reference = reference;
  }

  private static double getJaccardIndex(User a, User b) {
    Collection<UserSkill> aSkills = a.getSkillsExcludeHidden();
    Collection<UserSkill> bSkills = b.getSkillsExcludeHidden();

    double intersectionCount = aSkills.stream().filter(s -> b.getSkill(s.getName()) != null).count();
    double unionCount = aSkills.size() + bSkills.size() - intersectionCount;

    return intersectionCount / unionCount;
  }

  public List<User> getFrom(List<User> all, int max) {
    return all.stream()
        .sorted(new JaccardIndexComparator(reference))
        .limit(max)
        .collect(Collectors.toList());
  }

  private static class JaccardIndexComparator implements Comparator<User> {

    private final User reference;

    public JaccardIndexComparator(User reference) {
      this.reference = reference;
    }

    @Override
    public int compare(User a, User b) {
      return -1 * Double.compare(getJaccardIndex(a, reference), getJaccardIndex(b, reference));
    }
  }

}
