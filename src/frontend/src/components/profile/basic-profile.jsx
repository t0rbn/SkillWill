import React from 'react'
import User from "../user/user.jsx"
import config from '../../config.json'
import Editor from '../editor/editor.jsx'
import { Link } from 'react-router'
import SkillItem from '../skill-item/skill-item.jsx'

export default class BasicProfile extends React.Component {
	constructor(props) {
		super(props)
		this.state = {
			shouldShowAllSkills: this.props.shouldShowAllSkills,
			infoLayerAt: this.props.openLayerAt,
			showMoreLabel: "Mehr",
			editLayerAt: null,
			numberOfSkillsToShow: 6,
			sortedSkills: this.sortSkills('name'),
			topWills: this.sortSkills('willLevel', 'desc')
		}
		this.showAllSkills = this.showAllSkills.bind(this)
		this.getAvatarColor = this.getAvatarColor.bind(this)
		this.sortSkills = this.sortSkills.bind(this)
	}

	showAllSkills(e) {
		e.preventDefault()
		const { shouldShowAllSkills, numberOfSkillsToShow } = this.state
		this.setState({
			shouldShowAllSkills: !shouldShowAllSkills,
			numberOfSkillsToShow: numberOfSkillsToShow === 6 ? Infinity : 6
		})
		e.target.classList.toggle("open")
	}

	getAvatarColor() {
		const colors = ["blue", "red", "green"]
		let index = this.props.user.id
			.toLowerCase()
			.split('')
			.map(c => c.charCodeAt(0))
			.reduce((a, b) => a + b)
		return colors[index % colors.length]

	}

	sortSkills(criterion, order = 'asc') {
		const skills = [...this.props.user.skills]
		return skills
			.sort((a, b) => {
				if (order === 'asc') {
					return a[criterion].toString().toUpperCase() < b[criterion].toString().toUpperCase() ? -1 : 1
				} else {
					return a[criterion].toString().toUpperCase() < b[criterion].toString().toUpperCase() ? 1 : -1
				}
			})
	}

	sortAscending(a, b) {
		return a - b
	}
	sortDescending(a, b) {
		return b - a
	}

	render() {
		const {
			additionalSkillListing,
			user: {
				id,
				firstName,
				lastName,
				title,
				location,
				mail,
				phone,
				skills
			}
		} = this.props

		const {
			shouldShowAllSkills,
			numberOfSkillsToShow,
			sortedSkills,
			topWills
		} = this.state

		return (
			<ul class="basic-profile">
				<li class="info">
					<div class={`avatar avatar-${this.getAvatarColor()}`}><span class="fallback-letter">{firstName.charAt(0).toUpperCase()}</span></div>
					<p class="name">{firstName} {lastName}</p>
					<p class="id">{id}</p>
					<p class="department">{title}</p>
					<p class="location phone">{location} / TEL. {phone}</p>
					<Link class="mail" href={`mailto:${mail}`} target="_blank"></Link>
					<Link class="slack" href={`https://sinnerschrader.slack.com/messages/@${firstName.toLowerCase()}.${lastName.toLowerCase()}`} target="_blank"></Link>
					<Link class="move" href={`http://move.sinner-schrader.de/?id=${id}`} target="_blank"></Link>
				</li>

				{this.props.additionalSkillListing /*e.g. searched skills*/}

				<li class="top-wills skill-listing ">
					<div class="listing-header">Top Wills</div>
					<ul class="skills-list">
						{topWills.map((skill, i) => {
							if (i < 5 && skill['willLevel'] > 1)
								return <SkillItem skill={skill} key={i}></SkillItem>
						})}
					</ul>
				</li>
				<li class="all-skills skill-listing">
					<div class="listing-header">Alle Skills
						<ul class="sort-buttons">
							<li class="sort-button sort-button-name" onClick={() => this.setState({ sortedSkills: this.sortSkills('name', 'asc') })}
							><span class="sort-button-label">Name</span></li>
							<li class="sort-button sort-button-skill" onClick={() => this.setState({ sortedSkills: this.sortSkills('skillLevel', 'desc') })}
							><span class="sort-button-label">Skill</span></li>
							<li class="sort-button sort-button-will" onClick={() => this.setState({ sortedSkills: this.sortSkills('willLevel', 'desc') })}
							><span class="sort-button-label">Will</span></li>
						</ul>
					</div>
					<ul class="skills-list">
						{sortedSkills.map((skill, i) => {
							//display show-more-link after maximum skills to show
							if (i < (numberOfSkillsToShow)) {
								return <SkillItem skill={skill} key={i}></SkillItem>
							}
						}
						)}
					</ul>
					<a class="show-more-link" onClick={this.showAllSkills} href=""></a>
				</li>
			</ul>
		)
	}
}
