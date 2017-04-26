import React from 'react'
import SearchBar from './search-bar.jsx'
import Results from '../results/results.jsx'
import Skill from '../skill/skill.jsx'
import config from '../../config.json'

export default class SkillSearch extends React.Component {

	constructor(props) {
		super(props)
		this.state = {
			results: null,
			searchItems: [],
			searchStarted: false,
			shouldUpdate: false
		}
		this.handleSearchBarInput = this.handleSearchBarInput.bind(this)
		this.handleSearchBarDelete = this.handleSearchBarDelete.bind(this)
	}


	handleSearchBarInput(searchString) {
		this.setState({
			searchItems: this.state.searchItems.concat([searchString])
		})
	}

	handleSearchBarDelete(deleteItem) {
		const { searchItems } = this.state
		searchItems.splice(searchItems.indexOf(deleteItem), 1)
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
					searchTerms={searchItems}/>
			</div>
		)
	}
}
