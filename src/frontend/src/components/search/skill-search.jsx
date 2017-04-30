import React from 'react'
import SearchBar from './search-bar.jsx'
import Results from '../results/results.jsx'
import Skill from '../skill/skill.jsx'
import config from '../../config.json'
import { fetchSkills, keepSearchTerms } from '../../actions'
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
	shouldComponentUpdate(nextProps, nextState) {
		if (nextState.shouldUpdate && ((this.state.results !== nextState.results) || (this.state.searchItems.length !== nextState.searchItems.length))) {
			return true
		}
		return false
	}

		handleSearchBarInput(searchString) {
		const { searchItems } = this.state
		this.setState({
			searchItems: searchItems.concat([searchString]),
		})
		this.props.keepSearchTerms(this.state.searchItems)
		this.props.fetchSkills(this.state.searchItems)
		console.log('constructor',this.props)
	}

	toggleUpdate(bool) {
		this.setState({
			searchItems
		})
	}

	componentDidUpdate(prevProps, prevState) {
		const { route, searchItems, locationString } = this.state
		const newRoute = `${route}?skills=${searchItems}${locationString}`
		const prevSearchString = `search${prevProps.location.search}`
		document.SearchBar.SearchInput.focus()
		if (prevSearchString !== newRoute) {
			this.context.router.push(newRoute)
			// window.history.pushState({}, "", newRoute)
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

function mapStateToProps(state) {
	console.log('mapStateToProps',state.reducer)
	return {
		skills: state.reducer.results,
		searchTerms: state.reducer.keepSearchTerms
	};
}

export default connect(mapStateToProps, { fetchSkills, keepSearchTerms })(SkillSearch)
