import React from 'react'
import User from "../user/user.jsx"
import config from '../../config.json'
import Editor from '../editor/editor.jsx'
import { Link } from 'react-router'
import Levels from '../level/level.jsx'

export default class BasicProfile extends React.Component {
	constructor(props) {
		super(props)
		this.state = {
			userAvatarPath: "",
			showAllSkills: this.props.showAllSkills,
			infoLayerAt: this.props.openLayerAt,
			showMoreLabel: "Mehr",
			editLayerAt: null,
			numberOfSkillsToShow: 6
		}
		this.showAllSkills = this.showAllSkills.bind(this)
		this.openInfoLayer = this.openInfoLayer.bind(this)
		this.closeInfoLayer = this.closeInfoLayer.bind(this)
		this.renderSkills = this.renderSkills.bind(this)
		this.getAvatarColor = this.getAvatarColor.bind(this)
	}

	showAllSkills(e) {
		e.preventDefault()
		this.setState({
			showAllSkills: !(this.state.showAllSkills),
		})
		e.target.classList.toggle("open")
	}

	openInfoLayer(i) {
		if (i == this.state.infoLayerAt) {
			this.closeInfoLayer()
		}
		else {
			this.setState({
				infoLayerAt: i //set Layer to index of clicked item
			})
		}
	}

	closeInfoLayer() {
		this.setState({
			infoLayerAt: -1 //unset Layer
		})
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

	renderSkills(skill, i) {
		return (
			<div>
				{this.state.infoLayerAt == i
					? <div class="close-layer" onClick={this.closeInfoLayer}></div>
					: ""
				}
				<Levels skill={skill} key={i} onClick={() => this.openInfoLayer(i)}></Levels>
				{
					//open Info-Layer on clicked Item
					this.state.infoLayerAt == i ?
						<div class="info-layer">
							<p class="skill-title">{skill.name}</p>
							{
								// for my-profile only
								this.props.infoLayer(skill, i, this.state.showAllSkills)
							}
							<a class="close-btn small" onClick={this.closeInfoLayer}></a>
						</div>
						: ""
				}
			</div>
		)
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

				{this.props.additionalSkillListing} {/*e.g. searched skills}*/}

				<li class="top-wills skill-listing ">
					<div class="listing-header">Top Willls</div>
					<ul class="skills-list">
						{skills.map((skill, i) => {
							if (i < 3)
								return this.renderSkills(skill, i)
						})}
					</ul>
				</li>
				<li class="all-skills skill-listing">
					<div class="listing-header">Alle Skillls</div>

					<ul class="skills-list">
						{skills.map((skill, i) => {
							//display show-more-link after maximum skills to show
							if (this.state.showAllSkills) {
								return this.renderSkills(skill, i)
							}
							else {
								if (i < (this.state.numberOfSkillsToShow)) {
									return this.renderSkills(skill, i)
								}
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
