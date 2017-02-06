package com.sinnerschrader.skillwill.domain.person;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Compare Persons by the jaccard index of their skill sets
 *
 */
public class JaccardFilter {

	public static List<Person> getSimilar(Person reference, List<Person> all, int max) {
		return null;
	}

	private static double getJaccardIndex(Person a, Person b) {
		Set<String> intersection = new HashSet<>(a.getSkills().stream().map(s -> s.getName()).filter(s -> b.getSkill(s) != null).collect(Collectors.toList()));

		Set<String> union = new HashSet<>(a.getSkills().stream().map(s -> s.getName()).collect(Collectors.toList()));
		union.addAll(b.getSkills().stream().map(s -> s.getName()).collect(Collectors.toList()));

		return (double) (intersection.size() / union.size());
	}

	private class JaccardIndexedPerson {

		Person person;
		double index;

		public  JaccardIndexedPerson(Person person, double index) {
			this.person = person;
			this.index = index;
		}

	}

}