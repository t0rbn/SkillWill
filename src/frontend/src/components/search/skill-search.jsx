import React from 'react'
import SearchBar from './search-bar.jsx'
import Results from '../results/results.jsx'
import Skill from '../skill/skill.jsx'
import config from '../../config.json'
import { fetchSkills } from '../../actions'
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
		// this.handleSearchBarDelete = this.handleSearchBarDelete.bind(this)
	}

	// requestSearch(searchTerms) {

	// 	fetch(`${config.backendServer}/skills?search=${searchTerms}`)
	// 		.then(r => {
	// 			r.json().then(data => {
	// 				this.setState({
	// 					results: data,
	// 					searchStarted: true,
	// 					searchItems: searchTerms,
	// 					shouldUpdate: true
	// 				})
	// 			})
	// 		})
	// 		.catch(error => {
	// 			console.error("requestSearch" + error)
	// 			this.setState({ results: null })
	// 		})
	// }

	// update component only if search has changed
	// shouldComponentUpdate(nextProps, nextState) {
	// 	if (nextState.shouldUpdate && ((this.state.results !== nextState.results) || (this.state.searchItems.length !== nextState.searchItems.length))) {
	// 		return true
	// 	}
	// 	return false
	// }

		handleSearchBarInput(searchString) {
		const { searchItems } = this.state
		this.setState({
			searchItems: searchItems.concat([searchString]),
			searchStarted: true
		})
		this.props.fetchSkills(this.state.searchItems)
	}

	toggleUpdate(bool) {
		this.setState({
			searchItems
		})
	}

	renderResults(searchStarted, results, searchItems) {
		/* display Results component only when there has been an inital search */
		if (searchStarted) {
			return (
				<Results
					results={results}
					searchTerms={searchItems}
					noResultsLabel={"Keinen passenden Skill gefunden?"}>
					<Skill
						handleEdit={this.props.handleEdit}
						userData={this.props.data} />
				</Results>
			)
		}
	}

	render() {
		const { results, searchItems, searchStarted } = this.state
		return (
			<div class="searchbar">
				<p class="subtitle">Neuen Skill hinzufügen</p>
				<p class="search-description">Suche nach Skills, die Du auf Deinem Profil zeigen möchtest</p>
				<SearchBar
					onInputChange={this.handleSearchBarInput}
					onInputDelete={this.handleSearchBarDelete}
					parent={this}
					searchTerms={searchItems} />
				{this.renderResults(searchStarted, results, searchItems)}
			</div>
		)
	}
}

export default connect(null, { fetchSkills })(SkillSearch)
