import React from 'react'
import { SkillLegend, SkillLegendItem } from '../skill-legend/skill-legend'
import SkillItem from '../skill-item/skill-item'
import TicketNotice from '../search/ticket-notice/ticket-notice'
import { connect } from 'react-redux'

class SkillEditor extends React.Component {
	constructor(props) {
		super(props)
		this.state = {
			noUserSkills: [],
		}
	}

	componentWillReceiveProps(nextProps) {
		if (this.props.user.skills === nextProps.user.skills) {
			this.filterNoUserSkills(nextProps.skills, this.props.user.skills)
		}
	}

	filterNoUserSkills(searchedSkills, userSkills) {
		let userSkillNames = []
		let tempNoUserSkills = []

		userSkills.forEach(userSkill => {
			userSkillNames.push(userSkill.name)
		})

		searchedSkills
			.filter(searchedSkill => userSkillNames.indexOf(searchedSkill) === -1)
			.map(searchedSkill => {
				tempNoUserSkills.push({
					name: searchedSkill,
					skillLevel: 0,
					willLevel: 0,
					mentor: false,
				})
			})

		this.setState({
			noUserSkills: tempNoUserSkills,
		})
	}

	render() {
		const { handleEdit, handleDelete, searchTerms } = this.props
		const { noUserSkills } = this.state
		const noUserSkillsSorted = noUserSkills.sort((skillA, skillB) =>
			skillA.name.localeCompare(skillB.name)
		)

		return (
			<div className="skill-editor">
				<div className="skill-listing">
					{noUserSkillsSorted.length > 0 && (
						<div className="listing-header">
							<SkillLegend>
								<SkillLegendItem title="Name" wide />
								<div className="skill-legend__item--skills">
									<SkillLegendItem title="Skill level" withTooltip="skill" />
									<SkillLegendItem title="Will level" withTooltip="will" />
								</div>
							</SkillLegend>
						</div>
					)}
					<ul className="skills-list">
						{noUserSkillsSorted.map((skill, i) => {
							return (
								<SkillItem
									skill={skill}
									editSkill={handleEdit}
									deleteSkill={handleDelete}
									key={i}
								/>
							)
						})}
					</ul>
				</div>
				{searchTerms.length > 0 &&
					noUserSkills.length === 0 && (
						<TicketNotice
							title={`Sorry, there isn't any "${searchTerms}" skill you can add...`}
							subtitle="Submit your suggestion!"
						/>
					)}
			</div>
		)
	}
}

function mapStateToProps(state) {
	return {
		skills: state.skills,
		user: state.currentUser,
	}
}

export default connect(mapStateToProps)(SkillEditor)
