import React from 'react'
import { connect } from 'react-redux'

class SkillItemEditor extends React.Component {
	constructor(props) {
		super(props)
	}

	render() {
		const {
			name,
			skillLevel,
			willLevel,
			editSkill,
			deleteSkill,
			isMentor,
		} = this.props

		return (
			<div className="skill-item-editor">
				<label className="tutorCheckbox-label">
					<input
						className="tutorCheckbox"
						type="checkbox"
						name="tutorFlag"
						value="true"
						checked={isMentor}
						onChange={() => editSkill(name, skillLevel, willLevel, !isMentor)}
					/>
					<span className="tutorCheckbox-labelText">Tutor</span>
				</label>
				<input
					className="skill-item-editor__skill-editor"
					name={`skillLevel_${name}`}
					type="range"
					value={skillLevel}
					max="3"
					onChange={e => editSkill(name, e.target.value, willLevel, isMentor)}
				/>
				<input
					className="skill-item-editor__will-editor"
					name={`willLevel_${name}`}
					type="range"
					value={willLevel}
					max="3"
					onChange={e => editSkill(name, skillLevel, e.target.value, isMentor)}
				/>
				<div
					className="skill-item-editor__delete delete"
					onClick={() => deleteSkill(name)}
				/>
			</div>
		)
	}
}

class SkillItem extends React.Component {
	constructor(props) {
		super(props)
		this.state = {
			skillLevel: this.props.skill.skillLevel,
			willLevel: this.props.skill.willLevel,
			isMentor: this.props.skill.mentor,
			renderSkill: true,
			hasEdited: false,
			hasZeroLevel: false,
			singleSkillEditActive: false,
		}
		this.editSkill = this.editSkill.bind(this)
		this.deleteSkill = this.deleteSkill.bind(this)
	}

	componentWillMount() {
		if (this.props.skill.skillLevel === 0 && this.props.skill.willLevel === 0) {
			this.setState({ hasZeroLevel: !this.state.hasZeroLevel })
		}
	}

	componentWillUnmount() {
		this.setState({
			singleSkillEditActive: false,
		})
	}

	addSkill(name) {
		this.editSkill(name, 3, 3, false)

		this.setState({
			singleSkillEditActive: true,
			hasZeroLevel: !this.state.hasZeroLevel,
		})
	}

	editSkill(name, skillLevel, willLevel, isMentor) {
		this.setState({
			skillLevel,
			willLevel,
			isMentor,
		})
		this.props.editSkill(name, skillLevel, willLevel, isMentor)

		//trigger CSS animation
		if (!this.state.hasEdited) {
			this.setState({
				hasEdited: true,
			})
			setTimeout(
				function() {
					this.setState({
						hasEdited: false,
					})
				}.bind(this),
				3500
			)
		}
	}

	deleteSkill(name) {
		if (!this.state.singleSkillEditActive) {
			this.setState({
				renderSkill: false,
			})
		} else {
			this.setState({
				singleSkillEditActive: false,
				skillLevel: 0,
				willLevel: 0,
				isMentor: false,
				hasZeroLevel: !this.state.hasZeroLevel,
			})
		}
		this.props.deleteSkill(name)
	}

	render() {
		const { key, skill: { name }, isSkillEditActive } = this.props
		const {
			skillLevel,
			willLevel,
			isMentor,
			singleSkillEditActive,
			hasZeroLevel,
			hasEdited,
		} = this.state

		const isLayerActive = document.body.classList.contains('layer-open')
			? ' skill-level--layerWidth'
			: ''

		return (
			this.state.renderSkill && (
				<li
					key={key}
					className="skill-item"
					data-edited={hasEdited}
					data-empty={hasZeroLevel}>
					<p className="skill-name">
						<span>{name}</span>
					</p>
					{isMentor && (
						<span className="tutorCheckbox-labelText tutorlabel">Tutor</span>
					)}
					<div className={`skill-level${isLayerActive}`}>
						<div className="level">
							<div className={`skillBar levelBar levelBar--${skillLevel}`} />
						</div>
						<div className="level">
							<div className={`willBar levelBar levelBar--${willLevel}`} />
						</div>
						{(isSkillEditActive || singleSkillEditActive) && (
								<SkillItemEditor
									editSkill={this.editSkill}
									deleteSkill={this.deleteSkill}
									name={name}
									skillLevel={skillLevel}
									willLevel={willLevel}
									isMentor={isMentor}
								/>
							)}
						{hasZeroLevel && (
							<div
								className="skill-item-editor__add add"
								onClick={() => this.addSkill(name)}
							/>
						)}
					</div>
				</li>
			)
		)
	}
}
function mapStateToProps(state) {
	return {
		isSkillEditActive: state.isSkillEditActive,
	}
}

export default connect(mapStateToProps)(SkillItem)
