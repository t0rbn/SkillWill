import React from 'react'
import { Link } from 'react-router'
import SkillItem from '../skill-item/skill-item.jsx'

export default class User extends React.Component {
	constructor(props) {
		super(props)
		this.state = {
			skillsToShow: this.getSkillsToShow(this.props.searchTerms),
		}
		this.handleClick = this.handleClick.bind(this)
		this.getSkillsToShow = this.getSkillsToShow.bind(this)
	}

	getSkillsToShow(searchTerms) {
		const { user: { skills } } = this.props
		return skills.filter(skill => searchTerms.indexOf(skill.name) > -1)
	}

	handleClick() {
		document.body.classList.add('layer-open')
	}

	componentWillReceiveProps(nextProps) {
		if (
			nextProps.searchTerms &&
			nextProps.searchTerms !== this.props.searchTerms
		) {
			this.setState({
				skillsToShow: this.getSkillsToShow(nextProps.searchTerms),
			})
		}
	}

	render() {
		const { id, firstName, lastName, title, location } = this.props.user

		return (
			<Link
				to={`/profile/${id}`}
				activeClassName="active"
				id={id}
				onClick={this.handleClick}>
				<ul className="user">
					<li className="info">
						<span className="name">{`${firstName} ${lastName}`}</span>
						<span className="id">{id}</span>
						<span className="department">{title}</span>
					</li>
					<li className="location">{location}</li>
					<li className="skills">
						<ul className="skills-list">
							{this.state.skillsToShow.map(skill => {
								return <SkillItem key={skill.name} skill={skill} />
							})}
						</ul>
					</li>
				</ul>
			</Link>
		)
	}
}
