import React from 'react'
import { Router, Route, Link, browserHistory } from 'react-router'
import Levels from '../level/level.jsx'

export default class User extends React.Component {
	constructor(props) {
		super(props)
		this.state = {
			skillsToShow: this.getSkillsToShow()
		}
		this.handleClick = this.handleClick.bind(this)
		this.getSkillsToShow = this.getSkillsToShow.bind(this)
	}

	getSkillsToShow() {
		const { searchTerms, user: { skills } } = this.props
		return skills.filter(skill => searchTerms.indexOf(skill.name) > -1)
	}

	handleClick() {
		document.body.classList.add('layer-open')
	}

	render() {
		const {
			id,
			firstName,
			lastName,
			title,
			location
		} = this.props.user

		return (
			<ul class="user">
				<li class="info">
					<Link
						class="name"
						to={`/profile/${id}`}
						activeClassName="active"
						id={id}
						onClick={this.handleClick}>
						{`${firstName} ${lastName}`}
					</Link>
					<span class="id">{id}</span>
					<span class="department">{title}</span>
				</li>
				<li class="location">{location}</li>
				<li class="skills">
					<ul class="skills-list">
						{this.state.skillsToShow.map((user, i) => {
							return (
								<li key={i} class="skill-item">
									<Levels skill={user} />
								</li>
							)
						})}
					</ul>
				</li>
			</ul>
		)
	}
}
