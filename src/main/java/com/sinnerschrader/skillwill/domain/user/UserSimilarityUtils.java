package com.sinnerschrader.skillwill.domain.user;

import com.sinnerschrader.skillwill.domain.skills.UserSkill;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class UserSimilarityUtils {

  public static List<User> findSimilar(User user, Collection<User> candidates, Integer count) {
    if (count != null && count <= 0) {
      throw new IllegalArgumentException("count must be positive or null");
    }

    return candidates.stream()
      .filter(candidate -> !candidate.getEmail().equals(user.getEmail()))
      .sorted(new JaccardDistanceComparator(user))
      .limit(count != null ? count : Long.MAX_VALUE)
      .collect(Collectors.toList());
  }

  private static double jaccardDistance(User userA, User userB) {
    var skillsNamesA = userA.getSkills().stream().map(UserSkill::getName).collect(Collectors.toSet());
    var skillsNamesB = userB.getSkills().stream().map(UserSkill::getName).collect(Collectors.toSet());

    double intersectionSize = skillsNamesA.stream().filter(skillsNamesB::contains).count();
    double unionSize = skillsNamesA.size() + skillsNamesB.size() - intersectionSize;

    return 1 - intersectionSize / unionSize;
  }

  private static class JaccardDistanceComparator implements Comparator<User> {

    private final User referenceUser;

    private JaccardDistanceComparator(User reference) {
      this.referenceUser = reference;
    }

    @Override
    public int compare(User userA, User userB) {
      var distanceA = jaccardDistance(userA, referenceUser);
      var distanceB = jaccardDistance(userB, referenceUser);
      return Double.compare(distanceA, distanceB);
    }

  }

}
