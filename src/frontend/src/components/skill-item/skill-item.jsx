import React from 'react'

export default class SkillItem extends React.Component {
	constructor(props) {
		super(props)
	}

	levelIcons = ['💩', '🙂', '😀', '😬']

	render() {
		const {
			key,
			skill: {
				name,
				skillLevel,
				willLevel
			}
		} = this.props
		return (
			<li key={key} class="skill-item">
				<p class="skill-name">{name}</p>
				<p class="level">Skill:
					<span>{this.levelIcons[skillLevel]}</span>
				</p>
				<p class="level">Will:
					<span>{this.levelIcons[willLevel]}</span>
				</p>
			</li>
		)
	}
}
