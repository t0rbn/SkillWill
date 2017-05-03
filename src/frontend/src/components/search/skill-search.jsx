import React from 'react'
import SearchBar from './search-bar.jsx'
import Results from '../results/results.jsx'
import Skill from '../skill/skill.jsx'
import config from '../../config.json'
import { fetchSkill } from '../../actions'
import { connect } from 'react-redux'

class SkillSearch extends React.Component {

	constructor(props) {
		super(props)
		this.state = {
			results: null,
			searchItems: [],
			searchStarted: false,
			shouldUpdate: false
		}

		this.toggleUpdate = this.toggleUpdate.bind(this)
		this.handleSearchBarInput = this.handleSearchBarInput.bind(this)
	}

	handleSearchBarInput(searchString) {
		const { searchItems } = this.state
		this.setState({
			searchItems: searchItems.concat([searchString]),
			searchStarted: true
		})
		this.props.fetchSkill(this.state.searchItems)
	}

	toggleUpdate(bool) {
		this.setState({
			searchItems
		})
	}

	render() {
		const { searchItems } = this.state
		const { handleEdit, skill, user} = this.props
		return (
			<div class="searchbar">
				<p class="subtitle">Neuen Skill hinzufügen</p>
				<p class="search-description">Suche nach Skills, die Du auf Deinem Profil zeigen möchtest</p>
				<SearchBar
					onInputChange={this.handleSearchBarInput}
					parent={this}
					searchTerms={searchItems} />
				<Skill
					handleEdit={handleEdit}
					skill={skill}
					user={user} />
			</div>
		)
	}
}

function mapStateToProps(state) {
	return {
		skill: state.reducer.skill,
		user: state.reducer.user,
		searchTerms: state.reducer.searchTerms
	}
}

export default connect(mapStateToProps, { fetchSkill })(SkillSearch)
