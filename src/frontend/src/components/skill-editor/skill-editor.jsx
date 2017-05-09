import React from 'react'
import { Router, Route, Link } from 'react-router'
import Editor from '../editor/editor.jsx'
import config from '../../config.json'
import Cookies from 'react-cookie'
import { connect } from 'react-redux'

export default class Skill extends React.Component {
	constructor(props) {
		super(props)
		this.state = {
			editorIsOpen: false,
			userId: undefined,
			skillLvl: 0,
			willLvl: 0
		}
		this.toggleEditor = this.toggleEditor.bind(this)
		this.checkSkillLvls = this.checkSkillLvls.bind(this)
		this.checkSkillLvls()
	}

	toggleEditor(e) {
		this.setState({
			editorIsOpen: !this.state.editorIsOpen
		})
	}

	// check if Skill is allready in Profile
	// if so, set current levels to Editor
	checkSkillLvls() {
		this.props.user.skills.map((skill) => {
			if (skill.name === this.props.skill.name) {
				this.setState({
					skillLvl: skill.skillLevel,
					willLvl: skill.willLevel
				})
			}
		})
	}

	render() {
		const { name, handleEdit } = this.props.skill
		const { editorIsOpen, skillLvl, willLvl } = this.state

		return (
			<ul class={`skill ${editorIsOpen ? "toggled" : ""}`}>
				<li class="name" onClick={this.toggleEditor}>
					{name}
				</li>
				<li class="add" onClick={this.toggleEditor}></li>
				{editorIsOpen ?
					<li class="editor-container">
						<Editor skillName={name}
							skillLvl={skillLvl}
							willLvl={willLvl}
							handleAccept={handleEdit}
							handleClose={this.toggleEditor}
							handleEdit={handleEdit} />
					</li>
					: ""
				}
			</ul>
		)
	}
}
