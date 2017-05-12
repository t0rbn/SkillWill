import React from 'react'

export default class SkillItem extends React.Component {
	constructor(props) {
		super(props)
	}

	levelIcons = ['0', '1', '2', '⭐️']

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
				<div class="level">
					<div class={`skillBar levelBar levelBar--${skillLevel}`}></div>
				</div>
				<div class="level">
					<div class={`willBar levelBar levelBar--${willLevel}`}></div>
				</div>
			</li>
		)
	}
}
