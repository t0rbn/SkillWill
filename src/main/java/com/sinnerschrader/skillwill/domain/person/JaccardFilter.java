package com.sinnerschrader.skillwill.domain.person;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Get n Persons that are similar to the reference person
 * similar = high jaccard index when comparing skill sets
 *
 * @author torree
 */
public class JaccardFilter {

	Person reference;

	public JaccardFilter(Person reference) {
		this.reference = reference;
	}

	public List<Person> getFrom(List<Person> all, int max) {
		return all.stream().sorted(new JaccardIndexComparator(reference)).limit(max).collect(Collectors.toList());
	}

	private static double getJaccardIndex(Person a, Person b) {
		Set<String> aSkillNames = a.getSkills().stream().map(s -> s.getName()).collect(Collectors.toSet());
		Set<String> bSkillNames = b.getSkills().stream().map(s -> s.getName()).collect(Collectors.toSet());

		Set<String> intersection = aSkillNames.stream().filter(s -> bSkillNames.contains(s)).collect(Collectors.toSet());
		Set<String> union = aSkillNames;
		union.addAll(bSkillNames);

		return (double) intersection.size() / (double) union.size();
	}

	private class JaccardIndexComparator implements Comparator<Person> {

		Person reference;

		public JaccardIndexComparator(Person reference) {
			this.reference = reference;
		}

		@Override
		public int compare(Person a, Person b) {
			return -1 * Double.compare(getJaccardIndex(a, reference), getJaccardIndex(b, reference));
		}
	}

}