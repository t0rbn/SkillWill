import React from 'react'
import { Router, Link, browserHistory } from 'react-router'
import BasicProfile from "./basic-profile.jsx"
import config from '../../config.json'
import Levels from '../level/level.jsx'
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
		const { searchTerms } = this.props
		return (
			<li class="searched-skills skill-listing">
				<div class="listing-header">Gesuchte Skillls</div>
				<ul class="skills-list">
					{skills.map((skill, i) => {
						if (i <= 3)
							return (
								<Levels key={i} skill={skill} />
							)
					})}
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
					<div>
						<BasicProfile
							user={user}
							infoLayer={this.infoLayer}
							additionalSkillListing={this.searchedSkills()} />
					</div>
					: ""
				}
			</div>
		)
	}
}
function mapStateToProps(state) {
	return {
		searchTerms: state.reducer.searchTerms
	}
}

export default connect(mapStateToProps)(OthersProfile)