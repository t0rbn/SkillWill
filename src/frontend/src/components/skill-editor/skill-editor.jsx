import React from 'react'
import { SkillLegend, SkillLegendItem } from '../skill-legend/skill-legend'
import SkillItem from '../skill-item/skill-item'
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
		const { handleEdit, handleDelete } = this.props
		const { noUserSkills } = this.state

		return (
			<div className="skill-editor">
				<div className="skill-listing">
					{noUserSkills.length > 0 && (
						<div className="listing-header">
							<SkillLegend>
								<SkillLegendItem title="Name" wide />
								<SkillLegendItem title="Skill level" />
								<SkillLegendItem title="Will level" />
							</SkillLegend>
						</div>
					)}
					<ul className="skills-list">
						{noUserSkills.map((skill, i) => {
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
			</div>
		)
	}
}

function mapStateToProps(state) {
	return {
		skills: state.skills,
		user: state.user,
	}
}

export default connect(mapStateToProps)(SkillEditor)
