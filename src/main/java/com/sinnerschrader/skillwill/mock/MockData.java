package com.sinnerschrader.skillwill.mock;

import com.sinnerschrader.skillwill.domain.person.Person;
import com.sinnerschrader.skillwill.repositories.PersonRepository;
import com.sinnerschrader.skillwill.repositories.SkillsRepository;
import com.sinnerschrader.skillwill.services.LdapService;
import com.sinnerschrader.skillwill.domain.skills.KnownSkill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Reads Mock Data from files specified in application.properties and
 * inserts it into DB. Handle with care, as this could delete all your data.
 *
 * @author torree
 */
@Component
public class MockData {

	private static Logger logger = LoggerFactory.getLogger(MockData.class);

	@Autowired
	private SkillsRepository skillRepo;

	@Autowired
	private PersonRepository personRepo;

	@Autowired
	private LdapService ldapService;

	@Value("${mockInit}")
	private String initmock;

	@Value("${mockSkillFilePath}")
	private String skillsPath;

	@Value("${mockPersonsFilePath}")
	private String personsPath;

	@PostConstruct
	public void init() throws IOException {
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
			skillRepo.insert(new KnownSkill(line));
			logger.info("Successfully inserted mock skill {}", line);
		}

		// insert person
		// Structure:
		// 	* 1st line -> id
		//  * 2nd - nth line -> skillname, skilllevel, willlevel (i.E "Java, 3, 1")
		//  * separator "====" (exactly FOUR equal signs)
		Person curr = null;
		br = new BufferedReader(personsIS);
		while ((line = br.readLine()) != null) {
			if (line.equals("====")) {
				personRepo.insert(curr);
				ldapService.syncUser(curr);
				logger.info("Successfully inserted mock person {}", curr.getId());
				curr = null;
			} else if (curr == null) {
				curr = new Person(line);
			} else {
				String[] split = line.split("\\s*,\\s*");
				curr.addUpdateSkill(split[0], Integer.parseInt(split[1]), Integer.parseInt(split[2]));
			}
		}
	}

}
