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
		this.searchedSkills = this.searchedSkills.bind(this)
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

	searchedSkills() {
		const { skills } = this.state.user
		const { searched } = this.props
		if (!searched || searched.length <= 0) {
			return
		}
		return (
			<li class="searched-skills skill-listing">
				<div class="listing-header">Gesuchte Skills</div>
				<ul class="skills-list">
					{skills
						.filter(skill => searched.indexOf(skill.name) !== -1)
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
						additionalSkillListing={this.searchedSkills()} />
					: ""
				}
			</div>
		)
	}
}
function mapStateToProps(state) {
	return {
		searched: state.results.searched
	}
}

export default connect(mapStateToProps)(OthersProfile)