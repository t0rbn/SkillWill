import React from 'react'
import { connect } from 'react-redux'

class SkillItemEditor extends React.Component {
	constructor(props) {
		super(props)
	}

	editSkill() {

	}

	render() {
		const {
			name,
			skillLevel,
			willLevel,
			editSkill,
			deleteSkill
		} = this.props
		return (
			<div class="skill-item-editor">
				<input
					class="skill-item-editor__skill-editor"
					name={`skillLevel_${name}`}
					type="range" value={skillLevel}
					max="3"
					onChange={(e) => editSkill(name, e.target.value, willLevel)} />
				<input
					class="skill-item-editor__will-editor"
					name={`willLevel_${name}`}
					type="range" value={willLevel}
					max="3"
					onChange={(e) => editSkill(name, skillLevel, e.target.value)} />
				<div class="skill-item-editor__delete delete" onClick={() => deleteSkill(name)}></div>
			</div>
		)
	}
}

class SkillItem extends React.Component {
	constructor(props) {
		super(props)
		this.state = {
			skillLevel: this.props.skill.skillLevel,
			willLevel: this.props.skill.willLevel
		}
		console.log('const const')
		this.editSkill = this.editSkill.bind(this)
	}

	editSkill(name, skillLevel, willLevel) {
		this.setState({
			skillLevel,
			willLevel
		})
		this.props.editSkill(name, skillLevel, willLevel)
	}

	render() {
		const {
			key,
			skill: {
				name
			}
		} = this.props
		const {
			skillLevel,
			willLevel
		} = this.state
		return (
			<li key={key} class="skill-item">
				<p class="skill-name">{name}</p>
				<div class="skill-level">
					<div class="level">
						<div class={`skillBar levelBar levelBar--${skillLevel}`}></div>
					</div>
					<div class="level">
						<div class={`willBar levelBar levelBar--${willLevel}`}></div>
					</div>
					{
						this.props.isSkillEditActive
							? <SkillItemEditor editSkill={this.editSkill} name={name} skillLevel={skillLevel} willLevel={willLevel} />
							: ""
					}
				</div>
			</li>
		)
	}
}
function mapStateToProps(state) {
	return {
		isSkillEditActive: state.isSkillEditActive
	}
}

export default connect(mapStateToProps)(SkillItem)