import React from 'react'
import { Router, Link, browserHistory } from 'react-router'
import BasicProfile from "./basic-profile.jsx"
import config from '../../config.json'
import SkillItem from '../skill-item/skill-item.jsx'

import { connect } from 'react-redux'

class OthersProfile extends React.Component {
	constructor(props) {
		super(props)
		this.state = {
			userId: this.props.params.id || "id",
			dataLoaded: false,
			infoLayerAt: 0
		}
		this.renderSearchedSkills = this.renderSearchedSkills.bind(this)
	}

	componentDidMount() {
		fetch(`${config.backendServer}/users/${this.state.userId}`)
			.then(response => response.json())
			.then(user => {
				this.setState({
					user,
					dataLoaded: true
				})
			})
			.catch(function (error) {
				console.error(error)
			})
	}

	renderSearchedSkills() {
		const { skills } = this.state.user
		const { searchedSkills } = this.props
		if (!searchedSkills || searchedSkills.length <= 0) {
			return
		}
		return (
			<li class="searched-skills skill-listing">
				<div class="listing-header">Gesuchte Skills</div>
				<ul class="skills-list">
					{skills
						.filter(skill => searchedSkills.indexOf(skill.name) !== -1)
						.map((skill, i) => {
							return (
								<SkillItem key={i} skill={skill} />
							)
						})
					}
				</ul>
			</li>
		)
	}

	infoLayer(data) {
		//nothing to return
	}

	render() {
		const { dataLoaded, user } = this.state
		return (
			<div class="profile">
				{dataLoaded ?
					<BasicProfile
						user={user}
						infoLayer={this.infoLayer}
						additionalSkillListing={this.renderSearchedSkills()} />
					: ""
				}
			</div>
		)
	}
}
function mapStateToProps(state) {
	return {
		searchedSkills: state.results.searched
	}
}

export default connect(mapStateToProps)(OthersProfile)