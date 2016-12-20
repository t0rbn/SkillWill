package com.sinnerschrader.skillwill.mock;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.sinnerschrader.skillwill.person.Person;
import com.sinnerschrader.skillwill.repositories.PersonRepository;
import com.sinnerschrader.skillwill.repositories.SkillsRepository;
import com.sinnerschrader.skillwill.skills.KnownSkill;
import com.sinnerschrader.skillwill.skills.PersonalSkill;

/**
 * Reads Mock Data from files specified in application.properties and
 * inserts it into DB. Handle with care, as this could delete all your data.
 *
 * @author torree
 *
 */
@Component
public class MockData {

	private static Logger logger = LoggerFactory.getLogger(MockData.class);

	@Autowired
	private SkillsRepository skillRepo;

	@Autowired
	private PersonRepository personRepo;

	@Value("${mockInit}")
	private String initmock;

	@Value("${mockSkillFilePath}")
	private String skillsPath;

	@Value("${mockPersonsFilePath}")
	private String personsPath;

	@PostConstruct
	public void init() throws FileNotFoundException, IOException {
		if (!initmock.equals("true")) {
			return;
		}

		logger.warn("Mocking is enabled, this will overwrite all data in your DB");
		personRepo.deleteAll();
		skillRepo.deleteAll();

		InputStreamReader skillsIS = new InputStreamReader(getClass().getResourceAsStream("/mockdata/" + skillsPath));
		InputStreamReader personsIS = new InputStreamReader(getClass().getResourceAsStream("/mockdata/" + personsPath));

		// insert skills
		// skills file is a list of known Skills
		// new line -> new skill
		BufferedReader br = new BufferedReader(skillsIS);
		String line = "";
		while ((line = br.readLine()) != null) {
			logger.info("Inserting new skill: " + line);
			skillRepo.insert(new KnownSkill(line));
		}

		// insert person
		// Structure:
		// 	* 1st line -> id
		//  * 2nd - nth line -> skillname, skilllevel, willlevel (i.E "Java, 3, 1")
		//  * separator "====" (exactly FOUR equal signs)
		Person curr = null;
		br = new BufferedReader(personsIS);
		line = "";
		while ((line = br.readLine()) != null) {
			if (line.equals("====")) {
				logger.info("Inserting new person: " + curr.getId());
				personRepo.insert(curr);
				curr = null;
			} else if (curr == null) {
				curr = new Person(line);
			} else {
				String[] split = line.split("\\s*,\\s*");
				curr.addUpdateSkill(new PersonalSkill(split[0], Integer.parseInt(split[1]), Integer.parseInt(split[2])));
			}
		}
	}

}
